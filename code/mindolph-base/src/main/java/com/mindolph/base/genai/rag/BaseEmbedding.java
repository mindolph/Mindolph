package com.mindolph.base.genai.rag;

import com.mindolph.core.llm.DataSourceConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @since unknown
 */
public class BaseEmbedding {

    protected <T> Object withJdbcConnection(Function<Connection, T> handler) {
        DataSourceConfig dsConfig = new DataSourceConfig(System.getenv("PG_HOST"), 5433);
        dsConfig.setUser(System.getenv("PG_USER"));
        dsConfig.setPassword(System.getenv("PG_PASSWORD"));
        dsConfig.setDatabase("postgres");
        String url = "jdbc:postgresql://%s:%d/%s".formatted(dsConfig.getHost(), dsConfig.getPort(), dsConfig.getDatabase());
        try (Connection conn = DriverManager.getConnection(url, dsConfig.getUser(), dsConfig.getPassword())) {
            return handler.apply(conn);
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace for debugging
            return null;
        }
    }


    protected EmbeddingModel createEmbeddingModel() {
        EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();
        return embeddingModel;
    }

    protected EmbeddingStore<TextSegment> createEmbeddingStore(boolean createTable, boolean dropTable) {
        EmbeddingModel embeddingModel = this.createEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host(System.getenv("PG_HOST"))
                .port(5433)
                .database("postgres")
                .user(System.getenv("PG_USER"))
                .password(System.getenv("PG_PASSWORD"))
                .table("mindolph_embeddings")
                .createTable(createTable)
                .dimension(embeddingModel.dimension())
                .dropTableFirst(dropTable)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder().storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                        .columnDefinitions(Arrays.asList("doc_id varchar(32) not null")).build())
                .build();
        return embeddingStore;
    }
}
