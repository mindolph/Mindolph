package com.mindolph.base.genai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @since unknown
 */
public class EmbeddingService {

    private static final EmbeddingService ins = new EmbeddingService();

    public static EmbeddingService getInstance() {
        return ins;
    }

    private EmbeddingService() {
    }

    public void embed(String agentId, List<File> files, Consumer<Object> completed) {
        new Thread(() -> {
            List<Document> allDocuments = new ArrayList<>();
            for (File file : files) {
                List<Document> documents = FileSystemDocumentLoader.loadDocuments(file.getPath(),
                        FileSystems.getDefault().getPathMatcher("glob:*.md"));
                allDocuments.addAll(documents);
            }

            EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();
            EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                    .host(System.getenv("PG_HOST"))
                    .port(5433)
                    .database("postgres")
                    .user(System.getenv("PG_USER"))
                    .password(System.getenv("PG_PASSWORD"))
                    .table("mindolph_embeddings")
                    .createTable(true)
                    .dimension(embeddingModel.dimension())
                    .dropTableFirst(true)
                    .metadataStorageConfig(DefaultMetadataStorageConfig.builder().storageMode(MetadataStorageMode.COLUMN_PER_KEY)
                            .columnDefinitions(Arrays.asList("agent_id varchar(36) not null")).build())
                    .build();

            try {
                for (Document document : allDocuments) {
                    DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
                    List<TextSegment> segments = splitter.split(document);
                    segments.forEach(segment -> {segment.metadata().put("agent_id", agentId);});
                    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                    embeddingStore.addAll(embeddings, segments);
                }
            } catch (Exception e) {
                e.printStackTrace();
                completed.accept(e);
            }
            completed.accept(null);
        }).start();

    }
}
