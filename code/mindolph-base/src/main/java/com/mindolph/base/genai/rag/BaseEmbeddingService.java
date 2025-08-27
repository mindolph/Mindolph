package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.EmbeddingStage;
import com.mindolph.core.llm.DataSourceConfig;
import com.mindolph.core.llm.VectorStoreMeta;
import com.mindolph.core.util.AppUtils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since unknown
 */
public abstract class BaseEmbeddingService {


    private static final Logger log = LoggerFactory.getLogger(BaseEmbeddingService.class);

    protected VectorStoreMeta vectorStoreMeta;

    protected final EventSource<EmbeddingProgress> progressEventSource = new EventSource<>();

    protected <T> T withJdbcConnection(Function<Connection, T> handler) {
        DataSourceConfig dsConfig = new DataSourceConfig(vectorStoreMeta.getHost(), vectorStoreMeta.getPort());
        dsConfig.setUser(vectorStoreMeta.getUsername());
        dsConfig.setPassword(vectorStoreMeta.getPassword());
        dsConfig.setDatabase(vectorStoreMeta.getDatabase());
        String url = "jdbc:postgresql://%s:%d/%s".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase());
        try (Connection conn = DriverManager.getConnection(url, dsConfig.getUser(), dsConfig.getPassword())) {
            return handler.apply(conn);
        } catch (Exception e) {
            log.error("Error connecting to the database", e);
            throw new RuntimeException(e);
        }
    }

    protected EmbeddingModel createEmbeddingModel(String langCode, String modelName) {
        File baseDir = AppUtils.getAppBaseDir();
        String pathToModel = "models/%s/%s/model.onnx".formatted(langCode, modelName);
        String pathToTokenizer = "models/%s/%s/tokenizer.json".formatted(langCode, modelName);
        File modelFile = new File(baseDir, pathToModel);
        File tokenizerFile = new File(baseDir, pathToTokenizer);
        if (!modelFile.exists()) {
            // download
            this.emitProgressEvent("Downloading onnx model...");
        }
        if (!tokenizerFile.exists()) {
            // download
            this.emitProgressEvent("Downloading onnx tokenizer...");
        }
        PoolingMode poolingMode = PoolingMode.MEAN;
        this.emitProgressEvent("Loading onnx model tokenizer...");
        return new OnnxEmbeddingModel(modelFile.getPath(), tokenizerFile.getPath(), poolingMode);
    }

    protected EmbeddingStore<TextSegment> createEmbeddingStore(EmbeddingModel embeddingModel, boolean createTable, boolean dropTable) {
        this.emitProgressEvent("Preparing embedding store...");
        return PgVectorEmbeddingStore.builder()
                .host(vectorStoreMeta.getHost())
                .port(vectorStoreMeta.getPort())
                .database(vectorStoreMeta.getDatabase())
                .user(vectorStoreMeta.getUsername())
                .password(vectorStoreMeta.getPassword())
                .table("mindolph_embedding_%d".formatted(embeddingModel.dimension()))
                .createTable(createTable)
                .dimension(embeddingModel.dimension())
                .dropTableFirst(dropTable)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder().storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                        .columnDefinitions(List.of("doc_id varchar(32) not null")).build())
                .build();
    }

    public void listenOnProgressEvent(Consumer<EmbeddingProgress> callback) {
        progressEventSource.subscribe(callback);
    }

    public void emitProgressEvent(String message) {
        progressEventSource.push(new EmbeddingProgress(null, true, message, EmbeddingStage.PREPARE, 0));
    }

    public void emitProgressEvent(File file, boolean success, String message, float ratio) {
        progressEventSource.push(new EmbeddingProgress(file, success, message, EmbeddingStage.EMBEDDING, ratio));
    }

    public void setVectorStoreMeta(VectorStoreMeta vectorStoreMeta) {
        this.vectorStoreMeta = vectorStoreMeta;
    }

    /**
     * @param file
     * @param success
     * @param msg
     * @param stage   the stage of whole embedding.
     * @param ratio   0-1 the percent to complete
     */
    public record EmbeddingProgress(File file, boolean success, String msg, EmbeddingStage stage,
                                    float ratio) implements Serializable {
        public EmbeddingProgress(String msg) {
            this(null, false, msg, null, 0);
        }

        public EmbeddingProgress(File file, boolean success, String msg, EmbeddingStage stage, float ratio) {
            this.file = file;
            this.success = success;
            this.msg = msg;
            this.stage = stage;
            this.ratio = ratio;
        }
    }
}
