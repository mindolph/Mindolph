package com.mindolph.base.genai.rag;

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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since unknown
 */
public class BaseEmbeddingService {

    private final EventSource<String> progressEventSource = new EventSource<>();

    protected <T> T withJdbcConnection(Function<Connection, T> handler) {
        DataSourceConfig dsConfig = new DataSourceConfig(System.getenv("PG_HOST"), Integer.parseInt(System.getenv("PG_PORT")));
        dsConfig.setUser(System.getenv("PG_USER"));
        dsConfig.setPassword(System.getenv("PG_PASSWORD"));
        dsConfig.setDatabase(System.getenv("PG_DATABASE"));
        String url = "jdbc:postgresql://%s:%d/%s".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase());
        try (Connection conn = DriverManager.getConnection(url, dsConfig.getUser(), dsConfig.getPassword())) {
            return handler.apply(conn);
        } catch (SQLException e) {
            System.err.printf("Error connecting to the database: %s%n", e.getMessage());
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
            emitProgressEvent("Downloading onnx model...");
        }
        if (!tokenizerFile.exists()) {
            // download
            emitProgressEvent("Downloading onnx tokenizer...");
        }
        PoolingMode poolingMode = PoolingMode.MEAN;
        emitProgressEvent("Loading onnx model tokenizer...");
        return new OnnxEmbeddingModel(modelFile.getPath(), tokenizerFile.getPath(), poolingMode);
    }

    protected EmbeddingStore<TextSegment> createEmbeddingStore(EmbeddingModel embeddingModel, boolean createTable, boolean dropTable) {
        emitProgressEvent("Preparing embedding store...");
        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host(System.getenv("PG_HOST"))
                .port(Integer.parseInt(System.getenv("PG_PORT")))
                .database("postgres")
                .user(System.getenv("PG_USER"))
                .password(System.getenv("PG_PASSWORD"))
                .table("mindolph_embedding")
                .createTable(createTable)
                .dimension(embeddingModel.dimension())
                .dropTableFirst(dropTable)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder().storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                        .columnDefinitions(Arrays.asList("doc_id varchar(32) not null")).build())
                .build();
        return embeddingStore;
    }

    public void listenOnProgressEvent(Consumer<String> callback) {
        progressEventSource.subscribe(callback);
    }

    public void emitProgressEvent(String message) {
        progressEventSource.push(message);
    }
}
