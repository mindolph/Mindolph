package com.mindolph.base.genai.rag;

import com.mindolph.core.llm.DatasetMeta;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IdUtils;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;

/**
 * @since unknown
 */
public class EmbeddingService extends BaseEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private static final EmbeddingService ins = new EmbeddingService();

    public static EmbeddingService getInstance() {
        return ins;
    }

    private EmbeddingService() {
    }

    public void initDatabaseIfNotExist() {
        super.withJdbcConnection((Function<Connection, Void>) connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet tableMeta = metaData.getTables(null, null, "mindolph_doc", new String[]{"TABLE"});
                if (!tableMeta.next()) {
                    String sql = ClasspathResourceUtils.readResourceToString("genai/rag/init.sql");
                    if (StringUtils.isNotBlank(sql)) {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(sql);
                        log.info("init database successfully");
                    }
                    else {
                        throw new RuntimeException("init sql error");
                    }
                }
                else {
                    log.info("tables already exist, skip init");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private Object persistDocumentMetaIfNotExist(String datasetId, String filePath) {
        String fileName = FilenameUtils.getBaseName(filePath);
        return super.withJdbcConnection(conn -> {
            try {
                String docId = null;
                PreparedStatement ps = conn.prepareStatement("select id, file_name, dataset_id from mindolph_doc where dataset_id = ? and file_path = ?");
                ps.setString(1, datasetId);
                ps.setString(2, filePath);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    docId = rs.getString("id");
                    return docId; // already exists
                }
                docId = IdUtils.makeUUID();
                PreparedStatement insertStmt = conn.prepareStatement("insert into mindolph_doc(id, dataset_id, file_path, file_name, block_count) values(?, ?, ?, ?, ?)");
                insertStmt.setString(1, docId);
                insertStmt.setString(2, datasetId);
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

    public void embed(DatasetMeta datasetMeta, Consumer<Object> completed) {
        new Thread(() -> {
            if (datasetMeta.getFiles() == null || datasetMeta.getFiles().isEmpty()) {
                log.warn("No files to be embedded");
                return;
            }
            try {

                EmbeddingModel embeddingModel = super.createEmbeddingModel(
                        datasetMeta.getLanguageCode(), datasetMeta.getEmbeddingModel().name());
                EmbeddingStore<TextSegment> embeddingStore = super.createEmbeddingStore(embeddingModel, true, false);

                for (File file : datasetMeta.getFiles()) {
                    if (file.isDirectory()) {
                        FileUtils.listFiles(file, SUPPORTED_EMBEDDING_FILE_TYPES, true).forEach(f -> {
                            embedFile(f, datasetMeta.getId(), embeddingModel, embeddingStore);
                        });
                    }
                    else if (file.isFile()) {
                        embedFile(file, datasetMeta.getId(), embeddingModel, embeddingStore);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                completed.accept(e);
            }
            completed.accept("Embedding done successfully");
        }).start();
    }

    private void embedFile(File f, String datasetId, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        Document document = FileSystemDocumentLoader.loadDocument(f.getPath());
        Object docId = this.persistDocumentMetaIfNotExist(datasetId, f.getPath());
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
    }
}
