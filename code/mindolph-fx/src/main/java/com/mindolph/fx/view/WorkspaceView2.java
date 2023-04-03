package com.mindolph.fx.view;

import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.TreeFinder;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.*;
import com.mindolph.base.util.MindolphFileUtils;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.constant.NodeType;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.constant.Templates;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.SearchService;
import com.mindolph.core.template.Template;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.fx.IconBuilder;
import com.mindolph.fx.constant.IconName;
import com.mindolph.fx.dialog.FindInFilesDialog;
import com.mindolph.fx.helper.SceneRestore;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.search.MindMapTextMatcher;
import com.mindolph.plantuml.PlantUmlTemplates;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static com.mindolph.core.constant.SceneStatePrefs.*;
import static com.mindolph.core.constant.SupportFileTypes.TYPE_MIND_MAP;

/**
 * Load workspaces.
 * Load folders and files(with filters) for selected workspace to tree view lazily.
 * Open file by double-clicking.
 * Dra and drop single folder/file to another folder.
 * Context menu: rename, clone delete, open in system, find in files, new folder/mmd/plantuml/md/txt.
 *
 * @author mindolph.com@gmail.com
 */
public class WorkspaceView2 extends BaseView implements EventHandler<ActionEvent> {

    private final Logger log = LoggerFactory.getLogger(WorkspaceView2.class);

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
    private ComboBox<Pair<String, WorkspaceMeta>> cbWorkspaces;
    @FXML
    private TreeView<NodeData> treeView;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnReload;
    @FXML
    private Button btnCollapseAll;
    @FXML
    private Button btnFindInFiles;

    private final TreeItem<NodeData> rootItem; // root node is not visible
    private List<String> expendedFileList; // set by event listener, used for tree expansion restore.
    private NodeData activeWorkspaceData; // set during workspace loading, used for context menu.

    private ContextMenu contextMenuNew; // context menu for button "New"
    private ContextMenu itemContextMenu = null;
    private MenuItem miFolder;
    private MenuItem miMindMap;
    private MenuItem miTextFile;
    private Menu plantUmlMenu;
    private MenuItem miMarkdown;
    private MenuItem miCsvFile;
    private MenuItem miCopyFile;
    private MenuItem miPasteFile;
    private MenuItem miCopyPathAbsolute;
    private MenuItem miCopyPathRelative;
    private MenuItem miRename;
    private MenuItem miReload;
    private MenuItem miClone;
    private MenuItem miDelete;
    private MenuItem miOpenInSystem;
    private MenuItem miFindFiles;
    private MenuItem miCollapseAll;

    // Event handlers that handle events from me.
    private SearchResultEventHandler searchEventHandler;

    public WorkspaceView2() {
        super("/view/workspace_view2.fxml");
        log.info("Init workspace view");
        rootItem = new TreeItem<>(new NodeData("Workspace Stub", null));

        cbWorkspaces.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, WorkspaceMeta> pair) {
                return pair == null ? "" : pair.getValue().getName();
            }

