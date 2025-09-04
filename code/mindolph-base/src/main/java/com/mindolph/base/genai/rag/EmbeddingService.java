package com.mindolph.base.genai.rag;

import com.mindolph.core.async.GlobalExecutor;
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
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IdUtils;
import org.swiftboot.util.NumberFormatUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

    private EmbeddingModel embeddingModel;

    private EmbeddingStore<TextSegment> embeddingStore;

    public static EmbeddingService getInstance() {
        return ins;
    }

    private EmbeddingService() {
    }

    /**
     * Embed all files in the dataset.
     *
     * @param datasetMeta
     * @param completed
     */
    public void embedDataset(DatasetMeta datasetMeta, Consumer<EmbeddingProgress> completed) {
        if (datasetMeta.getFiles() == null || datasetMeta.getFiles().isEmpty()) {
            log.warn("No files to be embedded");
            completed.accept(new EmbeddingProgress("No files to be embedded"));
            return;
        }
        // load here for the config might be changed on the fly.
        super.loadVectorStorePrefs();
        if (vectorStoreMeta == null || !vectorStoreMeta.isAllSetup()) {
            log.debug("Vector store: %s".formatted(vectorStoreMeta));
            throw new RuntimeException("Vector store is not well setup");
        }
        if (!datasetMeta.isAllSetup()) {
            throw new RuntimeException("Dataset is not well setup for embedding");
        }
        GlobalExecutor.submit(() -> {
            List<File> selectedFiles = datasetMeta.getFiles();
            int total = selectedFiles.size();
            int successCount = 0;
            try {
                this.initDatabaseIfNotExist(); // check the database before starting embedding.
                if (embeddingModel == null) {
                    log.debug("Create embedding model instance");
                    embeddingModel = super.createEmbeddingModel(
                            datasetMeta.getLanguageCode(), datasetMeta.getEmbeddingModel());
                }
                if (embeddingStore == null) {
                    log.debug("Create embedding store instance");
                    embeddingStore = super.createEmbeddingStore(embeddingModel, true, false);
                }
                // find out embedded docs that need to be removed.
                List<String> selectedFilePaths = selectedFiles.stream().map(File::getPath).toList();
                List<EmbeddingDocEntity> allEmbeddings = findDocuments(datasetMeta.getId());
                List<EmbeddingDocEntity> toBeUnembedded = allEmbeddings.stream().filter(embeddingDocEntity -> !selectedFilePaths.contains(embeddingDocEntity.file_path())).toList();

                Integer removedCount = this.unembedFiles(toBeUnembedded, embeddingStore);
                log.info("%d embeddings docs has been removed".formatted(removedCount));

                for (int i = 0; i < selectedFiles.size(); i++) {
                    File file = selectedFiles.get(i);
                    if (datasetMeta.isStop()) {
                        log.info("Embedding is stopped by user");
                        completed.accept(new EmbeddingProgress("Embedding is stopped", successCount));
                        break;
                    }
                    BigDecimal ratio = BigDecimal.valueOf(i + 1).divide(new BigDecimal(total), 3, RoundingMode.HALF_UP);
                    boolean success = false;
                    if (file.isDirectory()) {
                        // NOTE: the directory is not included in the dataset yet, so the handling doesn't affect for now.
                        for (File f : FileUtils.listFiles(file, SUPPORTED_EMBEDDING_FILE_TYPES, true)) {
                            if (this.embedFile(f, datasetMeta.getId(), embeddingModel, embeddingStore)) {
                                successCount++;
                                success = true;
                            }
                        }
                    }
                    else if (file.isFile()) {
                        if (this.embedFile(file, datasetMeta.getId(), embeddingModel, embeddingStore)) {
                            successCount++;
                            success = true;
                        }
                    }
                    Double percent = NumberFormatUtils.toPercent(ratio.doubleValue(), 1);
                    super.emitProgressEvent(file, success, successCount, "Embedding...%.1f%%".formatted(percent), ratio.floatValue());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                completed.accept(new EmbeddingProgress("Failed: %s".formatted(e.getMessage()), successCount));
                return;
            }
//            if (!datasetMeta.isStop()) {
                completed.accept(new EmbeddingProgress("Embedding done with %d successes of %d".formatted(successCount, total), successCount));
//            }
        });
    }


    private void initDatabaseIfNotExist() {
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

    //
    private Integer unembedFiles(List<EmbeddingDocEntity> toBeEmbedded, EmbeddingStore<TextSegment> embeddingStore) {
        int deletedCount = 0;
        if (!toBeEmbedded.isEmpty()) {
            for (EmbeddingDocEntity docEntity : toBeEmbedded) {
                embeddingStore.removeAll(new IsEqualTo("doc_id", docEntity.id()));
                deletedCount += this.deleteDocument(docEntity.id());
            }
        }
        return deletedCount;
    }

    /**
     * Embed a file with specified embedding model and save to target embedding store,
     *
     * @param f
     * @param datasetId
     * @param embeddingModel
     * @param embeddingStore
     * @return success or not
     */
    private boolean embedFile(File f, String datasetId, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        log.info("embed file {}", f);
        String extension = FilenameUtils.getExtension(f.getName());
        DocumentParser documentParser;
        DocumentSplitter splitter;
        switch (extension) {
            case TYPE_MIND_MAP:
                documentParser = new MindMapDocumentParser();
                // use common doc splitter since the mind map has been converted to Markdown by MindMapDocumentParser;
                splitter = DocumentSplitters.recursive(512, 0);
//                splitter = new MindMapDocumentSplitter(1024, 1024);
                break;
            default:
                documentParser = new TextDocumentParser();
                splitter = DocumentSplitters.recursive(512, 0);
        }

        log.debug("embed file with parser %s and splitter %s".formatted(documentParser.getClass().getSimpleName(), splitter.getClass().getSimpleName()));

        // a docId is a unique identity for an embedded file.
        String docId = this.persistDocumentMetaIfNotExist(datasetId, f.getPath());
        if (StringUtils.isBlank(docId)) {
            log.error("Failed to persist document meta: {}", f.getPath());
            return false;
        }
        try {
            Document document = null;

            document = FileSystemDocumentLoader.loadDocument(f.getPath(), documentParser);

            log.debug("Doc meta: %s".formatted(document.metadata()));
            if (log.isTraceEnabled()) log.trace(StringUtils.abbreviate(document.text(), 256));

            // remove existing embedding first
            embeddingStore.removeAll(new MetadataFilterBuilder("doc_id").isEqualTo(docId));

            // do split and embed.
            List<TextSegment> segments = splitter.split(document);
            segments.forEach(segment -> {
                segment.metadata().put("doc_id", docId);
            });
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            embeddingStore.addAll(embeddings, segments);
            this.updateDocument(docId, segments.size(), true, "");
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.debug(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            this.updateDocument(docId, 0, false, e.getMessage());
            return false;
        }
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

    private void updateDocument(String docId, int blockCount, boolean embedded, String comment) {
        super.withJdbcConnection((Function<Connection, Void>) connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "update mindolph_doc set block_count = ?, embedded = ?, comment = ? where id = ?");
                ps.setInt(1, blockCount);
                ps.setBoolean(2, embedded);
                ps.setString(3, comment);
                ps.setString(4, docId);
                if (ps.executeUpdate() == 0) {
                    throw new RuntimeException("Failed to update document meta");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public List<EmbeddingDocEntity> findDocuments(String datasetId) {
        if (StringUtils.isBlank(datasetId)) return List.of();
        return super.withJdbcConnection(connection -> {
            try {
                List<EmbeddingDocEntity> results = new ArrayList<>();
                String sql = "select * from mindolph_doc where dataset_id = ?";
                log.debug("Executing query: {}", sql);
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, datasetId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    results.add(new EmbeddingDocEntity(rs.getString("id"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getInt("block_count"),
                            rs.getBoolean("embedded"),
                            rs.getString("comment")));
                }
                return results;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<EmbeddingDocEntity> findDocuments(String datasetId, List<File> files) {
        if (CollectionUtils.isEmpty(files)) return List.of();
        return super.withJdbcConnection(connection -> {
            try {
                List<EmbeddingDocEntity> results = new ArrayList<>();
                String params = StringUtils.join(Collections.nCopies(files.size(), "?"), ",");
                String sql = "select * from mindolph_doc where dataset_id = ? and file_path in (%s)".formatted(params);
                log.debug("Executing query: {}", sql);
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, datasetId);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    ps.setString(i + 2, file.getPath());
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    results.add(new EmbeddingDocEntity(rs.getString("id"),
                            rs.getString("file_name"),
                            rs.getString("file_path"),
                            rs.getInt("block_count"),
                            rs.getBoolean("embedded"),
                            rs.getString("comment")));
                }
                log.debug("Found {} embeddings for files {}", results.size(), files.size());
                return results;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Integer deleteDocument(String docId) {
        return super.withJdbcConnection(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "delete from mindolph_doc where id = ?");
                ps.setString(1, docId);
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
