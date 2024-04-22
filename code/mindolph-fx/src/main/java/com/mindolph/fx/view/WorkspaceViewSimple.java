package com.mindolph.fx.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.control.MTreeView;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mindolph.base.constant.Comparators.SORTING_TREE_ITEMS;

/**
 * The implementation loads all files(recursively) for each workspace at a time.
 *
 * @author mindolph.com@gmail.com
 * @since 1.8 from the old WorkspaceView
 */
public class WorkspaceViewSimple extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceViewSimple.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    private boolean excludeFiles = false;
    private boolean expandAllAsDefault = false;

    @FXML
    private MTreeView<NodeData> treeView;
    private TreeItem<NodeData> rootItem; // root node is not visible

    private final EventSource<NodeData> selectedEventSource = new EventSource<>();

    public void subscribeSelected(Consumer<NodeData> consumer) {
        selectedEventSource.subscribe(consumer);
    }

    public WorkspaceViewSimple() {
        super("/view/workspace_view.fxml");
        log.info("Init workspace view");
        rootItem = new TreeItem<>(new NodeData("Workspace Stub"));
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        treeView.setOnKeyPressed(event -> {
            log.debug("key pressed: " + event.getCode());
            if (event.getCode() == KeyCode.ENTER) {
                Optional<NodeData> selectedData = treeView.getSelectedData();
                selectedData.ifPresent(selectedEventSource::push);
                event.consume();
            }
        });


        treeView.setCellFactory(tv -> {
            WorkspaceViewCell cell = new WorkspaceViewCell();
            // handle double-click to select target folder.
            cell.setOnMouseClicked(mouseEvent -> {
                Optional<NodeData> selectedData = treeView.getSelectedData();
                selectedData.ifPresent(selectedEventSource::push);
//                if (mouseEvent.getClickCount() == 2) {
//                }
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
            return cell;
        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("Selection changed: " + newValue);
        });
    }

    /**
     * Load a workspace sub-tree to the end of tree.
     *
     * @param workspaceMeta
     * @param expandAllAsDefault whether all folders are expended by default.
     * @param excludeFiles       whether hide files.
     */
    public void loadWorkspace(WorkspaceMeta workspaceMeta, boolean expandAllAsDefault, boolean excludeFiles) {
        this.expandAllAsDefault = expandAllAsDefault;
        this.excludeFiles = excludeFiles;
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            log.debug("Workspace node '%s' is loaded".formatted(workspaceDataTreeItem.getValue()));
            Platform.runLater(() -> {
                treeView.refresh();
                treeView.requestFocus();
            });
        });
        asyncCreateWorkspaceSubTree(workspaceMeta);
    }

    /**
     * Reload project sub-tree to specified position of tree.
     *
     * @param workspaceMeta
     * @param index         original index in siblings.
     */
    public void reloadProject(WorkspaceMeta workspaceMeta, int index) {
        EventBus.getIns().subscribeWorkspaceLoaded(1, projectDataTreeItem -> {
            rootItem.getChildren().add(index, projectDataTreeItem);
            List<String> treeExpandedList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
//            onTreeExpandRestore(treeExpandedList);
            treeView.getSelectionModel().select(projectDataTreeItem);
            Platform.runLater(() -> treeView.requestFocus());
        });
        asyncCreateWorkspaceSubTree(workspaceMeta);
    }

    /**
     * Reload folder and it's sub-tree to specified position of tree.
     *
     * @param folderData
     * @param index
     */
    public void reloadFolder(NodeData folderData, int index) {
        Tree tree = WorkspaceManager.getIns().loadFolderRecursively(folderData.getFile(), workspaceConfig.makeFileFilter());
        TreeItem<NodeData> treeItem = new TreeItem<>(folderData);
        this.loadTreeNode(tree.getRootNode(), treeItem);
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        TreeItem<NodeData> parent = selectedTreeItem.getParent();
        treeItem.setExpanded(selectedTreeItem.isExpanded());
        parent.getChildren().remove(selectedTreeItem);
        parent.getChildren().add(index, treeItem);
        treeView.getSelectionModel().select(treeItem);
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        new Thread(() -> {
            log.debug("start a new thread to load workspace: %s".formatted(workspaceMeta.getBaseDirPath()));
            Tree tree = WorkspaceManager.getIns().loadWorkspaceRecursively(workspaceConfig, workspaceMeta);
            Node workspaceNode = tree.getRootNode();
            rootItem = new TreeItem<>((NodeData) workspaceNode.getData());

            Platform.runLater(() -> {
                this.loadTreeNode(workspaceNode, rootItem);
                treeView.setRoot(rootItem);
                log.debug("workspace loaded: " + workspaceMeta.getBaseDirPath());
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
    private void loadTreeNode(Node parentNode, TreeItem<NodeData> parent) {
        for (Node childNode : parentNode.getChildren()) {
            NodeData nodeData = (NodeData) childNode.getData();
            if (nodeData.isFolder()) {
                TreeItem<NodeData> folderItem = this.addFolder(parent, nodeData);
                folderItem.setExpanded(this.expandAllAsDefault);
                this.loadTreeNode(childNode, folderItem); // recursive
            }
            else if (nodeData.isFile()) {
                if (!this.excludeFiles) {
                    this.addFile(parent, nodeData);
                }
            }
        }
    }

    public TreeItem<NodeData> addFolder(TreeItem<NodeData> parent, NodeData folderData) {
        TreeItem<NodeData> folderItem = new TreeItem<>(folderData);
        parent.getChildren().add(folderItem);
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return folderItem;
    }

    public TreeItem<NodeData> addFileAndSelect(TreeItem<NodeData> parent, NodeData fileData) {
        TreeItem<NodeData> treeItem = this.addFile(parent, fileData);
        treeView.select(treeItem);
        return treeItem;
    }

    public TreeItem<NodeData> addFile(TreeItem<NodeData> parent, NodeData fileData) {
        TreeItem<NodeData> fileItem = new TreeItem<>(fileData);
        fileItem.setExpanded(this.expandAllAsDefault);
        parent.getChildren().add(fileItem);
        FXCollections.sort(parent.getChildren(), SORTING_TREE_ITEMS);
        return fileItem;
    }

    public void clear() {
        treeView.removeAll();
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
                NodeData value = treeItem.getValue();
                if (value.getFile().equals(nodeData.getFile())) {
                    log.debug("Found tree item to select");
                    treeItem.setExpanded(true);
                    treeView.getSelectionModel().select(treeItem);
                    return false;
                }
                return true;
            });
        }
    }

    /**
     * Expand specified nodes in this workspace tree.
     *
     * @param expendedFileList
     */
    public void expandTreeNodes(List<String> expendedFileList) {
        TreeVisitor.dfsTraverse(rootItem, treeItem -> {
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

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

}
