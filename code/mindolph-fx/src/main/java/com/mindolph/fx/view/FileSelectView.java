package com.mindolph.fx.view;

import com.mindolph.base.control.FileTreeHelper;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.genai.rag.EmbeddingDocEntity;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.async.GlobalExecutor;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBoxTreeItem;
import org.controlsfx.control.CheckTreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.Comparators.SORTING_TREE_ITEMS;

/**
 * File select view for selecting files to do embedding.
 *
 * @since unknown
 */
public class FileSelectView extends CheckTreeView<NodeData> {

    private static final Logger log = LoggerFactory.getLogger(FileSelectView.class);
    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    private final CheckBoxTreeItem<NodeData> rootItem;

    private boolean excludeFiles = false;
    private boolean expandAllAsDefault = false;
    private FileFilter fileFilter;

    public FileSelectView() {
        rootItem = new CheckBoxTreeItem<>(new NodeData("Workspace Stub"));
        rootItem.setExpanded(true);
        super.setRoot(rootItem);
        super.setShowRoot(false);
    }

    /**
     * Load all files in the workspace to the tree view asynchronously.
     *
     * @param workspaceMeta
     * @param checkedFiles
     * @param expandAllAsDefault
     * @param excludeFiles
     * @param filter
     */
    public void loadWorkspace(WorkspaceMeta workspaceMeta, List<File> checkedFiles, boolean expandAllAsDefault, boolean excludeFiles, FileFilter filter) {
        this.expandAllAsDefault = expandAllAsDefault;
        this.excludeFiles = excludeFiles;
        this.fileFilter = filter;
        if (log.isDebugEnabled()) {
            log.debug("Load Workspace {}", workspaceMeta);
            log.debug("Checked files: %d".formatted(checkedFiles.size()));
            checkedFiles.forEach(file -> {
                log.debug("Checked file: {}", file);
            });
            log.debug("Option expandAllAsDefault: {}", expandAllAsDefault);
            log.debug("Option ExcludeFiles: {}", excludeFiles);
        }
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            log.debug("Workspace node '%s' is loaded".formatted(workspaceDataTreeItem.getValue()));
            Platform.runLater(() -> {
                super.refresh();
                super.requestFocus();
                this.labelTheCheckedFileWithEmbeddingStatus(checkedFiles);
            });
        });
        rootItem.getChildren().clear();
        this.asyncCreateWorkspaceSubTree(workspaceMeta, checkedFiles);
    }

    /**
     * Find embedding status from vector data store for all checked files and label the checked tree view items.
     *
     * @param checkedFiles
     */
    private void labelTheCheckedFileWithEmbeddingStatus(List<File> checkedFiles) {
        GlobalExecutor.submit(() -> {
            List<EmbeddingDocEntity> embeddingStatues = EmbeddingService.getInstance().findEmbeddingStatues(checkedFiles);
            Map<String, EmbeddingDocEntity> fileEntityMap = embeddingStatues.stream().collect(Collectors.toMap(EmbeddingDocEntity::file_path, e -> e));
            Platform.runLater(() -> {
                for (File file : checkedFiles) {
                    if (fileEntityMap.containsKey(file.getPath())) {
                        this.findAndUpdateName(file, fileEntityMap.get(file.getPath()).embedded() ? "embedded" : "fail");
                    }
                    else {
                        this.findAndUpdateName(file, "never");
                    }
                }
                super.refresh();
                super.requestFocus();
            });
        });
    }

    /**
     *
     */
    public void clearEmbeddingStatusLabels() {
        TreeVisitor.dfsTraverse(rootItem, treeItem -> {
            treeItem.getValue().setFormatted(null);
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
//                rootItem = new CheckBoxTreeItem<>((NodeData) workspaceNode.getData());
                this.loadTreeNode(workspaceNode, rootItem, checkedFiles);
//                super.setRoot(rootItem);
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
                    }
                    else {
                        if (this.fileFilter.accept(nodeData.getFile())) {
                            this.addFile(parent, nodeData, checkedFiles);
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
        FileTreeHelper.findAndUpdateName(rootItem, file, nodeData -> "%s (%s)".formatted(nodeData.getName(), label));
    }

    public CheckBoxTreeItem<NodeData> getRootItem() {
        return rootItem;
    }
}
