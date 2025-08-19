package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.EmbeddingStage;
import com.mindolph.core.llm.DataSourceConfig;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since unknown
 */
public abstract class BaseEmbeddingService {


    private static final Logger log = LoggerFactory.getLogger(BaseEmbeddingService.class);

    protected final EventSource<EmbeddingProgress> progressEventSource = new EventSource<>();

    protected <T> T withJdbcConnection(Function<Connection, T> handler) {
        DataSourceConfig dsConfig = new DataSourceConfig(System.getenv("PG_HOST"), Integer.parseInt(System.getenv("PG_PORT")));
        dsConfig.setUser(System.getenv("PG_USER"));
        dsConfig.setPassword(System.getenv("PG_PASSWORD"));
        dsConfig.setDatabase(System.getenv("PG_DATABASE"));
        String url = "jdbc:postgresql://%s:%d/%s".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase());
        try (Connection conn = DriverManager.getConnection(url, dsConfig.getUser(), dsConfig.getPassword())) {
            return handler.apply(conn);
        } catch (SQLException e) {
            log.error("Error connecting to the database", e);
            e.printStackTrace(); // Print the full stack trace for debugging
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
        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host(System.getenv("PG_HOST"))
                .port(Integer.parseInt(System.getenv("PG_PORT")))
                .database("postgres")
                .user(System.getenv("PG_USER"))
                .password(System.getenv("PG_PASSWORD"))
                .table("mindolph_embedding_%d".formatted(embeddingModel.dimension()))
                .createTable(createTable)
                .dimension(embeddingModel.dimension())
                .dropTableFirst(dropTable)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder().storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                        .columnDefinitions(List.of("doc_id varchar(32) not null")).build())
                .build();
        return embeddingStore;
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

    /**
     * @param file
     * @param success
     * @param msg
     * @param stage 进行到的阶段
     * @param ratio 0-1 完成比例
     */
    public record EmbeddingProgress(File file, boolean success, String msg, EmbeddingStage stage, float ratio) {
    }
}
