package com.mindolph.base.genai.rag;

import com.mindolph.core.llm.DatasetMeta;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IdUtils;
import org.swiftboot.util.NumberFormatUtils;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;
import static com.mindolph.core.constant.SupportFileTypes.TYPE_MIND_MAP;

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


                List<File> files = datasetMeta.getFiles();
                int total = files.size();
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    if (file.isDirectory()) {
                        FileUtils.listFiles(file, SUPPORTED_EMBEDDING_FILE_TYPES, true).forEach(f -> {
                            embedFile(f, datasetMeta.getId(), embeddingModel, embeddingStore);
                        });
                    }
                    else if (file.isFile()) {
                        embedFile(file, datasetMeta.getId(), embeddingModel, embeddingStore);
                    }
                    super.progressEventSource.emit("%f".formatted(NumberFormatUtils.toPercent((i / total), 1)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                completed.accept(e);
                return;
            }
            completed.accept("Embedding done successfully");
        }).start();
    }

    private void embedFile(File f, String datasetId, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        log.info("embed file {}", f);
        String extension = FilenameUtils.getExtension(f.getName());
        DocumentParser documentParser;
        DocumentSplitter splitter;
        switch (extension) {
            case TYPE_MIND_MAP:
                documentParser = new MindMapDocumentParser();
                splitter = new MindMapDocumentSplitter(1024, 1024);
                break;
            default:
                documentParser = new TextDocumentParser();
                splitter = DocumentSplitters.recursive(300, 0);
        }

        log.debug("embed file with parser %s and splitter %s".formatted(documentParser.getClass().getSimpleName(), splitter.getClass().getSimpleName()));

        Document document = FileSystemDocumentLoader.loadDocument(f.getPath(), documentParser);
        String docId = this.persistDocumentMetaIfNotExist(datasetId, f.getPath());
        if (StringUtils.isBlank(docId)) {
            log.error("Failed to persist document meta: {}", f.getPath());
            return;
        }

        // remove existing embedding first
        embeddingStore.removeAll(new MetadataFilterBuilder("doc_id").isEqualTo(docId));

        // do split and embed.
        List<TextSegment> segments = splitter.split(document);
        segments.forEach(segment -> {
            segment.metadata().put("doc_id", docId);
        });
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        try {
            embeddingStore.addAll(embeddings, segments);
        } catch (Exception e) {
            this.updateDocument(docId, segments.size(), false);
            throw new RuntimeException(e);
        }
        this.updateDocument(docId, segments.size(), true);
    }


    private String persistDocumentMetaIfNotExist(String datasetId, String filePath) {
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

    private void updateDocument(String docId, int blockCount, boolean embedded) {
        super.withJdbcConnection((Function<Connection, Void>) connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "update mindolph_doc set block_count = ?, embedded = ? where id = ?");
                ps.setInt(1, blockCount);
                ps.setBoolean(2, embedded);
                ps.setString(3, docId);
                if (ps.executeUpdate() == 0) {
                    throw new RuntimeException("Failed to update document meta");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

//    private void deleteEmbeddingForDocument(String docId) {
//        super.withJdbcConnection((Function<Connection, Void>) connection -> {
//            try {
//                PreparedStatement ps = connection.prepareStatement(
//                        "delete from mindolph_doc where id = ?");
//                ps.setInt(1, blockCount);
//                ps.setBoolean(2, embedded);
//                ps.setString(3, docId);
//                if (ps.executeUpdate() == 0) {
//                    throw new RuntimeException("Failed to update document meta");
//                }
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//            return null;
//        });
//    }

}
