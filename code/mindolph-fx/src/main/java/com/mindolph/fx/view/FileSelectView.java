package com.mindolph.fx.view;

import com.mindolph.base.control.FileTreeHelper;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.genai.rag.EmbeddingDocEntity;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.mfx.util.GlobalExecutor;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckTreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.Comparators.SORTING_TREE_ITEMS;

/**
 * File select view for selecting files to do embedding.
 *
 * @since 1.13.0
 */
public class FileSelectView extends CheckTreeView<NodeData> {

    private static final Logger log = LoggerFactory.getLogger(FileSelectView.class);
    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    private final CheckBoxTreeItem<NodeData> rootItem;

    private boolean excludeFiles = false;
    private boolean expandAllAsDefault = false;
    private FileFilter fileFilter;

    // all files in the tree view (for retrieving embedding status)
    private final List<File> allFiles = new ArrayList<>();

    public FileSelectView() {
        rootItem = new CheckBoxTreeItem<>(new NodeData("Workspace Stub"));
        rootItem.setExpanded(true);
        super.setRoot(rootItem);
        super.setShowRoot(false);
        super.setCellFactory(nodeDataTreeView -> new CheckBoxTreeCell<>() {
            @Override
            public void updateItem(NodeData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    if (item.getFile().isFile()) {
                        String status = StringUtils.isBlank(item.getLabel())?"": "(%s)".formatted(item.getLabel());
                        setText("%s  %s  %s".formatted(item.getName(), FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(item.getFile())), status));
                    }
                    else {
                        setText(item.getName());
                    }
                }
            }
        });
    }

    /**
     * Load all files in the workspace to the tree view asynchronously.
     *
     * @param datasetMeta
     * @param workspaceMeta
     * @param expandAllAsDefault
     * @param excludeFiles
     * @param filter
     */
    public void loadWorkspace(DatasetMeta datasetMeta, WorkspaceMeta workspaceMeta, boolean expandAllAsDefault, boolean excludeFiles, FileFilter filter) {
        this.expandAllAsDefault = expandAllAsDefault;
        this.excludeFiles = excludeFiles;
        this.fileFilter = filter;
        if (log.isDebugEnabled()) {
            log.debug("Load Workspace {}", workspaceMeta.getName());
            if (datasetMeta.getFiles() != null) {
                log.debug("Checked files: %d".formatted(datasetMeta.getFiles().size()));
                datasetMeta.getFiles().forEach(file -> {
                    log.debug("  {}", file);
                });
            }
            log.debug("Option expandAllAsDefault: {}", expandAllAsDefault);
            log.debug("Option ExcludeFiles: {}", excludeFiles);
        }
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            log.debug("Workspace node '%s' is loaded".formatted(workspaceDataTreeItem.getValue()));
            Platform.runLater(() -> {
                super.refresh();
                super.requestFocus();
                this.labelTheCheckedFileWithEmbeddingStatus(datasetMeta, allFiles);
            });
        });
        rootItem.getChildren().clear();
        this.asyncCreateWorkspaceSubTree(workspaceMeta, datasetMeta.getFiles());
    }

    /**
     * Find embedding status from vector data store for all checked files and label the checked tree view items.
     *
     * @param allFiles
     */
    private void labelTheCheckedFileWithEmbeddingStatus(DatasetMeta datasetMeta, List<File> allFiles) {
        GlobalExecutor.submit(() -> {
            try {
                List<EmbeddingDocEntity> embeddingStatues = EmbeddingService.getInstance().findDocuments(datasetMeta.getId(), allFiles);
                // file path -> embedding doc
                Map<String, EmbeddingDocEntity> embeddedMap = embeddingStatues.stream().collect(Collectors.toMap(EmbeddingDocEntity::file_path, e -> e));
                log.debug("Label checked files: %d".formatted(embeddedMap.size()));
                Platform.runLater(() -> {
                    for (File file : allFiles) {
                        if (embeddedMap.containsKey(file.getPath())) {
                            this.findAndUpdateName(file, embeddedMap.get(file.getPath()).embedded() ? "embedded" : "fail");
                        }
                        else {
                            this.findAndUpdateName(file, "never");
                        }
                    }
                    super.refresh();
                    super.requestFocus();
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     *
     */
    public void clearEmbeddingStatusLabels() {
        TreeVisitor.dfsTraverse(rootItem, treeItem -> {
            treeItem.getValue().setLabel(null);
            return true;
        });
        super.refresh();
        super.requestFocus();
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta, List<File> checkedFiles) {
        GlobalExecutor.submit(() -> {
            log.debug("Start a new virtual thread to load workspace: %s".formatted(workspaceMeta.getBaseDirPath()));
            Tree tree = WorkspaceManager.getIns().loadWorkspaceRecursively(workspaceConfig, workspaceMeta);
            Node workspaceNode = tree.getRootNode();

            Platform.runLater(() -> {
                allFiles.clear();
                this.loadTreeNode(workspaceNode, rootItem, checkedFiles);
                log.debug("workspace loaded: %s".formatted(workspaceMeta.getBaseDirPath()));
                EventBus.getIns().notifyWorkspaceLoaded(rootItem);
            });
        });
    }

    /**
     * Load tree data recursively.
     *
     * @param parentNode
     * @param parent
     */
    private void loadTreeNode(Node parentNode, CheckBoxTreeItem<NodeData> parent, List<File> checkedFiles) {
        for (Node childNode : parentNode.getChildren()) {
            NodeData nodeData = (NodeData) childNode.getData();
            if (nodeData.isFolder()) {
                CheckBoxTreeItem<NodeData> folderItem = this.addFolder(parent, nodeData, checkedFiles);
                folderItem.setExpanded(this.expandAllAsDefault);
                this.loadTreeNode(childNode, folderItem, checkedFiles); // recursive
                this.removeFromParentIfEmpty(parent, folderItem); // empty folders are excluded
            }
            else if (nodeData.isFile()) {
                if (!this.excludeFiles) {
                    if (this.fileFilter == null) {
                        this.addFile(parent, nodeData, checkedFiles);
                        allFiles.add(nodeData.getFile());
                    }
                    else {
                        if (this.fileFilter.accept(nodeData.getFile())) {
                            this.addFile(parent, nodeData, checkedFiles);
                            allFiles.add(nodeData.getFile());
                        }
                        else {
                            if (log.isTraceEnabled()) log.trace("Filtered file: %s".formatted(nodeData.getFile()));
                        }
                    }
                }
                else {
                    if (log.isTraceEnabled()) log.trace("Ignore excluded file: %s".formatted(nodeData.getFile()));
                }
            }
            else {
                log.warn("Unknown file type: %s".formatted(nodeData.getFile()));
            }
        }
    }

    public CheckBoxTreeItem<NodeData> addFolder(CheckBoxTreeItem<NodeData> parent, NodeData folderData, List<File> checkedFiles) {
        CheckBoxTreeItem<NodeData> folderItem = new CheckBoxTreeItem<>(folderData);
        parent.getChildren().add(folderItem);
        if (log.isTraceEnabled()) log.trace("  add folder: %s".formatted(folderData.getFile()));
        if (checkedFiles != null && checkedFiles.contains(folderData.getFile())) {
            folderItem.setSelected(true);
            folderItem.setIndeterminate(true);
            super.getCheckModel().check(folderItem);
        }
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return folderItem;
    }

    public CheckBoxTreeItem<NodeData> addFileAndSelect(CheckBoxTreeItem<NodeData> parent, NodeData fileData, List<File> checkedFiles) {
        CheckBoxTreeItem<NodeData> treeItem = this.addFile(parent, fileData, checkedFiles);
        super.getSelectionModel().select(treeItem);
        return treeItem;
    }

    public CheckBoxTreeItem<NodeData> addFile(CheckBoxTreeItem<NodeData> parent, NodeData fileData, List<File> checkedFiles) {
        CheckBoxTreeItem<NodeData> fileItem = new CheckBoxTreeItem<>(fileData);
        fileItem.setExpanded(this.expandAllAsDefault);
        if (log.isTraceEnabled()) log.trace("  add file: %s".formatted(fileData.getFile()));
        if (checkedFiles != null && checkedFiles.contains(fileData.getFile())) {
            fileItem.setSelected(true);
            super.getCheckModel().check(fileItem); // TODO TBD
        }
        parent.getChildren().add(fileItem);
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return fileItem;
    }

    private void removeFromParentIfEmpty(CheckBoxTreeItem<NodeData> parent, CheckBoxTreeItem<NodeData> item) {
        if (item.getChildren().isEmpty()) {
            parent.getChildren().remove(item);
        }
    }

    public void findAndUpdateName(File file, String label) {
        try {
            FileTreeHelper.findAndUpdateLabel(rootItem, file, nodeData -> label);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    public CheckBoxTreeItem<NodeData> getRootItem() {
        return rootItem;
    }
}
