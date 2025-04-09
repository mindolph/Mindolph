package com.mindolph.fx.view;

import com.mindolph.base.event.EventBus;
import com.mindolph.core.WorkspaceManager;
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

import static com.mindolph.base.constant.Comparators.SORTING_TREE_ITEMS;

/**
 * @since unknown
 */
public class FileSelectView extends CheckTreeView<NodeData> {

    private static final Logger log = LoggerFactory.getLogger(FileSelectView.class);
    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    private CheckBoxTreeItem<NodeData> rootItem;

    private boolean excludeFiles = false;
    private boolean expandAllAsDefault = false;
    private FileFilter fileFilter;

    public FileSelectView() {
        rootItem = new CheckBoxTreeItem<>(new NodeData("Workspace Stub"));
        rootItem.setExpanded(true);
        super.setRoot(rootItem);
        super.setShowRoot(false);
    }

    public void loadWorkspace(WorkspaceMeta workspaceMeta, List<File> checkedFiles, boolean expandAllAsDefault, boolean excludeFiles, FileFilter filter) {
        this.expandAllAsDefault = expandAllAsDefault;
        this.excludeFiles = excludeFiles;
        this.fileFilter = filter;
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            log.debug("Workspace node '%s' is loaded".formatted(workspaceDataTreeItem.getValue()));
            Platform.runLater(() -> {
                super.refresh();
                super.requestFocus();
            });
        });
        rootItem.getChildren().clear();
        asyncCreateWorkspaceSubTree(workspaceMeta, checkedFiles);
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta, List<File> checkedFiles) {
        new Thread(() -> {
            log.debug("start a new thread to load workspace: %s".formatted(workspaceMeta.getBaseDirPath()));
            Tree tree = WorkspaceManager.getIns().loadWorkspaceRecursively(workspaceConfig, workspaceMeta);
            Node workspaceNode = tree.getRootNode();

            Platform.runLater(() -> {
//                rootItem = new CheckBoxTreeItem<>((NodeData) workspaceNode.getData());
                this.loadTreeNode(workspaceNode, rootItem, checkedFiles);
//                super.setRoot(rootItem);
                log.debug("workspace loaded: %s".formatted(workspaceMeta.getBaseDirPath()));
                EventBus.getIns().notifyWorkspaceLoaded(rootItem);
            });
        }, "Workspace Load Thread").start();
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
                CheckBoxTreeItem<NodeData> folderItem = this.addFolder(parent, nodeData);
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
                    }
                }
            }
        }
    }

    public CheckBoxTreeItem<NodeData> addFolder(CheckBoxTreeItem<NodeData> parent, NodeData folderData) {
        CheckBoxTreeItem<NodeData> folderItem = new CheckBoxTreeItem<>(folderData);
        parent.getChildren().add(folderItem);
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
        if (checkedFiles != null && checkedFiles.contains(fileData.getFile())) {
            fileItem.setSelected(true);
            super.getCheckModel().check(fileItem); // TODO TBD
        }
        parent.getChildren().add(fileItem);
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return fileItem;
    }

    public void removeFromParentIfEmpty(CheckBoxTreeItem<NodeData> parent, CheckBoxTreeItem<NodeData> item) {
        if (item.getChildren().isEmpty()) {
            parent.getChildren().remove(item);
        }
    }

    public CheckBoxTreeItem<NodeData> getRootItem() {
        return rootItem;
    }
}
