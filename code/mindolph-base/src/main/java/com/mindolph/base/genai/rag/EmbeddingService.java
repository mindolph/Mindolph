package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.Stage;
import com.mindolph.base.genai.event.AiEventBus;
import com.mindolph.base.genai.event.DoneEvent;
import com.mindolph.base.genai.event.ProgressEvent;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.mfx.util.GlobalExecutor;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;
import static com.mindolph.core.constant.SupportFileTypes.TYPE_MIND_MAP;

/**
 * @since 1.13.0
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
     * Unembed documents all files in dataset.
     *
     * @param datasetMeta
     * @return true means all documents have been deleted.
     */
    public CompletableFuture<Boolean> unembedDataset(DatasetMeta datasetMeta) {
        return GlobalExecutor.submitCompletable(() -> {
            try {
                this.initDatabaseIfNotExist(); // check the database before starting embedding.
                this.loadEmbeddingStoreIfNotExist(datasetMeta);
                List<EmbeddingDocEntity> documents = this.findDocuments(datasetMeta.getId());
                if (embeddingStore == null) {
                    log.warn("EmbeddingStore is not built");
                    AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.REMOVE_DATASET, "Embedding store is not built", false));
                    return false;
                }
                else {
                    log.info("Try to unembed %d documents...".formatted(documents.size()));
                    ProgressEvent progressEvent = new ProgressEvent(Stage.REMOVE_DATASET, "Removing embedded document...%.1f%%".formatted(0f), null, 0, 0f);
                    AiEventBus.getInstance().emitEvent(progressEvent);

                    Integer successCount = this.unembedFiles(documents, event -> {
                        event.setStage(Stage.REMOVE_DATASET);
                        AiEventBus.getInstance().emitEvent(event);
                    });
                    if (successCount == documents.size()) {
                        AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.REMOVE_DATASET, "Un-embedding done with %d successes of %d files.".formatted(successCount, documents.size()), true));
                    }
                    else {
                        AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.REMOVE_DATASET, "Un-embedding fail with %d successes of %d files.".formatted(successCount, documents.size()), false));
                    }
                    return successCount == documents.size();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.REMOVE_DATASET, e.getLocalizedMessage(), false));
                throw new RuntimeException(e);
            }
        });
    }

    /**
     *
     * @param datasetMeta
     * @param filter filter documents to be un-embedded
     * @return
     */
    public CompletableFuture<Boolean> embedDataset(DatasetMeta datasetMeta, Predicate<EmbeddingDocEntity> filter) {
        if (datasetMeta.getFiles() == null || datasetMeta.getFiles().isEmpty()) {
            log.warn("No files to be embedded");
            throw new RuntimeException("No files to be embedded");
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
        return GlobalExecutor.submitCompletable(() -> {
            try {
                this.initDatabaseIfNotExist(); // check the database before starting embedding.
                this.loadEmbeddingStoreIfNotExist(datasetMeta);
                List<EmbeddingDocEntity> documents = this.findDocuments(datasetMeta.getId());
                List<EmbeddingDocEntity> toBeUnembedded = documents.stream().filter(filter).toList();
                if (toBeUnembedded.isEmpty()) {
                    // Keep doing embedding
                    AiEventBus.getInstance().emitEvent(new ProgressEvent(Stage.EMBED_DATASET, "No embedded data needs to be deleted.", false));
                }
                AiEventBus.getInstance().emitEvent(new ProgressEvent(Stage.EMBED_DATASET, "Begin embedding.", false));
                if (embeddingStore == null) {
                    log.warn("EmbeddingStore is not built");
                    AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.EMBED_DATASET, "Embedding store is not built"));
                    return false;
                }
                else {
                    log.info("Try to unembed before embedding...");
                    Integer successCount = this.unembedFiles(toBeUnembedded, event -> {
                        event.setStage(Stage.EMBED_DATASET);
                        AiEventBus.getInstance().emitEvent(event);
                    });

                    if (successCount != toBeUnembedded.size()) {
                        log.warn("Unembed fail with %d documents deleted".formatted(successCount));
                        return false;
                    }
                    log.info("Start to embed files (%d): %s".formatted(datasetMeta.getFiles().size(), StringUtils.join(datasetMeta.getFiles(), ", ")));
                    AiEventBus.getInstance().emitEvent(new ProgressEvent(Stage.EMBED_DATASET, "Embedding...%.1f%%".formatted(0f), true, null, 0, 0f));
                    return this.embedDataset(datasetMeta);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.EMBED_DATASET, e.getLocalizedMessage()));
                throw new RuntimeException(e);
            }
        });
    }

    // unembed files, which means delete the embedding data for those documents.
    private Integer unembedFiles(List<EmbeddingDocEntity> toBeEmbedded, Consumer<ProgressEvent> onProgress) {
        int deletedCount = 0;
        if (!toBeEmbedded.isEmpty()) {
            log.info("%d files to be unembedded".formatted(toBeEmbedded.size()));
            for (EmbeddingDocEntity docEntity : toBeEmbedded) {
                log.debug("Unembed doc: %s - %s".formatted(docEntity.id(), docEntity.file_path()));
                embeddingStore.removeAll(new IsEqualTo("doc_id", docEntity.id()));
                deletedCount += this.deleteDocument(docEntity.id());
                // all the embedded docs must be deleted, so the count = index + 1
                BigDecimal ratio = BigDecimal.valueOf(deletedCount).divide(new BigDecimal(toBeEmbedded.size()), 3, RoundingMode.HALF_UP);
                Double percent = NumberFormatUtils.toPercent(ratio.doubleValue(), 1);

                ProgressEvent progressEvent = new ProgressEvent(null, "Removing embedded document...%.1f%%".formatted(percent), new File(docEntity.file_path()), deletedCount, ratio.floatValue());
                onProgress.accept(progressEvent);
            }
        }
        else {
            log.info("No documents to be unembed.");
        }
//        DoneEvent doneEvent = new DoneEvent(Stage.UNEMBEDDING, "Unembedding is done");
        return deletedCount;
    }

    /**
     * Embed all files in the dataset.
     *
     * @param datasetMeta
     * @return
     */
    private boolean embedDataset(DatasetMeta datasetMeta) {
        List<File> selectedFiles = datasetMeta.getFiles();
        int total = selectedFiles.size();
        int successCount = 0;
        try {

            for (int i = 0; i < selectedFiles.size(); i++) {
                File file = selectedFiles.get(i);
                if (datasetMeta.isStop()) {
                    log.info("Embedding is stopped by user");
                    AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.EMBED_DATASET, "Embedding is stopped", false));
                    break;
                }
                BigDecimal ratio = BigDecimal.valueOf(i + 1).divide(new BigDecimal(total), 3, RoundingMode.HALF_UP);
                boolean success = false;
                if (file.isDirectory()) {
                    // NOTE: the directory is not included in the dataset yet, so the handling doesn't affect for now.
                    for (File f : FileUtils.listFiles(file, SUPPORTED_EMBEDDING_FILE_TYPES, true)) {
                        if (this.embedFile(f, datasetMeta.getId(), embeddingModel)) {
                            successCount++;
                            success = true;
                        }
                    }
                }
                else if (file.isFile()) {
                    if (this.embedFile(file, datasetMeta.getId(), embeddingModel)) {
                        successCount++;
                        success = true;
                    }
                }
                Double percent = NumberFormatUtils.toPercent(ratio.doubleValue(), 1);
                AiEventBus.getInstance().emitEvent(new ProgressEvent(Stage.EMBED_DATASET, "Embedding...%.1f%%".formatted(percent), success, file, successCount, ratio.floatValue()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.EMBED_DATASET, "Failed: %s".formatted(e.getMessage()), false));
            return false;
        }
