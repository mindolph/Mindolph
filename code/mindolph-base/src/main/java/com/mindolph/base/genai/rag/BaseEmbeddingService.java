package com.mindolph.base.genai.rag;

import com.mindolph.base.genai.event.AiEventBus;
import com.mindolph.base.genai.event.PrepareEvent;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.DataSourceConfig;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.VectorStoreMeta;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static com.mindolph.core.constant.GenAiConstants.lookupModelMeta;

/**
 * @since 1.13.0
 */
public abstract class BaseEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(BaseEmbeddingService.class);

    private static final int CONNECT_TIME_IN_SECOND = 10;

    protected VectorStoreMeta vectorStoreMeta;

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
        ModelMeta modelMeta = lookupModelMeta(GenAiModelProvider.INTERNAL.name(),  modelName);
        File modelFile = LocalModelManager.getIns().getModelFile(langCode, modelMeta);
        File tokenizerFile = LocalModelManager.getIns().getTokenizerFile(langCode, modelMeta);
        if (!modelFile.exists()) {
            // download
            AiEventBus.getInstance().emitEvent(new PrepareEvent("Downloading onnx model..."));
        }
        if (!tokenizerFile.exists()) {
            // download
            AiEventBus.getInstance().emitEvent(new PrepareEvent("Downloading onnx tokenizer..."));
        }
        PoolingMode poolingMode = PoolingMode.MEAN;
        AiEventBus.getInstance().emitEvent(new PrepareEvent("Loading onnx model tokenizer..."));
        return new OnnxEmbeddingModel(modelFile.getPath(), tokenizerFile.getPath(), poolingMode);
    }

    protected EmbeddingStore<TextSegment> createEmbeddingStore(EmbeddingModel embeddingModel, boolean createTable, boolean dropTable) {
        AiEventBus.getInstance().emitEvent(new PrepareEvent("Preparing embedding store..."));
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
}
