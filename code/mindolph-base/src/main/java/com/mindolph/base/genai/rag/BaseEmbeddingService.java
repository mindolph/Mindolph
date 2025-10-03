package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.Stage;
import com.mindolph.base.genai.llm.LlmConfig;
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
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since 1.13.0
 */
public abstract class BaseEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(BaseEmbeddingService.class);

    private static final int CONNECT_TIME_IN_SECOND = 10;

    protected VectorStoreMeta vectorStoreMeta;

    protected final EventSource<EmbeddingProgress> progressEventSource = new EventSource<>();

    public BaseEmbeddingService() {
        DriverManager.setLoginTimeout(5);
    }

    protected void loadVectorStorePrefs() {
        this.vectorStoreMeta = LlmConfig.getIns().loadActiveVectorStorePrefs();
    }

    protected <T> T withJdbcConnection(Function<Connection, T> handler) {
        return withJdbcConnection(handler, CONNECT_TIME_IN_SECOND);
    }

    protected <T> T withJdbcConnection(Function<Connection, T> handler, int timeout) {
        this.loadVectorStorePrefs();
        DataSourceConfig dsConfig = new DataSourceConfig(vectorStoreMeta.getHost(), vectorStoreMeta.getPort());
        dsConfig.setUser(vectorStoreMeta.getUsername());
        dsConfig.setPassword(vectorStoreMeta.getPassword());
        dsConfig.setDatabase(vectorStoreMeta.getDatabase());
        String url = "jdbc:postgresql://%s:%d/%s?connectTimeout=%d".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase(), timeout);
        log.info("Try to connect database: %s".formatted(url));
        try (Connection conn = DriverManager.getConnection(url, dsConfig.getUser(), dsConfig.getPassword())) {
            log.info("Connected to vector database: %s:%d/%s".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase()));
            return handler.apply(conn);
        } catch (Exception e) {
            log.error("Error connecting to the database", e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public void checkDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException("Unable to load postgres driver", e);
        }
    }

    public void testConnection() {
        this.withJdbcConnection((conn) -> {
            log.info("Start to test database connection");
            try {
                PreparedStatement ps = conn.prepareStatement("select 1");
                ps.execute();
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
            log.info("Done with testing database connection");
            return null;
        }, 5);
    }

    protected EmbeddingModel createEmbeddingModel(String langCode, String modelName) {
        File baseDir = AppUtils.getAppBaseDir();
        String pathToModel = "models/%s/%s/model.onnx".formatted(langCode, modelName);
        String pathToTokenizer = "models/%s/%s/tokenizer.json".formatted(langCode, modelName);
        File modelFile = new File(baseDir, pathToModel);
        File tokenizerFile = new File(baseDir, pathToTokenizer);
        if (!modelFile.exists()) {
            // download
            this.emitEmbeddingProgressEvent("Downloading onnx model...");
        }
        if (!tokenizerFile.exists()) {
            // download
            this.emitEmbeddingProgressEvent("Downloading onnx tokenizer...");
        }
        PoolingMode poolingMode = PoolingMode.MEAN;
        this.emitEmbeddingProgressEvent("Loading onnx model tokenizer...");
        return new OnnxEmbeddingModel(modelFile.getPath(), tokenizerFile.getPath(), poolingMode);
    }

    protected EmbeddingStore<TextSegment> createEmbeddingStore(EmbeddingModel embeddingModel, boolean createTable, boolean dropTable) {
        this.emitEmbeddingProgressEvent("Preparing embedding store...");
        try {
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
                    .metadataStorageConfig(
                            DefaultMetadataStorageConfig.builder()
                                    .storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                                    .columnDefinitions(List.of("doc_id varchar(32) not null"))
                                    .build())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public void listenOnProgressEvent(Consumer<EmbeddingProgress> callback) {
        progressEventSource.subscribe(callback);
    }

    // TBD
    public void emitEmbeddingProgressEvent(String message) {
        progressEventSource.push(new EmbeddingProgressBuilder().success().msg(message).stage(Stage.PREPARE).build());
    }

    // TBD
    public void emitEmbeddingProgressEvent(File file, boolean success, int successCount, String message, float ratio) {
        progressEventSource.push(new EmbeddingProgressBuilder().file(file).success(success).successCount(successCount).msg(message).stage(Stage.EMBEDDING).ratio(ratio).build());
    }

    // TBD
    public void emitUnembeddingProgressEvent(File file, int successCount, String message, float ratio) {
        progressEventSource.push(new EmbeddingProgressBuilder().file(file).success(true).successCount(successCount).msg(message).stage(Stage.UNEMBEDDING).ratio(ratio).build());
    }


}