//            if (!datasetMeta.isStop()) {
        AiEventBus.getInstance().emitEvent(new DoneEvent(Stage.EMBED_DATASET, "Embedding done with %d successes of %d files.".formatted(successCount, total)));
//            }
        return true;
    }

    public void loadEmbeddingStoreIfNotExist(DatasetMeta datasetMeta) {
        if (embeddingModel == null) {
            log.debug("Create embedding model instance");
            embeddingModel = super.createEmbeddingModel(
                    datasetMeta.getLanguageCode(), datasetMeta.getEmbeddingModel());
        }
        if (embeddingStore == null) {
            log.debug("Create embedding store instance");
            embeddingStore = super.createEmbeddingStore(embeddingModel, true, false);
        }
    }

    public Boolean testTableExistence() {
        return this.withJdbcConnection((conn) -> {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM information_schema.tables  WHERE table_schema = ? AND table_name = ?")) {
                pstmt.setString(1, "public");
                pstmt.setString(2, "mindolph_doc");

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
            return false;
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


    /**
     * Embed a file with specified embedding model and save to target embedding store,
     *
     * @param f
     * @param datasetId
     * @param embeddingModel
     * @return success or not
     */
    private boolean embedFile(File f, String datasetId, EmbeddingModel embeddingModel) {
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

        log.info("embed file with parser %s and splitter %s".formatted(documentParser.getClass().getSimpleName(), splitter.getClass().getSimpleName()));

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
                throw new RuntimeException(e.getLocalizedMessage(), e);
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

    public int countEmbeddedDocuments(String datasetId) {
        if (StringUtils.isBlank(datasetId)) return 0;
        return super.withJdbcConnection(connection -> {
            try {
                String sql = "select count(id) from mindolph_doc where dataset_id = ? and embedded = true";
                log.debug("Executing query: {}", sql);
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, datasetId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (Exception e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
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
                throw new RuntimeException(e.getLocalizedMessage(), e);
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
