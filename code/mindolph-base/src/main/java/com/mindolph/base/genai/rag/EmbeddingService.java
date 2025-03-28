package com.mindolph.base.genai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * @since unknown
 */
public class EmbeddingService extends BaseEmbedding {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private static final EmbeddingService ins = new EmbeddingService();

    public static EmbeddingService getInstance() {
        return ins;
    }

    private EmbeddingService() {
    }

    private Object persistDocumentMetaIfNotExist(String agentId, String filePath) {
        String fileName = FilenameUtils.getBaseName(filePath);
        return super.withJdbcConnection(conn -> {
            try {
                String docId = null;
                PreparedStatement ps = conn.prepareStatement("select id, file_name, agent_id from mindolph_doc where agent_id = ? and file_path = ?");
                ps.setString(1, agentId);
                ps.setString(2, filePath);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    docId = rs.getString("id");
                    return docId; // already exists
                }
                docId = IdUtils.makeUUID();
                PreparedStatement insertStmt = conn.prepareStatement("insert into mindolph_doc(id, agent_id, file_path, file_name, block_count) values(?, ?, ?, ?, ?)");
                insertStmt.setString(1, docId);
                insertStmt.setString(2, agentId);
                insertStmt.setString(3, filePath);
                insertStmt.setString(4, fileName);
                insertStmt.setInt(5, 0);
                if (insertStmt.executeUpdate() == 0) {
                    throw new RuntimeException("Failed to save document meta");
                }
                return docId;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void embed(String agentId, List<File> files, Consumer<Object> completed) {
        new Thread(() -> {
            if (files == null || files.isEmpty()) {
                log.warn("No files to be embedded");
                return;
            }

            EmbeddingModel embeddingModel = super.createEmbeddingModel();
            EmbeddingStore<TextSegment> embeddingStore = super.createEmbeddingStore(true, false);

            try {
                for (File file : files) {
                    if (file.isDirectory()) {
                        FileUtils.listFiles(file, new String[]{"md"}, true).forEach(f -> {
                            Document document = FileSystemDocumentLoader.loadDocument(f.getPath());
                            Object docId = this.persistDocumentMetaIfNotExist(agentId, f.getPath());
                            if (docId == null) {
                                log.error("Failed to persist document meta: {}", f.getPath());
                                return;
                            }

                            DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
                            List<TextSegment> segments = splitter.split(document);
                            segments.forEach(segment -> {
                                segment.metadata().put("doc_id", (String) docId);
                            });
                            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                            embeddingStore.addAll(embeddings, segments);
                        });
                    }
                    else if (file.isFile()) {
                        // TODO
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                completed.accept(e);
            }
            completed.accept(null);
        }).start();

    }
}