            @Override
            public Pair<String, WorkspaceMeta> fromString(String string) {
                return null;
            }
        });
        cbWorkspaces.setCellFactory(pairListView -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, WorkspaceMeta> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(item.getValue().getName());
                }
            }
        });
        cbWorkspaces.getSelectionModel().selectedItemProperty().addListener((observableValue, workspaceMeta, selectedWorkspace) -> {
            if (selectedWorkspace != null) {
                loadWorkspace(selectedWorkspace.getValue());
                fxPreferences.savePreference(MINDOLPH_ACTIVE_WORKSPACE, selectedWorkspace.getValue().getBaseDirPath());
            }
        });
        cbWorkspaces.setOnDragEntered(event -> {
            cbWorkspaces.setBorder(new Border(new BorderStroke(Color.DARKBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            event.consume();
        });
        cbWorkspaces.setOnDragExited(event -> {
            cbWorkspaces.setBorder(null);
            log.debug("exit");
        });
        cbWorkspaces.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });
        cbWorkspaces.setOnDragDropped(event -> {
            log.debug("dropped");
            if (event.getDragboard().hasString()) {
                List<File> files = event.getDragboard().getFiles();
                log.debug(StringUtils.join(files, ", "));
                moveToTreeItem(files.stream().map(NodeData::new).toList(), rootItem);
            }
            event.consume();
        });

        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        treeView.setOnKeyPressed(event -> {
            log.debug("key pressed: %s".formatted(event.getCode()));
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFile();
                event.consume();
            }
        });

        treeView.setCellFactory(treeView -> {
            WorkspaceViewCell cell = new WorkspaceViewCell();
            // handle double-click to open file
            cell.setOnMouseClicked(mouseEvent -> {
                if (itemContextMenu != null) itemContextMenu.hide();
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    TreeItem<NodeData> ti = cell.getTreeItem();
                    itemContextMenu = createItemContextMenu(ti);
                    itemContextMenu.show(treeView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
                else if (mouseEvent.getClickCount() == 2) {
                    this.openSelectedFile();
                }
            });
            cell.focusedProperty().addListener((observable, oldValue, isFocused) -> {
                if (itemContextMenu != null && !isFocused) itemContextMenu.hide();
            });
            // bind the cell index to a tree item for auto-scroll
            cell.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) newValue.setDisplayIndex(cell.getIndex());
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
            cell.setDragFileEventHandler((nodeDatas, target) -> {
                File toDir = target.getFile();
                if (!toDir.isDirectory() || !toDir.exists()) {
                    log.warn("Target dir doesn't exist: %s".formatted(toDir.getPath()));
                    return;
                }
                log.debug("Drop %d files to %s".formatted(nodeDatas.size(), toDir.getPath()));
                TreeItem<NodeData> treeItemFolder = findTreeItemByFile(toDir);
                moveToTreeItem(nodeDatas, treeItemFolder);
            });
            return cell;
        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedItem) -> {
            log.debug("Selection changed: " + selectedItem);
            Optional<NodeData> selectedValue = this.getSelectedValue();
            EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.NEW_FILE,
                    selectedValue.isPresent()
                            && !selectedValue.get().isFile());
        });

        btnNew.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnReload.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnCollapseAll.setGraphic(FontIconManager.getIns().getIcon(IconKey.COLLAPSE_ALL));
        btnFindInFiles.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEARCH));

        EventHandler<MouseEvent> btnEventHandler = event -> {
            treeView.getSelectionModel().select(rootItem); // root item is the item for workspace
            Button btn = (Button) event.getSource();
            if (btn == btnNew) {
                if (contextMenuNew != null) {
                    contextMenuNew.hide();
                }
                else {
                    contextMenuNew = new ContextMenu();
                    Menu menuNew = createMenuNew();
                    contextMenuNew.getItems().addAll(menuNew.getItems());
                    contextMenuNew.setAutoHide(true);// doesn't work but kept for reference
                }
                contextMenuNew.show(WorkspaceView2.this, event.getScreenX(), event.getScreenY());
            }
            else if (btn == btnReload) {
                reloadWorkspace(rootItem, activeWorkspaceData);
            }
            else if (btn == btnCollapseAll) {
                collapseTreeNodes(rootItem, false);
            }
            else if (btn == btnFindInFiles) {
                launchFindInFilesDialog(activeWorkspaceData);
            }
        };

        btnNew.focusedProperty().addListener((observable, oldValue, isFocused) -> {
            if (!isFocused && contextMenuNew != null) contextMenuNew.hide();
        });
        btnNew.setOnMouseClicked(btnEventHandler);
        btnReload.setOnMouseClicked(btnEventHandler);
        btnCollapseAll.setOnMouseClicked(btnEventHandler);
        btnFindInFiles.setOnMouseClicked(btnEventHandler);

        EventBus.getIns().subscribeNewFileToWorkspace(file -> {
                    TreeItem<NodeData> parentTreeItem = this.findTreeItemByFile(file.getParentFile());
                    this.addFileAndSelect(parentTreeItem, new NodeData(file));
                })
                .subscribeWorkspaceRenamed(event -> {
                    this.loadWorkspaces(WorkspaceManager.getIns().getWorkspaceList());
                })
                .subscribeWorkspaceClosed(closedWorkspaceMeta -> {
                    // TODO refactor?
                    if (closedWorkspaceMeta.getBaseDirPath().equals(activeWorkspaceData.getFile().getPath())) {
                        activeWorkspaceData = null;
                        fxPreferences.savePreference(MINDOLPH_ACTIVE_WORKSPACE, StringUtils.EMPTY);
                    }
                    SceneRestore.getInstance().saveScene(WorkspaceManager.getIns().getWorkspaceList());
                    this.loadWorkspaces(WorkspaceManager.getIns().getWorkspaceList());
                });
        SearchService.getIns().registerMatcher(TYPE_MIND_MAP, new MindMapTextMatcher());
    }

    private void moveToTreeItem(List<NodeData> nodeDatas, TreeItem<NodeData> targetTreeItem) {
        if (targetTreeItem == null) {
            log.warn("No tree item folder provided");
            return;
        }
        boolean needReload = false;
        for (NodeData nodeData : nodeDatas) {
            File fileToBeMoved = nodeData.getFile();
            // update the tree
            TreeItem<NodeData> treeItemToBeMoved = findTreeItemByFile(fileToBeMoved);
            if (treeItemToBeMoved == null || treeItemToBeMoved == targetTreeItem || treeItemToBeMoved.getParent() == targetTreeItem) {
                log.debug("Nothing to do");
            }
            else {
                log.debug("Move tree item '%s' to '%s'".formatted(treeItemToBeMoved.getValue(), targetTreeItem.getValue()));
                treeItemToBeMoved.getParent().getChildren().remove(treeItemToBeMoved); // detach self from parent

                File newFile = new File(targetTreeItem.getValue().getFile(), nodeData.getName());
                try {
                    if (nodeData.isFile()) {
                        FileUtils.moveFile(fileToBeMoved, newFile);
                        log.debug("File %s is moved".formatted(fileToBeMoved.getName()));
                    }
                    else if (nodeData.isFolder()) {
                        FileUtils.moveDirectory(fileToBeMoved, newFile);
                        log.debug("Folder %s is moved".formatted(fileToBeMoved.getName()));
                    }
                    EventBus.getIns().notifyFilePathChanged(nodeData, newFile);
                    nodeData.setFile(newFile);
                    FXCollections.sort(targetTreeItem.getChildren(), SORTING_TREE_ITEMS);
                    needReload = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Moving file failed");
                }
            }
        }
        if (needReload) {
            this.reloadFolder(targetTreeItem, targetTreeItem.getValue());
            if (targetTreeItem == rootItem) {
                // directly call this method because the root item is hidden
                onTreeItemExpandOrCollapsed(true, targetTreeItem);
            }
            else {
                // collapse and expand to activate the loading of sub-folders for the expanded target folder.
                targetTreeItem.setExpanded(false);
                targetTreeItem.setExpanded(true);
            }
            treeView.refresh();
        }
    }

    /**
     * Load all workspaces to combobox to let user select.
     *
     * @param workspaceList
     */
    public void loadWorkspaces(WorkspaceList workspaceList) {
        cbWorkspaces.getItems().clear();
        cbWorkspaces.getItems().addAll(workspaceList.getProjects().stream().map(workspaceMeta -> {
            return new Pair<>(workspaceMeta.getBaseDirPath(), workspaceMeta);
        }).toList());
        // init the active workspace if the workspace exist
        String activeWorkspacePath = fxPreferences.getPreference(MINDOLPH_ACTIVE_WORKSPACE, String.class);
        log.debug("Last active workspace: " + activeWorkspacePath);
        if (StringUtils.isNotBlank(activeWorkspacePath)
                && cbWorkspaces.getItems().stream().anyMatch(p -> p.getKey().equals(activeWorkspacePath))) {
            cbWorkspaces.setValue(new Pair<>(activeWorkspacePath, workspaceList.matchByFilePath(activeWorkspacePath)));
        }
        else {
            Optional<WorkspaceMeta> first = workspaceList.getProjects().stream().findFirst();
            first.ifPresent(workspaceMeta -> cbWorkspaces.setValue(new Pair<>(workspaceMeta.getBaseDirPath(), workspaceMeta)));
        }
        EventBus.getIns().notifyWorkspacesRestored(); // notify that all workspaces restored
    }

    /**
     * Load a workspace sub-tree.
     *
     * @param workspaceMeta
     */
    public void loadWorkspace(WorkspaceMeta workspaceMeta) {
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            activeWorkspaceData = workspaceDataTreeItem.getValue();
            expendedFileList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
            this.expandTreeNodes();
            Platform.runLater(() -> treeView.requestFocus());
        });
        asyncCreateWorkspaceSubTree(workspaceMeta);
    }

    private void reloadWorkspace(TreeItem<NodeData> selectedTreeItem, NodeData selectedData) {
        if (selectedData != null) {
            if (selectedData.isWorkspace()) {
                rootItem.getChildren().remove(selectedTreeItem);
                WorkspaceMeta meta = new WorkspaceMeta(selectedData.getFile().getPath());
                this.loadWorkspace(meta);
            }
        }
    }

    /**
     * Reload folder and it's sub-tree to specified position of tree.
     *
     * @param treeItem   tree item to load data to
     * @param folderData
     */
    public void reloadFolder(TreeItem<NodeData> treeItem, NodeData folderData) {
        log.debug("reload folder: %s".formatted(folderData.getFile()));
        List<NodeData> childrenData = WorkspaceManager.getIns().loadFolder(folderData, workspaceConfig);
        this.loadTreeNode(treeItem, childrenData);
        treeView.getSelectionModel().select(treeItem);
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        new Thread(() -> {
            createWorkspaceSubTree(workspaceMeta);
        }, "Workspace Load Thread").start();
    }

    private void createWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        NodeData workspaceData = new NodeData(NodeType.WORKSPACE, new File(workspaceMeta.getBaseDirPath()));
        workspaceData.setWorkspaceData(workspaceData);
        rootItem.setValue(workspaceData);
        List<NodeData> childrenData = WorkspaceManager.getIns().loadWorkspace(workspaceData, workspaceConfig);
        Platform.runLater(() -> {
            this.loadTreeNode(rootItem, childrenData);
            rootItem.getChildren().forEach(nodeDataTreeItem -> {
                NodeData folderData = nodeDataTreeItem.getValue();
                if (folderData.isFolder()) {
                    List<NodeData> grandChildrenData = WorkspaceManager.getIns().loadFolder(folderData, workspaceConfig);
                    this.loadTreeNode(nodeDataTreeItem, grandChildrenData);
                }
            });
            log.debug("workspace loaded: " + workspaceMeta.getBaseDirPath());
            EventBus.getIns().notifyWorkspaceLoaded(rootItem);
        });
    }

    private void loadTreeNode(TreeItem<NodeData> parent, List<NodeData> childrenData) {
        // note: use name other than file path to match tree item and node data, because the file path changes when folder name changes.
        log.trace("load folder item: %s/".formatted(parent));
        // remove not exists tree items for theirs file might be deleted.
        boolean isRemoved = parent.getChildren().removeIf(nodeDataTreeItem -> {
            return childrenData.stream().noneMatch(nodeData -> {
                return nodeDataTreeItem.getValue().getName().equals(nodeData.getName());
            });
        });
        if (isRemoved) log.trace("Some tree items are moved");

        for (NodeData childNodeData : childrenData) {
            // already exists
            Optional<TreeItem<NodeData>> existing = parent.getChildren().stream().filter(nodeDataTreeItem -> childNodeData.getName().equals(nodeDataTreeItem.getValue().getName())).findFirst();
            if (existing.isPresent()) {
                log.trace("already exists, ignore: %s".formatted(childNodeData));
                TreeItem<NodeData> existingItem = existing.get();
                log.trace("existing tree item: %s, expanded: %s".formatted(existingItem.getValue().getFile(), existingItem.isExpanded()));
                existingItem.setValue(childNodeData);
                if (childNodeData.isFolder() && existingItem.isExpanded()) {
                    loadTreeNode(existingItem, WorkspaceManager.getIns().loadFolder(childNodeData, workspaceConfig));
                }
                continue;
            }
            log.trace("%s does not existed, try to create it.".formatted(childNodeData));
            if (childNodeData.isFolder()) {
                TreeItem<NodeData> folderItem = this.addFolder(parent, childNodeData);
                log.trace("add folder: %s/".formatted(folderItem.getValue().getName()));
            }
            else if (childNodeData.isFile()) {
                TreeItem<NodeData> fileItem = this.addFile(parent, childNodeData);
                log.trace("add file: %s".formatted(fileItem.getValue().getName()));
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
        Optional<NodeData> selectedValue = getSelectedValue();
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


    private ContextMenu createItemContextMenu(TreeItem<NodeData> treeItem) {
        ContextMenu contextMenu = new ContextMenu();
        if (treeItem != null) {
            log.debug("create context menu for item: " + treeItem.getValue().getName());
            NodeData nodeData = treeItem.getValue();
            boolean isFolder = !nodeData.isFile();
            boolean isFile = nodeData.isFile();
            if (isFolder) {
                Menu miNew = createMenuNew();
                contextMenu.getItems().add(miNew);
            }
            if (isFile) {
                Menu miCopy = new Menu("Copy");
                miCopyFile = new MenuItem("File", FontIconManager.getIns().getIcon(IconKey.FILE));
                miCopyPathAbsolute = new MenuItem("Absolute Path: " + StringUtils.abbreviateMiddle(treeItem.getValue().getFile().getPath(), "...", 32));
                miCopyPathRelative = new MenuItem("Relative Path: " + StringUtils.abbreviateMiddle(treeItem.getValue().getFileRelativePath(), "...", 32));
                miCopyFile.setOnAction(this);
                miCopyPathAbsolute.setOnAction(this);
                miCopyPathRelative.setOnAction(this);
                miCopy.getItems().addAll(miCopyFile, miCopyPathAbsolute, miCopyPathRelative);
                contextMenu.getItems().add(miCopy);
            }
            if (isFolder) {
                miPasteFile = new MenuItem("Paste");
                miPasteFile.setDisable(!Clipboard.getSystemClipboard().hasFiles());
                miPasteFile.setOnAction(this);
                contextMenu.getItems().add(miPasteFile);
            }
            miRename = new MenuItem("Rename", FontIconManager.getIns().getIcon(IconKey.RENAME));
            miRename.setOnAction(this);
            contextMenu.getItems().addAll(miRename);
            if (isFolder) {
                miReload = new MenuItem("Reload", FontIconManager.getIns().getIcon(IconKey.REFRESH));
                miReload.setOnAction(this);
                contextMenu.getItems().addAll(miReload);
            }
            else if (nodeData.isFile()) {
                miClone = new MenuItem("Clone", FontIconManager.getIns().getIcon(IconKey.CLONE));
                miClone.setOnAction(this);
                contextMenu.getItems().add(miClone);
            }
            miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
            miDelete.setOnAction(this);
            contextMenu.getItems().addAll(miDelete);
            if (!nodeData.isMindMap()){
                miOpenInSystem = new MenuItem("Open in System", FontIconManager.getIns().getIcon(IconKey.SYSTEM));
                miOpenInSystem.setOnAction(this);
                contextMenu.getItems().add(miOpenInSystem);
            }
            if (isFolder) {
                miCollapseAll = new MenuItem("Collapse All", FontIconManager.getIns().getIcon(IconKey.COLLAPSE_ALL));
                miCollapseAll.setOnAction(this);
                miFindFiles = new MenuItem("Find in Files", FontIconManager.getIns().getIcon(IconKey.SEARCH));
                miFindFiles.setOnAction(this);
                contextMenu.getItems().addAll(miCollapseAll, new SeparatorMenuItem(), miFindFiles);
            }
        }
        return contextMenu;
    }


    private Menu createMenuNew() {
        Menu miNew = new Menu("New");
        miFolder = new MenuItem("Folder", new IconBuilder().name(IconName.FOLDER).build());
        miMindMap = new MenuItem("Mind Map(.mmd)", new IconBuilder().name(IconName.FILE_MMD).build());
        miMarkdown = new MenuItem("Markdown(.md)", new IconBuilder().name(IconName.FILE_MARKDOWN).build());
        plantUmlMenu = new Menu("PlantUML(.puml)", new IconBuilder().name(IconName.FILE_PUML).build());
        miCsvFile = new MenuItem("Sheet(.csv)", new IconBuilder().name(IconName.FILE_CSV).build());
        for (Template template : PlantUmlTemplates.getIns().getTemplates()) {
            MenuItem mi = new MenuItem(template.getTitle());
            mi.setUserData(template);
            mi.setOnAction(this);
            plantUmlMenu.getItems().add(mi);
        }
        miTextFile = new MenuItem("Text(.txt)", new IconBuilder().name(IconName.FILE_TXT).build());
        miNew.getItems().addAll(miFolder, miMindMap, miMarkdown, plantUmlMenu, miTextFile, miCsvFile);
        miFolder.setOnAction(this);
        miMindMap.setOnAction(this);
        miMarkdown.setOnAction(this);
        miTextFile.setOnAction(this);
        miCsvFile.setOnAction(this);
        return miNew;
    }

    /**
     * Find and select a tree item by it's node data and expand it's path nodes.
     *
     * @param nodeData
     */
    public void selectByNodeDataInAppropriateWorkspace(NodeData nodeData) {
        if (nodeData != null) {
            WorkspaceMeta workspaceMeta = WorkspaceManager.getIns().getWorkspaceList().matchByFilePath(nodeData.getFile().getPath());
            if (workspaceMeta.getBaseDirPath().equals(activeWorkspaceData.getFile().getPath())) {
                selectByNodeData(nodeData);
                this.scrollToSelected();
            }
            else {
                EventBus.getIns().subscribeWorkspaceLoaded(1, nodeDataTreeItem -> {
                    selectByNodeData(nodeData);
                    this.scrollToSelected();
                });
                log.debug("Select workspace: %s".formatted(workspaceMeta.getBaseDirPath()));
                cbWorkspaces.getSelectionModel().select(new Pair<>(workspaceMeta.getBaseDirPath(), workspaceMeta));
            }
        }
    }

    /**
     * Find and select a tree item by it's node data and expand it's path nodes.
     *
     * @param nodeData
     * @return TreeItem if selected.
     */
    private TreeItem<NodeData> selectByNodeData(NodeData nodeData) {
        if (nodeData != null) {
            log.debug("Select in tree: %s".formatted(nodeData));
            return TreeVisitor.dfsSearch(rootItem, treeItem -> {
                NodeData curNodeData = treeItem.getValue();
                if (!treeItem.isLeaf() && curNodeData.isParentOf(nodeData)) {
                    treeItem.setExpanded(true);
                }
                if (curNodeData.getFile().equals(nodeData.getFile())) {
                    log.debug("Found tree item to select");
                    treeItem.setExpanded(true);
                    treeView.getSelectionModel().select(treeItem);
                    return treeItem; // stop traversing
                }
                return null; // keep traversing
            });
        }
        return null;
    }

    public void scrollToSelected() {
        Platform.runLater(() -> {
            if (!isItemVisible(treeView.getSelectionModel().getSelectedItem())) {
                log.debug("Scroll to invisible selected tree item");
                treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
//        treeView.refresh();
            }
        });
    }

    private boolean isItemVisible(TreeItem<NodeData> item) {
        if (item == null) return false;
        TreeViewSkin<?> skin = (TreeViewSkin<?>) treeView.getSkin();
        VirtualFlow<?> vf = (VirtualFlow<?>) skin.getChildren().get(0);
        int f = vf.getFirstVisibleCell().getIndex();
        int l = vf.getLastVisibleCell().getIndex();
        Integer i = item.getValue().getDisplayIndex();
        log.trace("The index of target tree item %d".formatted(i));
        log.trace("  is between %d and %d".formatted(f, l));
        return i != null && (i >= f && i <= l);
    }


    /**
     * Expand specified nodes in this workspace tree.
     */
    public void expandTreeNodes() {
        TreeVisitor.dfsTraverse(rootItem, treeItem -> {
            // excludes the nodes whose parent is collapsed.
            if (treeItem.getParent() != null && !treeItem.getParent().isExpanded()) {
                return null;
            }
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
     * @param includeParent
     */
    public void collapseTreeNodes(TreeItem<NodeData> treeItem, boolean includeParent) {
        log.debug("Collapse all expanded nodes under " + treeItem);
        TreeVisitor.dfsTraverse(treeItem, item -> {
            if (item.isExpanded()) {
                log.debug("Collapse node: " + item);
                item.setExpanded(false);
            }
            return null;
        });
        if (includeParent) treeItem.setExpanded(false);
    }

    /**
     * Handle tree node expansion and collapse and call outer listener.
     *
     * @param expanded
     * @param treeItem
     */
    private void onTreeItemExpandOrCollapsed(Boolean expanded, TreeItem<NodeData> treeItem) {
        if (expanded) {
            EventBus.getIns().notifyTreeExpandCollapse(treeItem, true);
            // if expanded, pre-load all children of each child of this tree item.
            for (TreeItem<NodeData> child : treeItem.getChildren()) {
                File childFile = child.getValue().getFile();
                if (childFile.isDirectory()) {
                    List<NodeData> childrenOfChild = WorkspaceManager.getIns().loadFolder(child.getValue(), workspaceConfig);
                    this.loadTreeNode(child, childrenOfChild);
                    // expand the child node if it should be restored to expanded.
                    expendedFileList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
                    if (expendedFileList != null && expendedFileList.contains(childFile.getPath())) {
                        child.setExpanded(true);
                    }
                }
            }
        }
        else {
            EventBus.getIns().notifyTreeExpandCollapse(treeItem, false);
        }
    }

    private boolean handlePlantumlCreation(MenuItem mi, File newFile) {
        Object userData = mi.getUserData();
        if (userData == null) return false;
        Template template = (Template) userData;
        try {
            String snippet = template.getContent().formatted(FilenameUtils.getBaseName(newFile.getName()));
            FileUtils.writeStringToFile(newFile, snippet, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        TreeItem<NodeData> selectedTreeItem = getSelectedTreeItem();
        NodeData selectedData = selectedTreeItem.getValue(); // use selected tree item as target even for workspace folder(the root item), because the user data of tree item might be used for other purpose.
        if (source == miFolder) {
            Dialog<String> dialog = new TextDialogBuilder()
                    .owner(DialogFactory.DEFAULT_WINDOW)
                    .title("New Folder Name")
                    .width(300)
                    .build();
            Optional<String> opt = dialog.showAndWait();
            if (opt.isPresent()) {
                String folderName = opt.get();
                if (selectedData.getFile().isDirectory()) {
                    File newDir = new File(selectedData.getFile(), folderName);
                    if (newDir.mkdir()) {
                        selectedTreeItem.setExpanded(true);
                        addFolder(selectedTreeItem, new NodeData(NodeType.FOLDER, newDir));
                        EventBus.getIns().notifyOpenFile(new OpenFileEvent(newDir, true));
                    }
                }
            }
        }
        else if (source == miMindMap || source == miTextFile || source == miMarkdown || source == miCsvFile
                || (source.getParentMenu() != null && source.getParentMenu() == plantUmlMenu)) {
            log.debug("New %s File".formatted(source.getText()));
            log.debug("source: %s from %s".formatted(source.getText(), source.getParentMenu() == null ? "" : source.getParentMenu().getText()));
            Dialog<String> dialog = new TextDialogBuilder()
                    .owner(DialogFactory.DEFAULT_WINDOW)
                    .width(300)
                    .title("New %s Name".formatted(source.getText())).build();
            Optional<String> opt = dialog.showAndWait();
            if (opt.isPresent()) {
                String fileName = opt.get();

                if (selectedData.getFile().isDirectory()) {
                    File newFile = null;
                    if (source == miMindMap) {
                        newFile = createEmptyFile(fileName, selectedData, "mmd");
                        if (newFile != null) {
                            final MindMap<TopicNode> mindMap = new MindMap<>();
                            ExtraNote extraNote = new ExtraNote("Created on " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
                            TopicNode rootTopic = new TopicNode(mindMap, null, FilenameUtils.getBaseName(newFile.getPath()), extraNote);
                            mindMap.setRoot(rootTopic);
                            final String text;
                            try {
                                text = mindMap.write(new StringWriter()).toString();
                                FileUtils.writeStringToFile(newFile, text, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else if (source == miTextFile) {
                        newFile = createEmptyFile(fileName, selectedData, "txt");
                    }
                    else if (source.getParentMenu() == plantUmlMenu) {
                        log.debug("Handle dynamic menu item: " + source.getText());
                        newFile = createEmptyFile(fileName, selectedData, "puml");
                        if (newFile != null) {
                            this.handlePlantumlCreation(source, newFile);
                        }
                    }
                    else if (source == miMarkdown) {
                        newFile = createEmptyFile(fileName, selectedData, "md");
                        if (newFile != null) {
                            String snippet = Templates.MARKDOWN_TEMPLATE.formatted(fileName);
                            try {
                                FileUtils.writeStringToFile(newFile, snippet, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else if (source == miCsvFile) {
                        newFile = createEmptyFile(fileName, selectedData, "csv");
                    }
                    else {
                        log.warn("Not supported file type?");
                        return;
                    }
                    // add new file to tree view
                    if (newFile != null && newFile.exists()) {
                        selectedTreeItem.setExpanded(true);
                        addFile(selectedTreeItem, new NodeData(newFile));
                        EventBus.getIns().notifyOpenFile(new OpenFileEvent(newFile));
                    }
                }
            }
        }
        else if (source == miCopyFile) {
            ClipBoardUtils.filesToClipboard(Collections.singletonList(selectedData.getFile()));
        }
        else if (source == miCopyPathAbsolute) {
            ClipBoardUtils.textToClipboard(selectedData.getFile().getAbsolutePath());
        }
        else if (source == miCopyPathRelative) {
            ClipBoardUtils.textToClipboard(selectedData.getFileRelativePath());
        }
        else if (source == miPasteFile) {
            this.copyFile(selectedTreeItem, ClipBoardUtils.filesFromClipboard().get(0));
        }
        else if (source == miRename) {
            this.requestRenameFolderOrFile(selectedData, newNameFile -> {
                if (selectedData.isFile()) {
                    NodeData newFileData = new NodeData(newNameFile);
                    newFileData.setWorkspaceData(selectedData.getWorkspaceData());
                    selectedTreeItem.setValue(newFileData);
                }
                else if (selectedData.isFolder()) {
                    NodeData newFolderData = new NodeData(NodeType.FOLDER, newNameFile);
                    newFolderData.setWorkspaceData(selectedData.getWorkspaceData());
                    selectedTreeItem.setValue(newFolderData);
                    this.reloadFolder(selectedTreeItem, newFolderData);
                }
                treeView.refresh();
                // remove old path from expanded list as well.
                SceneRestore.getInstance().removeFromExpandedList(selectedData.getFile().getPath());
                EventBus.getIns().notifyFilePathChanged(selectedData, newNameFile);
            });
        }
        else if (source == miClone) {
            if (selectedData != null) {
                if (selectedData.isFile()) {
                    File file = selectedData.getFile();
                    this.copyFile(selectedTreeItem.getParent(), file);
                }
            }
        }
        else if (source == miDelete) {
            if (selectedData != null) {
                try {
                    if (selectedData.getFile().isDirectory() && !FileUtils.isEmptyDirectory(selectedData.getFile())) {
                        DialogFactory.errDialog("You can not delete a folder with files.");
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                boolean needDelete = DialogFactory.yesNoConfirmDialog("Are you sure to delete %s".formatted(selectedData.getName()));
                if (needDelete) {
                    log.info("Delete file: %s".formatted(selectedData.getFile()));
                    try {
                        FileUtils.delete(selectedData.getFile());
                    } catch (IOException e) {
                        e.printStackTrace();
                        DialogFactory.errDialog("Delete file failed: " + e.getLocalizedMessage());
                        return;
                    }
                    // remove from recent list if exists
                    EventBus.getIns().notifyDeletedFile(selectedData);
                    removeTreeNode(selectedData);
                }
            }
        }
        else if (source == miReload) {
            this.reloadFolder(selectedTreeItem, selectedData);
        }
        else if (source == miFindFiles) {
            this.launchFindInFilesDialog(selectedData);
        }
        else if (source == miOpenInSystem) {
            if (selectedData != null) {
                log.info("Try to open file: " + selectedData.getFile());
                if (selectedData.isMindMap()) {
                    this.openSelectedFile(); // always open mmd file in Mindolph
                }
                else {
                    MindolphFileUtils.openFileInSystem(selectedData.getFile());
                }
            }
        }
        else if (source == miCollapseAll) {
            this.collapseTreeNodes(treeView.getSelectionModel().getSelectedItem(), true);
        }
    }

    /**
     * Copy file to target folder of tree item with existence check.
     *
     * @param folderTreeItem
     * @param file
     */
    private void copyFile(TreeItem<NodeData> folderTreeItem, File file) {
        File targetDir = folderTreeItem.getValue().getFile();
        String cloneFileName = "%s_copy.%s".formatted(FilenameUtils.getBaseName(file.getName()), FilenameUtils.getExtension(file.getName()));
        File cloneFile = new File(targetDir, cloneFileName);
        if (cloneFile.exists()) {
            DialogFactory.errDialog("File %s already exists".formatted(cloneFileName));
            return;
        }
        try {
            FileUtils.copyFile(file, cloneFile);
        } catch (IOException e) {
            e.printStackTrace();
            DialogFactory.errDialog("Clone file failed: " + e.getLocalizedMessage());
            return;
        }
        NodeData newFileData = new NodeData(cloneFile);
        addFile(folderTreeItem, newFileData);
        treeView.refresh();
    }

    private void requestRenameFolderOrFile(NodeData selectedData, Consumer<File> consumer) {
        Optional<String> s = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Rename %s".formatted(selectedData.getName()))
                .content("Input a new name")
                .text(selectedData.isFile() ? FilenameUtils.getBaseName(selectedData.getFile().getPath()) : selectedData.getName())
                .width(400)
                .build().showAndWait();
        if (s.isPresent()) {
            String newName = s.get();
            File origFile = selectedData.getFile();
            if (selectedData.isFile()) {
                newName = FileNameUtils.appendFileExtensionIfAbsent(newName, FilenameUtils.getExtension(origFile.getPath()));
            }
            File newNameFile = new File(origFile.getParentFile(), newName);
            if (newNameFile.exists()) {
                DialogFactory.errDialog("file %s already exists".formatted(newName));
            }
            else {
                if (origFile.renameTo(newNameFile)) {
                    log.debug("Rename file from %s to %s".formatted(origFile.getPath(), newNameFile));
                    consumer.accept(newNameFile);
                }
            }
        }
    }

    private void launchFindInFilesDialog(NodeData selectedData) {
        if (!selectedData.isFile()) {
            if (!selectedData.getFile().exists()) {
                DialogFactory.errDialog("The workspace or folder you selected doesn't exist, probably be deleted externally.");
            }
            else {
                SearchParams searchParams = new FindInFilesDialog(selectedData.getWorkspaceData().getFile(), selectedData.getFile()).showAndWait();
                if (searchParams != null && StringUtils.isNotBlank(searchParams.getKeywords())) {
                    IOFileFilter searchFilter = workspaceConfig.makeFileFilter();
                    searchParams.setWorkspaceDir(selectedData.getWorkspaceData().getFile());
                    searchParams.setSearchInDir(selectedData.getFile());
                    searchParams.setSearchFilter(searchFilter);
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_KEYWORD, searchParams.getKeywords());
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_CASE_SENSITIVITY, searchParams.isCaseSensitive());
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_OPTIONS, searchParams.getFileTypeName());
                    searchEventHandler.onSearchStart(searchParams);
                }
            }
        }
    }

    /**
     * @param fileName
     * @param parentNodeData
     * @param extension
     * @return null if file can't be created.
     */
    private File createEmptyFile(String fileName, NodeData parentNodeData, String extension) {
        fileName = FileNameUtils.appendFileExtensionIfAbsent(fileName, extension);
        File newFile = new File(parentNodeData.getFile(), fileName);
        if (newFile.exists()) {
            DialogFactory.errDialog("File %s already existed!".formatted(fileName));
            return null;
        }
        try {
            FileUtils.touch(newFile);
        } catch (IOException e) {
            return null;
        }
        return newFile;
    }


    public TreeItem<NodeData> getSelectedTreeItem() {
        return treeView.getSelectionModel().getSelectedItem();
    }

    public Optional<NodeData> getSelectedValue() {
        TreeItem<NodeData> selectedItem = getSelectedTreeItem();
        if (selectedItem == null) {
            return Optional.empty();
        }
        else {
            return Optional.ofNullable(selectedItem.getValue());
        }
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

    /**
     * Find a tree item by searching exactly the path of file,
     * because it's faster than traversing the whole tree.
     *
     * @param file
     * @return
     */
    public TreeItem<NodeData> findTreeItemByFile(File file) {
        return TreeFinder.findTreeItemPathMatch(rootItem, treeItem -> {
            File nodeFile = treeItem.getValue().getFile();
            return rootItem == treeItem ||
                    file.getPath().startsWith(nodeFile.getPath());
        }, treeItem -> {
            if (treeItem == rootItem) {
                return false;
            }
            File nodeFile = treeItem.getValue().getFile();
            return nodeFile.equals(file);
        });
    }

    public void removeTreeNode(NodeData nodeData) {
        TreeItem<NodeData> selectedTreeItem = getSelectedTreeItem();
        if (selectedTreeItem.getValue() == nodeData) {
            selectedTreeItem.getParent().getChildren().remove(selectedTreeItem);
            treeView.refresh();
        }
    }

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

    public void setSearchEventHandler(SearchResultEventHandler searchEventHandler) {
        this.searchEventHandler = searchEventHandler;
    }

}
