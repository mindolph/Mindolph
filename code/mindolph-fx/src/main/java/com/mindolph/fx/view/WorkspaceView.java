package com.mindolph.fx.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.control.FileTreeHelper;
import com.mindolph.base.control.MTreeView;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.*;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.constant.NodeType;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchService;
import com.mindolph.fx.helper.TreeExpandRestoreListener;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.GlobalExecutor;
import com.mindolph.mindmap.search.MindMapTextMatcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mindolph.core.constant.SupportFileTypes.TYPE_MIND_MAP;

/**
 * Load folders and files(with filters) in them lazily.
 * Open file by double-clicking.
 * Dra and drop single folder/file to another folder.
 * Context menu: rename, clone delete, open in system, find in files, new folder/mmd/plantuml/md/txt.
 *
 * @author mindolph.com@gmail.com
 * @deprecated kept for possible using in future
 */
public class WorkspaceView extends BaseView implements EventHandler<ActionEvent>,
        TreeExpandRestoreListener {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceView.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    private final Comparator<TreeItem<NodeData>> SORTING_TREE_ITEMS = (o1, o2) -> {
        File f1 = o1.getValue().getFile();
        File f2 = o2.getValue().getFile();
        if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
            return f1.getName().compareTo(f2.getName());
        }
        else if (f1.isDirectory()) {
            return -1;
        }
        else {
            return 1;
        }
    };

    @FXML
    private MTreeView<NodeData> treeView;

    private final TreeItem<NodeData> rootItem; // root node is not visible
    private List<String> expendedFileList; // set by event listener, used for tree expansion restore.

    private ExpandEventHandler expandEventHandler;
    private CollapseEventHandler collapseEventHandler;
    private FileChangedEventHandler fileChangedEventHandler;

    public WorkspaceView() {
        super("/view/workspace_view.fxml");
        log.info("Init workspace view");
        rootItem = new TreeItem<>(new NodeData("Hidden Root"));
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        treeView.setOnKeyPressed(event -> {
            log.debug("key pressed: " + event.getCode());
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFile();
                event.consume();
            }
        });

        treeView.setCellFactory(treeView -> {
            WorkspaceViewCell cell = new WorkspaceViewCell();
            // handle double-click to open file
            cell.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    this.openSelectedFile();
                }
            });
            cell.setOnMouseEntered(event -> {
                TreeItem<NodeData> treeItem = cell.getTreeItem();
                if (treeItem != null) {
                    log.trace("Install tooltip for " + treeItem);
                    NodeData data = treeItem.getValue();
                    cell.setTooltip(new Tooltip(data.getFile().getPath()));
                }
                else {
                    log.trace("Not tree item");
                    cell.setTooltip(null);
                }
            });
            cell.setDragFileEventHandler((files, target) -> {
                File toDir = target.getFile();
                if (!toDir.isDirectory() || !toDir.exists()) {
                    log.warn("Target dir doesn't exist: %s".formatted(toDir.getPath()));
                    return;
                }
                log.debug("Drop %d files to %s".formatted(files.size(), toDir.getPath()));
                for (NodeData fileData : files) {
                    if (fileData.isFile()) {
                        fileChangedEventHandler.onFileChanged(fileData);
                    }
                    File draggingFile = fileData.getFile();
                    // update the tree
                    TreeItem<NodeData> treeItemFile = FileTreeHelper.findTreeItemByFile(rootItem, draggingFile);
                    TreeItem<NodeData> treeItemFolder = FileTreeHelper.findTreeItemByFile(rootItem, toDir);
                    if (treeItemFile == null || treeItemFolder == null
                            || treeItemFile == treeItemFolder
                            || treeItemFile.getParent() == treeItemFolder) {
                        log.debug("Nothing to do");
                    }
                    else {
                        log.debug("Move tree item %s to %s".formatted(treeItemFile.getValue(), treeItemFolder.getValue()));
                        treeItemFile.getParent().getChildren().remove(treeItemFile);
                        treeItemFolder.getChildren().add(treeItemFile);

                        File newFilePath = new File(toDir, fileData.getName());
                        try {
                            if (fileData.isFile()) {
                                FileUtils.moveFile(draggingFile, newFilePath);
                                log.debug("File %s is moved".formatted(draggingFile.getName()));
                            }
                            else if (fileData.isFolder()) {
                                FileUtils.moveDirectory(draggingFile, newFilePath);
                                log.debug("Folder %s is moved".formatted(draggingFile.getName()));
                            }
                            fileData.setFile(newFilePath);
                            FXCollections.sort(treeItemFolder.getChildren(), SORTING_TREE_ITEMS);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException("Move file failed");
                        }
                    }
                }
                treeView.refresh();
            });
            return cell;
        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("Selection changed: " + newValue);
            Optional<NodeData> selectedValue = treeView.getSelectedData();
            EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.NEW_FILE,
                    selectedValue.isPresent()
                            && !selectedValue.get().isFile());
        });
        SearchService.getIns().registerMatcher(TYPE_MIND_MAP, new MindMapTextMatcher(true));
    }


    public void loadWorkspaces(WorkspaceList workspaceList) {
        AtomicInteger count = new AtomicInteger();
        EventBus.getIns().subscribeWorkspaceLoaded(workspaceList.getSize(), workspaceDataTreeItem -> {
            rootItem.getChildren().add(workspaceDataTreeItem);
            log.debug("Add workspace node (%d) '%s' to root".formatted(count.get(), workspaceDataTreeItem.getValue()));
            if (count.incrementAndGet() == workspaceList.getSize()) {
                log.debug("All workspaces are restored");
                EventBus.getIns().notifyWorkspacesRestored(); // notify that all workspaces loaded.
            }
            Platform.runLater(() -> treeView.requestFocus());
        });
        GlobalExecutor.submit(() -> {
            log.debug("start a new thread to load workspaces: %d".formatted(workspaceList.getSize()));
            for (WorkspaceMeta workspaceMeta : workspaceList.getProjects().stream().filter(workspaceMeta -> new File(workspaceMeta.getBaseDirPath()).exists()).toList()) {
                createWorkspaceSubTree(workspaceMeta);
            }
        });
    }

    /**
     * Load a workspace sub-tree.
     *
     * @param workspaceMeta
     */
    public void loadWorkspace(WorkspaceMeta workspaceMeta) {
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            rootItem.getChildren().add(workspaceDataTreeItem);
            log.debug("Add workspace node '%s' to root".formatted(workspaceDataTreeItem.getValue()));
            Platform.runLater(() -> {
                treeView.requestFocus();
            });
        });
        asyncCreateWorkspaceSubTree(workspaceMeta);
    }

    /**
     * Reload workspace sub-tree to specified position of tree.
     *
     * @param workspaceMeta
     * @param index         original index in siblings.
     */
    public void reloadWorkspace(WorkspaceMeta workspaceMeta, int index) {
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            rootItem.getChildren().add(index, workspaceDataTreeItem);
            List<String> treeExpandedList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
            onTreeExpandRestore(treeExpandedList);
            treeView.getSelectionModel().select(workspaceDataTreeItem);
            Platform.runLater(() -> treeView.requestFocus());
        });
        asyncCreateWorkspaceSubTree(workspaceMeta);
    }

    /**
     * Reload folder and it's sub-tree to specified position of tree.
     *
     * @param folderData
     */
    public void reloadFolder(NodeData folderData) {
        log.debug("reload folder: %s".formatted(folderData.getFile()));
        List<NodeData> childrenData = WorkspaceManager.getIns().loadFolder(folderData, workspaceConfig);
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        this.loadTreeNode(selectedTreeItem, childrenData);
        treeView.getSelectionModel().select(selectedTreeItem);
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        GlobalExecutor.submit(() -> {
            createWorkspaceSubTree(workspaceMeta);
        });
    }

    private void createWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        NodeData workspaceData = new NodeData(NodeType.WORKSPACE, new File(workspaceMeta.getBaseDirPath()));
        workspaceData.setWorkspaceData(workspaceData);
        List<NodeData> childrenData = WorkspaceManager.getIns().loadWorkspace(workspaceData, workspaceConfig);
        TreeItem<NodeData> workspaceItem = new TreeItem<>(workspaceData);
        workspaceItem.expandedProperty().addListener((observable, oldExpanded, newExpanded) -> {
            if (!oldExpanded.equals(newExpanded)) {
                onTreeItemExpandOrCollapsed(newExpanded, workspaceItem);
            }
        });
        Platform.runLater(() -> {
            this.loadTreeNode(workspaceItem, childrenData);
            log.debug("workspace loaded: " + workspaceMeta.getBaseDirPath());
            EventBus.getIns().notifyWorkspaceLoaded(workspaceItem);
        });
    }

    private void loadTreeNode(TreeItem<NodeData> parent, List<NodeData> childrenData) {
        // note: use name other than file path to match tree item and node data, because the file path changes when folder name changes.
        log.debug("load folder item: %s/".formatted(parent));
        // remove not exists tree items for theirs file might be deleted.
        boolean isRemoved = parent.getChildren().removeIf(nodeDataTreeItem -> {
            return childrenData.stream().noneMatch(nodeData -> {
                return nodeDataTreeItem.getValue().getName().equals(nodeData.getName());
            });
        });
        if (isRemoved) log.debug("Some tree items are moved");

        for (NodeData childNodeData : childrenData) {
            // already exists
            Optional<TreeItem<NodeData>> existing = parent.getChildren().stream().filter(nodeDataTreeItem -> childNodeData.getName().equals(nodeDataTreeItem.getValue().getName())).findFirst();
            if (existing.isPresent()) {
                log.debug("already exists, ignore: %s".formatted(childNodeData));
                TreeItem<NodeData> existingItem = existing.get();
                log.debug("existing tree item: %s, expanded: %s".formatted(existingItem.getValue().getFile(), existingItem.isExpanded()));
                existingItem.setValue(childNodeData);
                if (childNodeData.isFolder() && existingItem.isExpanded()) {
                    loadTreeNode(existingItem, WorkspaceManager.getIns().loadFolder(childNodeData, workspaceConfig));
                }
                continue;
            }
            log.debug(childNodeData + " does not existed, try to create it.");
            if (childNodeData.isFolder()) {
                TreeItem<NodeData> folderItem = this.addFolder(parent, childNodeData);
                log.debug("add folder: %s/".formatted(folderItem.getValue().getName()));
            }
            else if (childNodeData.isFile()) {
                TreeItem<NodeData> fileItem = this.addFile(parent, childNodeData);
                log.debug("add file: %s".formatted(fileItem.getValue().getName()));
            }
        }
    }

    public TreeItem<NodeData> addFolder(TreeItem<NodeData> parent, NodeData folderData) {
        TreeItem<NodeData> folderItem = new TreeItem<>(folderData);
        folderItem.expandedProperty().addListener((observable, oldValue, newValue) -> onTreeItemExpandOrCollapsed(newValue, folderItem));
        parent.getChildren().add(folderItem);
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return folderItem;
    }

    public TreeItem<NodeData> addFileAndSelect(TreeItem<NodeData> parent, NodeData fileData) {
        TreeItem<NodeData> treeItem = this.addFile(parent, fileData);
        treeView.getSelectionModel().select(treeItem);
        return treeItem;
    }

    public TreeItem<NodeData> addFile(TreeItem<NodeData> parent, NodeData fileData) {
        TreeItem<NodeData> fileItem = new TreeItem<>(fileData);
        if (parent == null) {
            log.warn("This file doesn't belong to any workspace or folder.");
        }
        else {
            if (parent.getChildren().stream().anyMatch(nodeDataTreeItem -> nodeDataTreeItem.getValue().equals(fileData))) {
                return null; // already exists, TBD consider return the existing tree item
            }
            parent.getChildren().add(fileItem);
            FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        }
        return fileItem;
    }

    private void openSelectedFile() {
        Optional<NodeData> selectedValue = treeView.getSelectedData();
        if (selectedValue.isPresent()) {
            NodeData nodeObject = selectedValue.get();
            if (nodeObject.isFile()) {
                if (!nodeObject.getFile().exists()) {
                    DialogFactory.errDialog("File doesn't exist, it might be deleted or moved externally.");
                    removeTreeNode(nodeObject);
                    EventBus.getIns().notifyDeletedFile(nodeObject);
                    return;
                }
                log.info("Open file: " + nodeObject.getFile());
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(nodeObject.getFile()));
            }
        }
    }

    @Override
    public void onTreeExpandRestore(List<String> expandedNodes) {
        log.info("Restore tree expansion: " + StringUtils.join(expandedNodes, ", "));
        this.expandTreeNodes(expandedNodes);
    }

    /**
     * Find and select a tree item by it's node data and expand it's path nodes.
     *
     * @param nodeData
     */
    public void selectByNodeData(NodeData nodeData) {
        if (nodeData != null) {
            log.debug("Select in tree: " + nodeData);
            TreeVisitor.dfsTraverse(rootItem, treeItem -> {
                NodeData curNodeData = treeItem.getValue();
                if (!treeItem.isLeaf() && curNodeData.isParentOf(nodeData)) {
                    treeItem.setExpanded(true);
                }
                if (curNodeData.getFile().equals(nodeData.getFile())) {
                    log.debug("Found tree item to select");
                    treeItem.setExpanded(true);
                    treeView.getSelectionModel().select(treeItem);
                    return false;
                }
                return true;
            });
        }
    }

    public void scrollToSelected() {
        log.debug("Scroll to selected tree item");
        treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
        treeView.refresh();
    }

    /**
     * Expand specified nodes in this workspace tree.
     *
     * @param expendedFileList
     */
    public void expandTreeNodes(List<String> expendedFileList) {
        this.expendedFileList = expendedFileList;
        TreeVisitor.dfsTraverse(rootItem, treeItem -> {
            // excludes the nodes whose parent is collapsed.
            if (treeItem.getParent() != null && !treeItem.getParent().isExpanded()) {
                return null;
            }
            // excludes files
            if (treeItem.getValue().isFile()) {
                return null;
            }
            NodeData nodeData = treeItem.getValue();
            if (expendedFileList.contains(nodeData.getFile().getPath())) {
                treeItem.setExpanded(true);
            }
            return null;
        });
    }

    /**
     * Collapse node and all it's sub nodes.
     *
     * @param treeItem
     */
    public void collapseTreeNodes(TreeItem<NodeData> treeItem) {
        log.debug("Collapse all expanded nodes under " + treeItem);
        TreeVisitor.dfsTraverse(treeItem, item -> {
            if (item.isExpanded()) {
                log.debug("Expand node: " + item);
                item.setExpanded(false);
            }
            return null;
        });
        treeItem.setExpanded(false);
    }

    /**
     * Handle tree node expansion and collapse and call outer listener.
     *
     * @param expanded
     * @param treeItem
     */
    private void onTreeItemExpandOrCollapsed(Boolean expanded, TreeItem<NodeData> treeItem) {
        if (expanded) {
            expandEventHandler.onTreeItemExpanded(treeItem);
            // if expanded, pre-load all children of each child of this tree item.
            for (TreeItem<NodeData> child : treeItem.getChildren()) {
                File childFile = child.getValue().getFile();
                if (childFile.isDirectory()) {
                    List<NodeData> childrenOfChild = WorkspaceManager.getIns().loadFolder(child.getValue(), workspaceConfig);
                    this.loadTreeNode(child, childrenOfChild);
                    // expand the child node if it should be restored to expanded.
                    if (expendedFileList != null && expendedFileList.contains(childFile.getPath())) {
                        child.setExpanded(true);
                    }
                }
            }
        }
        else {
            collapseEventHandler.onTreeItemCollapsed(treeItem);
        }
    }

    /**
     * Handle events on node of the tree view.
     *
     * @param event
     */
    @Override
    public void handle(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        NodeData selectedData = selectedTreeItem.getValue();

    }


    /**
     * Get the workspace node for selected node.
     *
     * @return
     * @deprecated not using but keep it
     */
    public TreeItem<NodeData> getSelectedWorkspace() {
        TreeItem<NodeData> selectedItem = treeView.getSelectionModel().getSelectedItem();
        return findParentNodeWithDataType(selectedItem, NodeType.WORKSPACE);
    }

    /**
     * Find first parent TreeItem matches class type of data object for {@code treeItem} recursively.
     *
     * @param treeItem
     * @param nodeType data type to match
     * @return
     * @deprecated not using but keep it
     */
    private TreeItem<NodeData> findParentNodeWithDataType(TreeItem<NodeData> treeItem, NodeType nodeType) {
        if (treeItem.getValue().getNodeType() == nodeType) {
            return treeItem;
        }
        if (treeItem.getParent().getValue().getNodeType() == nodeType) {
            return treeItem.getParent();
        }
        else {
            return findParentNodeWithDataType(treeItem.getParent(), nodeType);
        }
    }

    public void removeTreeNode(NodeData nodeData) {
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        if (selectedTreeItem.getValue() == nodeData) {
            selectedTreeItem.getParent().getChildren().remove(selectedTreeItem);
            treeView.refresh();
        }
    }

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

    public void setExpandEventHandler(ExpandEventHandler expandEventHandler) {
        this.expandEventHandler = expandEventHandler;
    }

    public void setCollapseEventHandler(CollapseEventHandler collapseEventHandler) {
        this.collapseEventHandler = collapseEventHandler;
    }

    public void setFileChangedEventHandler(FileChangedEventHandler fileChangedEventHandler) {
        this.fileChangedEventHandler = fileChangedEventHandler;
    }
}
