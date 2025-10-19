package com.mindolph.fx.view;

import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.FileTreeHelper;
import com.mindolph.base.control.MTreeView;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.FileChangeEvent;
import com.mindolph.base.event.FileChangeEvent.FileChangeType;
import com.mindolph.base.event.FolderReloadEvent;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.MindolphFileUtils;
import com.mindolph.base.util.RegionUtils;
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
import com.mindolph.core.util.TimeUtils;
import com.mindolph.csv.CsvMatcher;
import com.mindolph.fx.control.WorkspaceSelector;
import com.mindolph.fx.dialog.FileReferenceDialog;
import com.mindolph.fx.dialog.FindInFilesDialog;
import com.mindolph.fx.dialog.UsageDialog;
import com.mindolph.fx.dialog.WorkspaceDialog;
import com.mindolph.fx.helper.SceneRestore;
import com.mindolph.mfx.dialog.ConfirmDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.GlobalExecutor;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.search.FileLinkMindMapSearchMatcher;
import com.mindolph.mindmap.search.MindMapTextMatcher;
import com.mindolph.plantuml.PlantUmlTemplates;
import io.methvin.watcher.DirectoryWatcher;
import io.methvin.watcher.hashing.FileHasher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mindolph.base.constant.Comparators.SORTING_TREE_ITEMS;
import static com.mindolph.base.constant.PrefConstants.GENERAL_HIDE_EXTENSION;
import static com.mindolph.base.constant.PrefConstants.GENERAL_SHOW_HIDDEN_FILES;
import static com.mindolph.base.util.MindolphFileUtils.deleteMacFile;
import static com.mindolph.base.util.MindolphFileUtils.isFolderEmpty;
import static com.mindolph.core.constant.SceneStatePrefs.*;
import static com.mindolph.core.constant.SupportFileTypes.*;
import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 * An advanced editable workspace view, includes features:
 * Load workspaces.
 * Load folders and files(with filters) for selected workspace to tree view lazily.
 * Open file by double-clicking.
 * Drag and drop a single folder/file to another folder.
 * Context menu: rename, clone, delete, open in the system, find in files, new folder/mmd/plantuml/md/txt/csv.
 *
 * @author mindolph.com@gmail.com
 */
public class WorkspaceViewEditable extends BaseView implements EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceViewEditable.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    private final WorkspaceConfig workspaceConfig = new WorkspaceConfig();

    @FXML
    private WorkspaceSelector workspaceSelector;
    @FXML
    private MTreeView<NodeData> treeView;
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
    private NodeData activeWorkspaceData; // set during workspace loading, used for the context menu.

    private ContextMenu contextMenuNew; // context menu for button "New"
    private ContextMenu itemContextMenu = null;
    private MenuItem miOpenFile;
    private MenuItem miCopyFile;
    private MenuItem miPasteFile;
    private MenuItem miCopyPathAbsolute;
    private MenuItem miCopyPathRelative;
    private MenuItem miRename;
    private MenuItem miMoveTo;
    private MenuItem miReload;
    private MenuItem miClone;
    private MenuItem miDelete;
    private MenuItem miOpenInSystem;
    private MenuItem miUsage;
    private MenuItem miFindFiles;
    private MenuItem miCollapseAll;

    // Event handlers that handle events from me.
    private final EventSource<SearchParams> searchEventSource = new EventSource<>();

    private List<File> foundFiles;

    // Monitoring the files of active workspace.
    private DirectoryWatcher fileWatcher = null;
    // event source for directory monitor to reduce file change events.
    private final EventSource<List<File>> observerEventSource = new EventSource<>();

    public static boolean hideFileExtension = FxPreferences.getInstance().getPreference(GENERAL_HIDE_EXTENSION, false);

    public WorkspaceViewEditable() {
        super("/view/workspace_view_editable.fxml");
        log.info("Init workspace view");
        rootItem = new TreeItem<>(new NodeData("Workspace Stub"));

        workspaceSelector.getSelectionModel().selectedItemProperty().addListener((observableValue, workspaceMeta, selectedWorkspace) -> {
            if (selectedWorkspace != null) {
                this.loadWorkspace(selectedWorkspace.getValue().getWorkspaceMeta());
                fxPreferences.savePreference(MINDOLPH_ACTIVE_WORKSPACE, selectedWorkspace.getValue().getWorkspaceMeta().getBaseDirPath());
            }
            else {
                // clear the tree view if the last workspace is closed.
                treeView.getRoot().getChildren().clear();
            }
            toggleButtons(selectedWorkspace == null);
        });
        workspaceSelector.setOnDragEntered(event -> {
            RegionUtils.applyDragDropBorder(workspaceSelector);
            event.consume();
        });
        workspaceSelector.setOnDragExited(event -> {
            workspaceSelector.setBorder(null);
            log.debug("exit");
        });
        workspaceSelector.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });
        workspaceSelector.setOnDragDropped(event -> {
            log.debug("dropped");
            if (event.getDragboard().hasString()) {
                List<File> files = event.getDragboard().getFiles();
                log.debug(StringUtils.join(files, ", "));
                List<NodeData> nodeDatas = files.stream().map(NodeData::new).toList();
                nodeDatas.forEach(nd -> nd.setWorkspaceData(activeWorkspaceData));
                moveToTreeItem(nodeDatas, rootItem);
            }
            event.consume();
        });

        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setOnKeyPressed(event -> {
            log.debug("key pressed: %s".formatted(event.getCode()));
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFiles();
                event.consume();
            }
        });

        treeView.setCellFactory(treeView -> {
            WorkspaceViewCell cell = new WorkspaceViewCell();
            // handle double-click to the open file
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
                TreeItem<NodeData> treeItemFolder = FileTreeHelper.findTreeItemByFile(rootItem, toDir);
                moveToTreeItem(nodeDatas, treeItemFolder);
            });
            return cell;
        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedItem) -> {
            log.debug("Selection changed: %s".formatted(selectedItem));
            Optional<NodeData> selectedValue = treeView.getSelectedData();
            EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.NEW_FILE,
                    selectedValue.isPresent()
                            && !selectedValue.get().isFile());
        });

        btnNew.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnReload.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnCollapseAll.setGraphic(FontIconManager.getIns().getIcon(IconKey.COLLAPSE_FOLDERS));
        btnFindInFiles.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEARCH));

        // event handler for toolbar buttons.
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
                contextMenuNew.show(WorkspaceViewEditable.this, event.getScreenX(), event.getScreenY());
            }
            else if (btn == btnReload) {
                reloadWorkspace(rootItem, activeWorkspaceData);
            }
            else if (btn == btnCollapseAll) {
                treeView.collapseTreeNodes(rootItem, false);
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


        EventBus.getIns()
                // for "save as"
//                .subscribeNewFileToWorkspace(file -> {
//                    NodeData newFileData = new NodeData(file);
//                    newFileData.setWorkspaceData(activeWorkspaceData);
//                    if (activeWorkspaceData.getFile().equals(file.getParentFile())) {
//                        this.addFileAndSelect(rootItem, newFileData);
//                    }
//                    else {
//                        TreeItem<NodeData> parentTreeItem = FileTreeHelper.findTreeItemByFile(rootItem, file.getParentFile());
//                        this.addFileAndSelect(parentTreeItem, newFileData);
//                    }
//                })
                .subscribeFolderRefreshInWorkspace(folderReloadEvent -> {
                    for (File dir : folderReloadEvent.getDirs()) {
                        NodeData fileData = new NodeData(dir);
                        fileData.setWorkspaceData(activeWorkspaceData);
                        TreeItem<NodeData> treeItem = FileTreeHelper.findTreeItemByFile(rootItem, dir);
                        if (treeItem != null) {
                            this.reloadFolder(treeItem, treeItem.getValue(), false);
                            treeView.getSelectionModel().clearSelection();
                            if (treeItem == rootItem) {
                                // directly call this method because the root item is hidden
                                onTreeItemExpandOrCollapsed(true, treeItem);
                            }
                            else {
                                // collapse and expand to activate the loading of subfolders for the expanded target folder.
                                treeItem.setExpanded(false);
                                treeItem.setExpanded(true);
                            }
                        }
                    }
                })
                .subscribeFileChangeInWorkspace(fileChangeEvent -> {
                    log.debug("file %s is %s".formatted(fileChangeEvent.getFile(), fileChangeEvent.getType()));
                })
//                .subscribeFileChangeInWorkspace(fileChangeEvent -> {
//                    File file = fileChangeEvent.getFile();
//                    NodeData fileData = new NodeData(file);
//                    fileData.setWorkspaceData(activeWorkspaceData);
//                    if (fileChangeEvent.getType() == FileChangeEvent.FileChangeType.CREATE) {
//                        if (activeWorkspaceData.getFile().equals(file.getParentFile())) {
//                            this.addFileAndSelect(rootItem, fileData);
//                        }
//                        else {
//                            TreeItem<NodeData> parentTreeItem = this.findTreeItemByFile(file.getParentFile());
//                            this.addFileAndSelect(parentTreeItem, fileData);
//                        }
//                    }
//                    else if (fileChangeEvent.getType() == FileChangeEvent.FileChangeType.DELETE) {
//                        TreeItem<NodeData> treeItem = this.findTreeItemByFile(file);
//                        if (treeItem != null) treeView.removeTreeItem(treeItem);
//                    }
//                })
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
        SearchService.getIns().registerMatcher(TYPE_MIND_MAP, new MindMapTextMatcher(true));
        SearchService.getIns().registerMatcher(TYPE_CSV, new CsvMatcher(true));
        SearchService.getIns().registerFileLinkMatcher(TYPE_MIND_MAP, new FileLinkMindMapSearchMatcher());

        this.subscribePreferenceChanges();
    }

    private void subscribePreferenceChanges() {
        hideFileExtension = FxPreferences.getInstance().getPreference(GENERAL_HIDE_EXTENSION, Boolean.FALSE);
        workspaceConfig.setShowHiddenFile(FxPreferences.getInstance().getPreference(GENERAL_SHOW_HIDDEN_FILES, Boolean.FALSE));
        PluginEventBus.getIns().subscribePreferenceChanges(pe -> {
            if (pe.getEventType() == PluginEvent.EventType.GENERAL_PREF_CHANGED) {
                Boolean prefHideExt = FxPreferences.getInstance().getPreference(GENERAL_HIDE_EXTENSION, Boolean.FALSE);
                if (hideFileExtension != prefHideExt) {
                    hideFileExtension = prefHideExt;
                    this.treeView.refresh();
                    this.treeView.requestFocus();
                }
                Boolean preShowHiddenFiles = FxPreferences.getInstance().getPreference(GENERAL_SHOW_HIDDEN_FILES, Boolean.FALSE);
                if (workspaceConfig.isShowHiddenFile() != preShowHiddenFiles) {
                    workspaceConfig.setShowHiddenFile(preShowHiddenFiles);
                    reloadWorkspace(rootItem, activeWorkspaceData);
                    this.treeView.refresh();
                    this.treeView.requestFocus();
                }
            }
        });
    }

    public void toggleButtons(boolean disable) {
        btnNew.setDisable(disable);
        btnCollapseAll.setDisable(disable);
        btnReload.setDisable(disable);
        btnFindInFiles.setDisable(disable);
    }

    /**
     * Move file(s) to target tree node in either current workspace or other workspace.
     * If any file is moved successfully, the treeview's selection will be cleared and moved files will be selected.
     *
     * @param nodeDatas      selected files in the current workspace.
     * @param targetTreeItem target tree item in current or other workspace.
     * @return moved files.
     */
    private List<File> moveToTreeItem(List<NodeData> nodeDatas, TreeItem<NodeData> targetTreeItem) {
        if (targetTreeItem == null) {
            log.warn("No tree item folder provided!");
            return null;
        }
//        boolean needReload = false;
        List<File> failedFiles = new LinkedList<>();
        List<File> newFiles = new LinkedList<>();
        for (NodeData nodeData : nodeDatas) {
            File fileToBeMoved = nodeData.getFile();
            if (!this.beforeFilePathChanged(nodeData)) {
                log.debug("Cancel moving file");
                break;
            }
            if (workspaceSelector.getSelectedWorkspace().contains(fileToBeMoved)) {
                // in the same workspace
                TreeItem<NodeData> treeItemToBeMoved = FileTreeHelper.findTreeItemByFile(rootItem, fileToBeMoved);
                if (treeItemToBeMoved == null || treeItemToBeMoved == targetTreeItem || treeItemToBeMoved.getParent() == targetTreeItem) {
                    log.debug("Not move this item: %s".formatted(treeItemToBeMoved));
                }
                else {
                    log.debug("Move tree item '%s' to '%s'".formatted(treeItemToBeMoved.getValue(), targetTreeItem.getValue()));
                    try {
                        newFiles.add(this.moveFile(nodeData, targetTreeItem.getValue().getFile()));
                        // moving files trigger the monitor to update tree items, so no need to do it here anymore.
                        // treeItemToBeMoved.getParent().getChildren().remove(treeItemToBeMoved); // detach self from parent
                        FXCollections.sort(targetTreeItem.getChildren(), SORTING_TREE_ITEMS);
//                        needReload = true;
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage(), e);
                        failedFiles.add(nodeData.getFile());
                    }
                }
            }
            else {
                // Moving to different workspace doesn't require
                try {
                    newFiles.add(this.moveFile(nodeData, targetTreeItem.getValue().getFile()));
                    FXCollections.sort(targetTreeItem.getChildren(), SORTING_TREE_ITEMS);
//                    needReload = true;
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    failedFiles.add(nodeData.getFile());
                }
            }
        }
//        if (needReload) {
//            this.reloadFolder(targetTreeItem, targetTreeItem.getValue(), false);
//            if (targetTreeItem == rootItem) {
//                // directly call this method because the root item is hidden
//                onTreeItemExpandOrCollapsed(true, targetTreeItem);
//            }
//            else {
//                // collapse and expand to activate the loading of subfolders for the expanded target folder.
//                targetTreeItem.setExpanded(false);
//                targetTreeItem.setExpanded(true);
//            }
//            treeView.reselect(newFiles.stream().map(this::findTreeItemByFile).toList());
//            treeView.refresh();
//        }
        if (!failedFiles.isEmpty()) {
            DialogFactory.warnDialog("Failed files: \n" + StringUtils.join(failedFiles, LINE_SEPARATOR));
        }
        return newFiles;
    }

    /**
     * Move the file from NodeData to a target folder.
     * After that, notify the MainController and RecentView to do the necessary update.
     *
     * @param toBeMoved
     * @param targetFolder
     * @return
     * @throws IOException
     */
    private File moveFile(NodeData toBeMoved, File targetFolder) throws IOException {
        File fileToBeMoved = toBeMoved.getFile();
        File newFile = new File(targetFolder, toBeMoved.getName());
        if (toBeMoved.isFile()) {
            FileUtils.moveFile(fileToBeMoved, newFile);
            log.debug("File %s is moved".formatted(fileToBeMoved.getName()));
        }
        else if (toBeMoved.isFolder()) {
            FileUtils.moveDirectory(fileToBeMoved, newFile);
            log.debug("Folder %s is moved".formatted(fileToBeMoved.getName()));
        }
        EventBus.getIns().notifyFilePathChanged(toBeMoved, newFile);
        toBeMoved.setFile(newFile);
        return newFile;
    }

    /**
     * Load all workspaces to combo box to let user select.
     *
     * @param workspaceList
     */
    public void loadWorkspaces(WorkspaceList workspaceList) {
        String activeWorkspacePath = fxPreferences.getPreference(MINDOLPH_ACTIVE_WORKSPACE, String.class);
        log.debug("Last active workspace: %s".formatted(activeWorkspacePath));

        workspaceSelector.loadWorkspaces(workspaceList, new WorkspaceMeta(activeWorkspacePath), true);

        EventBus.getIns().notifyWorkspacesRestored(); // notify that all workspaces restored
    }

    /**
     * Load a workspace subtree.
     *
     * @param workspaceMeta
     */
    public void loadWorkspace(WorkspaceMeta workspaceMeta) {
        if (!workspaceMeta.exists()) {
            DialogFactory.errDialog("The workspace '%s' does not exist, it might has been moved or deleted from storage.".formatted(workspaceMeta.getName()));
            return;
        }
        EventBus.getIns().subscribeWorkspaceLoaded(1, workspaceDataTreeItem -> {
            activeWorkspaceData = workspaceDataTreeItem.getValue();
            expendedFileList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
            this.expandTreeNodes();
            Platform.runLater(() -> treeView.requestFocus());
//            GlobalExecutor.submit(() -> {
//                // NOTE the directory-watcher actually blocks the thread, so wrap it in a thread to void blocking.
                this.observeWorkspace(workspaceMeta);
//            });
        });
        this.asyncCreateWorkspaceSubTree(workspaceMeta);
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
     * Reload folder and it's subtree to specified position of the tree.
     * Select the target tree item after reloading.
     *
     * @param treeItem   tree item to load data to
     * @param folderData
     * @param select     whether select the folder tree item.
     */
    public void reloadFolder(TreeItem<NodeData> treeItem, NodeData folderData, boolean select) {
        log.debug("reload folder: %s".formatted(folderData.getFile()));
        List<NodeData> childrenData = WorkspaceManager.getIns().loadFolder(folderData, workspaceConfig);
        this.populateTreeNode(treeItem, childrenData);
        if (select) treeView.select(treeItem);
    }

    private void asyncCreateWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        GlobalExecutor.submit(() -> {
            this.createWorkspaceSubTree(workspaceMeta);
        });
    }

    private void createWorkspaceSubTree(WorkspaceMeta workspaceMeta) {
        NodeData workspaceData = new NodeData(NodeType.WORKSPACE, new File(workspaceMeta.getBaseDirPath()));
        workspaceData.setWorkspaceData(workspaceData);
        rootItem.setValue(workspaceData);
        List<NodeData> childrenData = WorkspaceManager.getIns().loadWorkspace(workspaceData, workspaceConfig);
        Platform.runLater(() -> {
            this.populateTreeNode(rootItem, childrenData);
            rootItem.getChildren().forEach(nodeDataTreeItem -> {
                NodeData folderData = nodeDataTreeItem.getValue();
                if (folderData.isFolder()) {
                    List<NodeData> grandChildrenData = WorkspaceManager.getIns().loadFolder(folderData, workspaceConfig);
                    this.populateTreeNode(nodeDataTreeItem, grandChildrenData);
                }
            });
            log.debug("workspace loaded: %s".formatted(workspaceMeta.getBaseDirPath()));
            EventBus.getIns().notifyWorkspaceLoaded(rootItem);
        });
    }

    private void observeWorkspace(WorkspaceMeta workspaceMeta) {
        Path directoryToWatch = new File(workspaceMeta.getBaseDirPath()).toPath();
        try {
            if (fileWatcher != null) {
                fileWatcher.close();
            }
            observerEventSource.reduceSuccessions((a, b) -> {
                return Stream.concat(a.stream(), b.stream()).collect((Supplier<List<File>>) ArrayList::new, (files, target) -> {
                    // collect folder if it is not subfolder of any collected.
                    if (files.isEmpty() || files.stream().noneMatch(f -> f.equals(target) || PathUtils.isParentFolder(f, target))) {
                        files.add(target);
                    }
                }, List::addAll).stream().toList();
            }, Duration.ofMillis(500)).subscribe(dirs -> {
                log.debug("reduced as %d dirs".formatted(dirs.size()));
                EventBus.getIns().notifyFolderRefreshInWorkspace(new FolderReloadEvent(dirs));
            });
            log.debug("Build file watcher...");
            // Monitor the directory of the active workspace.
            fileWatcher = DirectoryWatcher.builder()
                    .path(directoryToWatch)
                    .fileHasher(FileHasher.LAST_MODIFIED_TIME)
                    .listener(event -> {
                        Path filePath = event.path();
                        switch (event.eventType()) {
                            case CREATE:
                                log.debug("File created：%s".formatted(filePath));
                                EventBus.getIns().notifyFileChangeInWorkspace(new FileChangeEvent(FileChangeType.CREATE, filePath.toFile()));
                                observerEventSource.push(Collections.singletonList(filePath.toFile().getParentFile()));
                                break;
                            case MODIFY:
                                log.debug("File modified：%s".formatted(filePath));
//                                String msg = "File has been modified externally: %s".formatted(filePath);
//                                Platform.runLater(() -> {
//                                    Notifications.create().title("Notice").text(msg).hideAfter(Duration.seconds(60)).showInformation();
//                                });
                                break;
                            case DELETE:
                                log.debug("File deleted：%s".formatted(filePath));
                                EventBus.getIns().notifyFileChangeInWorkspace(new FileChangeEvent(FileChangeType.DELETE, filePath.toFile()));
                                observerEventSource.push(Collections.singletonList(filePath.toFile().getParentFile()));
                                break;
                        }
                    })
                    // .fileHashing(false) // defaults to true
                    // .logger(logger) // defaults to LoggerFactory.getLogger(DirectoryWatcher.class)
                    // .watchService(watchService) // defaults based on OS to either JVM WatchService or the JNA macOS WatchService
                    .build();
            log.debug("Start to watch...");
            CompletableFuture<Void> fileWatcherFuture = fileWatcher.watchAsync();
            log.debug("File watcher started for workspace: " + workspaceMeta.getBaseDirPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param parent       The tree node populates children to
     * @param childrenData
     */
    private void populateTreeNode(TreeItem<NodeData> parent, List<NodeData> childrenData) {
        // note: use name other than file path to match tree item and node data, because the file path changes when folder name changes.
        log.trace("load folder item: %s/".formatted(parent));
        // remove doesn't exist tree items for theirs file might be deleted.
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
                if (log.isTraceEnabled()) log.trace("already exists, ignore: %s".formatted(childNodeData));
                TreeItem<NodeData> existingItem = existing.get();
                if (log.isTraceEnabled())
                    log.trace("existing tree item: %s, expanded: %s".formatted(existingItem.getValue().getFile(), existingItem.isExpanded()));
                existingItem.setValue(childNodeData);
                if (childNodeData.isFolder() && existingItem.isExpanded()) {
                    populateTreeNode(existingItem, WorkspaceManager.getIns().loadFolder(childNodeData, workspaceConfig));
                }
                continue;
            }
            if (log.isTraceEnabled()) log.trace("%s does not existed, try to create it.".formatted(childNodeData));
            if (childNodeData.isFolder()) {
                TreeItem<NodeData> folderItem = this.addFolder(parent, childNodeData);
                if (log.isTraceEnabled()) log.trace("add folder: %s/".formatted(folderItem.getValue().getName()));
                if (parent.isExpanded()) {
                    // this implements the case that re-load a folder whose parent is expanded and new subfiles created externally.
                    // without this implementation, the folder will be empty.
                    populateTreeNode(folderItem, WorkspaceManager.getIns().loadFolder(childNodeData, workspaceConfig));
                }
            }
            else if (childNodeData.isFile()) {
                TreeItem<NodeData> fileItem = this.addFile(parent, childNodeData);
                if (log.isTraceEnabled()) log.trace("add file: %s".formatted(fileItem.getValue().getName()));
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

    public TreeItem<NodeData> addFolderAndSelect(TreeItem<NodeData> parent, NodeData folderData) {
        TreeItem<NodeData> folderItem = this.addFolder(parent, folderData);
        treeView.reselect(folderItem);
        return folderItem;
    }

    public TreeItem<NodeData> addFileAndSelect(TreeItem<NodeData> parent, NodeData fileData) {
        TreeItem<NodeData> treeItem = this.addFile(parent, fileData);
        treeView.reselect(treeItem);
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
            NodeData nodeData = selectedValue.get();
            if (nodeData.isFile()) {
                if (!nodeData.getFile().exists()) {
                    DialogFactory.errDialog("File doesn't exist, it might be deleted or moved externally.");
                    removeTreeNode(nodeData, true);
                    EventBus.getIns().notifyDeletedFile(nodeData);
                    return;
                }
                log.info("Open file: %s".formatted(nodeData.getFile()));
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(nodeData.getFile()));
            }
        }
    }

    private void openSelectedFiles() {
        List<NodeData> selectedItemsData = treeView.getSelectedItemsData();
        for (NodeData snd : selectedItemsData) {
            if (snd.isFile()) {
                if (!snd.getFile().exists()) {
                    removeTreeNode(snd, true);
                    EventBus.getIns().notifyDeletedFile(snd);
                    continue;
                }
                log.info("Open file: %s".formatted(snd.getFile()));
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(snd.getFile()));
            }
        }
    }


    private ContextMenu createItemContextMenu(TreeItem<NodeData> treeItem) {
        ContextMenu contextMenu = new ContextMenu();

        boolean isSingleSelected = treeView.isSingleSelected();
        if (treeItem != null) {
            log.debug("create context menu for item: %s".formatted(treeItem.getValue().getName()));
            NodeData nodeData = treeItem.getValue();
            boolean isFolder = !nodeData.isFile();
            boolean isFile = nodeData.isFile();
            if (isFolder && isSingleSelected) {
                Menu miNew = createMenuNew();
                contextMenu.getItems().add(miNew);
            }
            if (isFile) {
                miOpenFile = new MenuItem("Open");
                miOpenFile.setMnemonicParsing(false);
                miOpenFile.setOnAction(this);
                contextMenu.getItems().add(miOpenFile);

                Menu miCopy = new Menu("Copy");
                miCopyFile = new MenuItem("File(s)", FontIconManager.getIns().getIcon(IconKey.FILE));
                miCopyPathAbsolute = new MenuItem("Absolute Path: " + StringUtils.abbreviateMiddle(treeItem.getValue().getFile().getPath(), "...", 32));
                miCopyPathRelative = new MenuItem("Relative Path: " + StringUtils.abbreviateMiddle(treeItem.getValue().getFileRelativePath(), "...", 32));
                miCopyPathAbsolute.setMnemonicParsing(false);
                miCopyPathRelative.setMnemonicParsing(false);
                miCopyFile.setOnAction(this);
                miCopyPathAbsolute.setOnAction(this);
                miCopyPathRelative.setOnAction(this);
                miCopy.getItems().addAll(miCopyFile, miCopyPathAbsolute, miCopyPathRelative);
                contextMenu.getItems().add(miCopy);
            }
            if (isFolder && isSingleSelected) {
                miPasteFile = new MenuItem("Paste");
                miPasteFile.setDisable(!Clipboard.getSystemClipboard().hasFiles());
                miPasteFile.setOnAction(this);
                contextMenu.getItems().add(miPasteFile);
            }
            if (isSingleSelected) {
                miRename = new MenuItem("Rename...", FontIconManager.getIns().getIcon(IconKey.RENAME));
                miRename.setOnAction(this);
                contextMenu.getItems().addAll(miRename);
            }
            miMoveTo = new MenuItem("Move to...", FontIconManager.getIns().getIcon(IconKey.MOVE_FOLDER));
            miMoveTo.setOnAction(this);
            contextMenu.getItems().add(miMoveTo);
            if (isFolder && isSingleSelected) {
                miReload = new MenuItem("Reload", FontIconManager.getIns().getIcon(IconKey.REFRESH));
                miReload.setOnAction(this);
                contextMenu.getItems().addAll(miReload);
            }
            else if (nodeData.isFile() && isSingleSelected) {
                miClone = new MenuItem("Clone", FontIconManager.getIns().getIcon(IconKey.CLONE));
                miClone.setOnAction(this);
                contextMenu.getItems().add(miClone);
            }
            miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
            miDelete.setOnAction(this);
            contextMenu.getItems().addAll(miDelete);
            if (!nodeData.isMindMap() && isSingleSelected) {
                miOpenInSystem = new MenuItem("Open in System", FontIconManager.getIns().getIcon(IconKey.SYSTEM));
                miOpenInSystem.setOnAction(this);
                contextMenu.getItems().add(miOpenInSystem);
            }
            if (isFolder && isSingleSelected) {
                miCollapseAll = new MenuItem("Collapse All", FontIconManager.getIns().getIcon(IconKey.COLLAPSE_FOLDERS));
                miCollapseAll.setOnAction(this);
                miFindFiles = new MenuItem("Find in Files...", FontIconManager.getIns().getIcon(IconKey.SEARCH));
                miFindFiles.setOnAction(this);
                contextMenu.getItems().addAll(miCollapseAll, new SeparatorMenuItem(), miFindFiles);
            }
            if (isSingleSelected) {
                miUsage = new MenuItem("Find Usage");
                miUsage.setOnAction(this);
                contextMenu.getItems().add(miUsage);
            }
        }
        return contextMenu;
    }

    private Menu createMenuNew() {
        Menu miNew = new Menu("New");
        MenuItem miFolder = new MenuItem("Folder", FontIconManager.getIns().getIcon(IconKey.FOLDER));
        MenuItem miMindMap = new MenuItem("Mind Map(.mmd)", FontIconManager.getIns().getIcon(IconKey.FILE_MMD));
        MenuItem miMarkdown = new MenuItem("Markdown(.md)", FontIconManager.getIns().getIcon(IconKey.FILE_MD));
        Menu plantUmlMenu = new Menu("PlantUML(.puml)", FontIconManager.getIns().getIcon(IconKey.FILE_PUML));
        MenuItem miCsvFile = new MenuItem("Sheet(.csv)", FontIconManager.getIns().getIcon(IconKey.FILE_CSV));
        for (Template template : PlantUmlTemplates.getIns().getTemplates()) {
            MenuItem mi = new MenuItem(template.getTitle());
            mi.setUserData(template);
            mi.setOnAction(this);
            plantUmlMenu.getItems().add(mi);
        }
        MenuItem miTextFile = new MenuItem("Text(.txt)", FontIconManager.getIns().getIcon(IconKey.FILE_TXT));
        miFolder.setUserData(TYPE_FOLDER);
        miMindMap.setUserData(TYPE_MIND_MAP);
        miMarkdown.setUserData(TYPE_MARKDOWN);
        plantUmlMenu.setUserData(TYPE_PLANTUML);
        miCsvFile.setUserData(TYPE_CSV);
        miTextFile.setUserData(TYPE_PLAIN_TEXT);

        miFolder.setOnAction(this);
        miMindMap.setOnAction(this);
        miMarkdown.setOnAction(this);
        miTextFile.setOnAction(this);
        miCsvFile.setOnAction(this);
        miNew.getItems().addAll(miFolder, miMindMap, miMarkdown, plantUmlMenu, miTextFile, miCsvFile);
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
            if (workspaceMeta == null) {
                log.debug("The file of this node does not belongs to any workspace");
                return;
            }
            if (workspaceMeta.getBaseDirPath().equals(activeWorkspaceData.getFile().getPath())) {
                // the current workspace is the target workspace
                this.selectByNodeData(nodeData);
                treeView.scrollToSelected();
            }
            else {
                // switch to the target workspace before searching
                EventBus.getIns().subscribeWorkspaceLoaded(1, nodeDataTreeItem -> {
                    this.selectByNodeData(nodeData);
                    treeView.scrollToSelected();
                });
                log.debug("Select workspace: %s".formatted(workspaceMeta.getBaseDirPath()));
                workspaceSelector.select(workspaceMeta);
            }
        }
    }

    /**
     * Find and select a tree item by its node data and expand its path nodes.
     *
     * @param nodeData
     * @return TreeItem if selected.
     */
    private TreeItem<NodeData> selectByNodeData(NodeData nodeData) {
        if (nodeData != null) {
            log.debug("Select in tree: %s".formatted(nodeData));
            return TreeVisitor.dfsSearch(rootItem, treeItem -> {
                NodeData curNodeData = treeItem.getValue();
                if (curNodeData.getFile().equals(nodeData.getFile())) {
                    log.debug("Found tree item to select");
                    treeItem.setExpanded(true);
                    treeView.reselect(treeItem);
                    return treeItem; // stop traversing
                }
                if (!treeItem.isLeaf()) {
                    if (curNodeData.isParentOf(nodeData)) {
                        treeItem.setExpanded(true);
                    }
                    else {
                        // stop traversing deeper for unnecessary searching
                        log.debug("Not traversing deeper for the tree item is not parent of target node");
                        return null;
                    }
                }
                return new TreeItem<>(); // keep traversing
            });
        }
        return null;
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
     * Handle tree node expansion and collapse and call outer listener.
     *
     * @param expanded
     * @param treeItem
     */
    private void onTreeItemExpandOrCollapsed(Boolean expanded, TreeItem<NodeData> treeItem) {
        if (expanded) {
            EventBus.getIns().notifyTreeExpandCollapse(treeItem, true);
            // if expanded, preload all children of each child of this tree item.
            for (TreeItem<NodeData> child : treeItem.getChildren()) {
                File childFile = child.getValue().getFile();
                if (childFile.isDirectory()) {
                    List<NodeData> childrenOfChild = WorkspaceManager.getIns().loadFolder(child.getValue(), workspaceConfig);
                    this.populateTreeNode(child, childrenOfChild);
                    // expand the child node if it should be restored to expand.
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
            String snippet = template.getContent().formatted(TimeUtils.createTimestamp(), FilenameUtils.getBaseName(newFile.getName()));
            FileUtils.writeStringToFile(newFile, snippet, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Handle events on a node of the tree view.
     *
     * @param event
     */
    @Override
    public void handle(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        NodeData selectedData = selectedTreeItem.getValue(); // use selected tree item as target even for workspace folder(the root item), because the user data of tree item might be used for the other purpose.
        List<NodeData> selectedNodes = treeView.getSelectedItemsData();
        if (selectedData == null || selectedData.getFile() == null) {
            return;
        }
        String fileType = String.valueOf(source.getUserData() instanceof Template ? source.getParentMenu().getUserData() : source.getUserData());
        if (TYPE_FOLDER.equals(fileType)) {
            this.requestNewFolder(selectedData, newDir -> {
                selectedTreeItem.setExpanded(true);
                NodeData folderData = new NodeData(NodeType.FOLDER, newDir);
                folderData.setWorkspaceData(selectedData.getWorkspaceData());
                addFolderAndSelect(selectedTreeItem, folderData);
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(newDir, true));
            });
        }
        else if (Strings.CS.equalsAny(fileType, TYPE_MIND_MAP, TYPE_MARKDOWN, TYPE_CSV, TYPE_PLANTUML, TYPE_PLAIN_TEXT)) {
            this.requestNewFile(source, selectedData, fileType, newFile -> {
                selectedTreeItem.setExpanded(true);
                NodeData nodeData = new NodeData(newFile);
                nodeData.setWorkspaceData(selectedData.getWorkspaceData());
                this.addFileAndSelect(selectedTreeItem, nodeData);
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(newFile));
            });
        }
        else if (source == miOpenFile) {
            this.openSelectedFiles();
        }
        else if (source == miCopyFile) {
            ClipBoardUtils.filesToClipboard(selectedNodes.stream().map(NodeData::getFile).toList());
        }
        else if (source == miCopyPathAbsolute) {
            ClipBoardUtils.textToClipboard(selectedNodes.stream().map(nodeData -> nodeData.getFile().getAbsolutePath()).collect(Collectors.joining(LINE_SEPARATOR)));
        }
        else if (source == miCopyPathRelative) {
            ClipBoardUtils.textToClipboard(selectedNodes.stream().map(NodeData::getFileRelativePath).collect(Collectors.joining(LINE_SEPARATOR)));
        }
        else if (source == miPasteFile) {
            File file = ClipBoardUtils.filesFromClipboard().getFirst();
            String cloneFileName = "%s_copy.%s".formatted(FilenameUtils.getBaseName(file.getName()), FilenameUtils.getExtension(file.getName()));
            this.copyFile(selectedTreeItem, file, cloneFileName);
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
                    this.reloadFolder(selectedTreeItem, newFolderData, true);
                }
                treeView.refresh();
                // remove the old path from the expanded list as well.
                SceneRestore.getInstance().removeFromExpandedList(selectedData.getFile().getPath());
                EventBus.getIns().notifyFilePathChanged(selectedData, newNameFile);
            });
        }
        else if (source == miMoveTo) {
            Optional<NodeData> optSelected = treeView.getSelectedData();
            if (optSelected.isPresent()) {
                WorkspaceMeta meta = new WorkspaceMeta(activeWorkspaceData.getFile().getPath());
                WorkspaceDialog workspaceDialog = new WorkspaceDialog("Move selected file(s) to",
                        WorkspaceManager.getIns().getWorkspaceList(), meta);
                WorkspaceDialog.Selection selection = workspaceDialog.showAndWait();
                if (selection != null) {
                    boolean isSameWorkspace = selection.workspaceMeta().contains(activeWorkspaceData.getFile());
                    File targetFolder = selection.folderPath();
                    List<NodeData> toBeMoved = treeView.getSelectedItemsData();
                    EventBus.getIns().subscribeWorkspaceLoaded(1, rootTreeItem -> {
                        log.debug("Move %d files to %s".formatted(toBeMoved.size(), targetFolder));
                        TreeItem<NodeData> targetTreeItem = FileTreeHelper.findTreeItemByFile(rootItem, targetFolder);
                        if (targetTreeItem == null) {
                            // no target tree item found means workspace root has been selected.
                            targetTreeItem = rootTreeItem;
                        }
                        List<File> movedFiles = this.moveToTreeItem(toBeMoved, targetTreeItem);
                        if (movedFiles != null) {
                            treeView.scrollToSelected();
                        }
                    });

                    if (targetFolder != null && targetFolder.isDirectory()) {
                        log.debug(targetFolder.getPath());
                        if (isSameWorkspace) {
                            // utilize the workspace loaded event to execute files movement.
                            EventBus.getIns().notifyWorkspaceLoaded(rootItem);
                        }
                        else {
                            log.debug("Switch workspace if not the same with current one");
                            workspaceSelector.select(selection.workspaceMeta());
                        }
                    }
                    else {
                        log.warn("Illegal target folder: %s".formatted(targetFolder));
                    }
                }
            }
        }
        else if (source == miClone) {
            this.requestCloneFile(selectedData, newFileName -> {
                File file = selectedData.getFile();
                String ext = FilenameUtils.getExtension(file.getName());
                this.copyFile(selectedTreeItem.getParent(), file, "%s.%s".formatted(newFileName, ext));
            });
        }
        else if (source == miDelete) {
            try {
                for (NodeData sn : selectedNodes) {
                    if (sn.getFile().isDirectory() && !isFolderEmpty(sn.getFile())) {
                        DialogFactory.errDialog("The folder you want to delete is not empty");
                        return;
                    }
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
                return;
            }
            String summary = "Are you sure to delete %s".formatted(
                    selectedNodes.size() == 1 ? selectedData.getName() : "%d selected files".formatted(selectedNodes.size())
            );
            Boolean needDelete = new ConfirmDialogBuilder().positive("Delete").cancel().asDefault().content(summary).showAndWait();
            if (needDelete != null && needDelete) {
                for (TreeItem<NodeData> curItem : List.copyOf(treeView.getSelectionModel().getSelectedItems())) {
                    NodeData sn = curItem.getValue();
                    if (!this.beforeFilePathChanged(sn)) {
                        log.debug("Cancel deleting file");
                        return;
                    }
                    log.info("Delete file '%s' attached with %s".formatted(sn.getFile(), curItem));
                    if (sn.getFile().isDirectory()) {
                        deleteMacFile(sn.getFile());
                    }
                    try {
                        FileUtils.delete(sn.getFile());
                    } catch (IOException e) {
                        log.error(e.getLocalizedMessage(), e);
                        DialogFactory.errDialog("Delete file failed: " + e.getLocalizedMessage());
                        return;
                    }
                    // note: the file change monitor will remove tree item, so don't do it here.
                    // curItem.getParent().getChildren().remove(curItem);
                    // close opened file tab(if exists) and remove from the recent list if exists
                    EventBus.getIns().notifyDeletedFile(sn);
                }
                treeView.refresh();
                treeView.getSelectionModel().clearSelection();
            }
        }
        else if (source == miReload) {
            this.reloadFolder(selectedTreeItem, selectedData, true);
//            // activate the folder loading by re-expanding the folder.
//            if (selectedTreeItem.isExpanded()) {
//                selectedTreeItem.setExpanded(false);
//                selectedTreeItem.setExpanded(true);
//            }
        }
        else if (source == miFindFiles) {
            this.launchFindInFilesDialog(selectedData);
        }
        else if (source == miUsage) {
            this.launchUsageDialog(selectedData);
        }
        else if (source == miOpenInSystem) {
            log.info("Try to open file: %s".formatted(selectedData.getFile()));
            if (selectedData.isMindMap()) {
                this.openSelectedFile(); // always open mmd file in Mindolph
            }
            else {
                MindolphFileUtils.openFileInSystem(selectedData.getFile());
            }
        }
        else if (source == miCollapseAll) {
            treeView.collapseTreeNodes(treeView.getSelectionModel().getSelectedItem(), true);
        }
        else {
            log.debug("Unknown event source: %s".formatted(source));
        }
    }

    private void requestCloneFile(NodeData selectedData, Consumer<String> consumer) {
        String oldFileName = selectedData.isFile() ? FilenameUtils.getBaseName(selectedData.getFile().getPath()) : selectedData.getName();
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Clone %s".formatted(selectedData.getName()))
                .content("Input a new name")
                .text("%s_copy".formatted(oldFileName))
                .width(400)
                .build();
        Optional<String> optNewName = dialog.showAndWait();
        if (optNewName.isPresent()) {
            String newName = optNewName.get();

            // TODO allow separator if folder is reloaded automatically.
            if (FileNameUtils.containsSeparator(newName)) {
                // let user try again
                DialogFactory.errDialog("Please input valid folder name");
                this.requestCloneFile(selectedData, consumer);
                return;
            }

            if (selectedData.isFile()) {
                consumer.accept(newName);
            }
        }
    }

    private void requestNewFolder(NodeData selectedData, Consumer<File> consumer) {
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("New Folder Name")
                .width(300)
                .build();
        dialog.setGraphic(FontIconManager.getIns().getIconForFile(TYPE_FOLDER, 32));
        Optional<String> opt = dialog.showAndWait();
        if (opt.isPresent()) {
            String folderName = opt.get();

            // TODO allow separator if folder is reloaded automatically.
            if (FileNameUtils.containsSeparator(folderName)) {
                // let user try again
                DialogFactory.errDialog("Please input valid folder name");
                this.requestNewFolder(selectedData, consumer);
                return;
            }

            if (selectedData.getFile().isDirectory()) {
                File newDir = new File(selectedData.getFile(), folderName);
                if (newDir.mkdir()) {
                    consumer.accept(newDir);
                }
            }
        }
    }

    private void requestNewFile(MenuItem source, NodeData selectedData, String fileType, Consumer<File> consumer) {
        log.debug("New %s File".formatted(source.getText()));
        log.debug("source: %s from %s".formatted(source.getText(), source.getParentMenu() == null ? "" : source.getParentMenu().getText()));
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .width(300)
                .title("New %s Name".formatted(source.getText())).build();
        String iconType = String.valueOf(source.getUserData() instanceof Template ? source.getParentMenu().getUserData() : source.getUserData());
        dialog.setGraphic(FontIconManager.getIns().getIconForFile(iconType, 32));
        Optional<String> opt = dialog.showAndWait();
        if (opt.isPresent()) {
            String fileName = opt.get();

            // TODO allow separator if folder is reloaded automatically.
            if (FileNameUtils.containsSeparator(fileName)) {
                // let user try again
                DialogFactory.errDialog("Please input valid file name");
                this.requestNewFile(source, selectedData, fileType, consumer);
                return;
            }

            if (selectedData.getFile().isDirectory()) {
                File newFile = null;
                if (TYPE_MIND_MAP.equals(fileType)) {
                    newFile = createEmptyFile(fileName, selectedData, "mmd");
                    if (newFile != null) {
                        final MindMap<TopicNode> mindMap = new MindMap<>();
                        Boolean addDefaultComment = fxPreferences.getPreference(PrefConstants.PREF_KEY_MMD_ADD_DEF_COMMENT_TO_ROOT, true);
                        TopicNode rootTopic;
                        if (addDefaultComment) {
                            ExtraNote extraNote = new ExtraNote(this.createDefaultNote());
                            rootTopic = new TopicNode(mindMap, null, FilenameUtils.getBaseName(newFile.getPath()), extraNote);
                        }
                        else {
                            rootTopic = new TopicNode(mindMap, null, FilenameUtils.getBaseName(newFile.getPath()));
                        }
                        mindMap.setRoot(rootTopic);
                        final String text;
                        try (Writer w = new StringWriter()) {
                            text = mindMap.write(w).toString();
                            FileUtils.writeStringToFile(newFile, text, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            log.error(e.getLocalizedMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                }
                else if (TYPE_PLAIN_TEXT.equals(fileType)) {
                    newFile = createEmptyFile(fileName, selectedData, "txt");
                }
                else if (TYPE_PLANTUML.equals(fileType)) {
                    log.debug("Handle dynamic menu item: %s".formatted(source.getText()));
                    newFile = createEmptyFile(fileName, selectedData, "puml");
                    if (newFile != null) {
                        this.handlePlantumlCreation(source, newFile);
                    }
                }
                else if (TYPE_MARKDOWN.equals(fileType)) {
                    newFile = createEmptyFile(fileName, selectedData, "md");
                    if (newFile != null) {
                        String baseFileName = FilenameUtils.getBaseName(newFile.toString());
                        String title = RegExUtils.replaceAll(baseFileName, "[+=_`~&@,\\-\\<\\>\\.\\?\\^#\\$\\(\\)]+", " ");
                        String snippet = Templates.MARKDOWN_TEMPLATE.formatted(title, TimeUtils.createTimestamp());
                        try {
                            FileUtils.writeStringToFile(newFile, snippet, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            log.error(e.getLocalizedMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                }
                else if (TYPE_CSV.equals(fileType)) {
                    newFile = createEmptyFile(fileName, selectedData, "csv");
                }
                else {
                    log.warn("Not supported file type?");
                    return;
                }
                // add the new file to tree view
                if (newFile != null && newFile.exists()) {
                    consumer.accept(newFile);
                }
            }
        }
    }

    /**
     * Copy file to target folder of tree item with existence check.
     *
     * @param folderTreeItem
     * @param file
     * @param newFileName
     */
    private void copyFile(TreeItem<NodeData> folderTreeItem, File file, String newFileName) {
        File targetDir = folderTreeItem.getValue().getFile();
//        String cloneFileName = "%s_copy.%s".formatted(FilenameUtils.getBaseName(file.getName()), FilenameUtils.getExtension(file.getName()));
        File cloneFile = new File(targetDir, newFileName);
        if (cloneFile.exists()) {
            DialogFactory.errDialog("File %s already exists".formatted(newFileName));
            return;
        }
        try {
            FileUtils.copyFile(file, cloneFile);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            DialogFactory.errDialog("Clone file failed: " + e.getLocalizedMessage());
            return;
        }
        NodeData newFileData = new NodeData(cloneFile);
        newFileData.setWorkspaceData(activeWorkspaceData);
        addFile(folderTreeItem, newFileData);
        treeView.refresh();
    }

    private void requestRenameFolderOrFile(NodeData selectedData, Consumer<File> consumer) {
        if (!this.beforeFilePathChanged(selectedData)) {
            log.debug("Cancel renaming file");
            return;
        }
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Rename %s".formatted(selectedData.getName()))
                .content("Input a new name")
                .text(selectedData.isFile() ? FilenameUtils.getBaseName(selectedData.getFile().getPath()) : selectedData.getName())
                .width(400)
                .build();
        dialog.setGraphic(FontIconManager.getIns().getIconForFile(selectedData, 32));
        Optional<String> s = dialog.showAndWait();
        if (s.isPresent()) {
            String newName = s.get();
            File origFile = selectedData.getFile();
            if (selectedData.isFile()) {
                newName = FileNameUtils.appendFileExtensionIfAbsent(newName, FilenameUtils.getExtension(origFile.getPath()));
            }
            if (FileNameUtils.containsSeparator(newName)) {
                // let user try input again.
                DialogFactory.errDialog("Please input valid file name");
                this.requestRenameFolderOrFile(selectedData, consumer);
                return;
            }
            File newNameFile = new File(origFile.getParentFile(), newName);
            if (newNameFile.exists()) {
                DialogFactory.errDialog("Folder or file %s already exists".formatted(newName));
                this.requestRenameFolderOrFile(selectedData, consumer);
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
        if (selectedData != null && !selectedData.isFile()) {
            if (!selectedData.getFile().exists()) {
                DialogFactory.errDialog("The workspace or folder you selected doesn't exist, probably be deleted externally.");
            }
            else {
                SearchParams searchParams = new FindInFilesDialog(selectedData.getWorkspaceData().getFile(), selectedData.getFile()).showAndWait();
                if (searchParams != null && StringUtils.isNotBlank(searchParams.getKeywords())) {
                    IOFileFilter searchFilter = workspaceConfig.makeFileFilter();
                    searchParams.setWorkspaceDir(selectedData.getWorkspaceData().getFile());
                    searchParams.setSearchFilter(searchFilter);
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_KEYWORD, searchParams.getKeywords());
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_CASE_SENSITIVITY, searchParams.isCaseSensitive());
                    fxPreferences.savePreference(MINDOLPH_FIND_FILES_OPTIONS, searchParams.getFileTypeName());
                    searchEventSource.push(searchParams);
                }
            }
        }
    }

    private void launchUsageDialog(NodeData selectedData) {
        if (selectedData.isFolder() || selectedData.isFile()) {
            File workspaceDir = selectedData.getWorkspaceData().getFile();
            SearchParams searchParams = new SearchParams();
            searchParams.setKeywords(PathUtils.getRelativePath(selectedData.getFile(), workspaceDir));
            searchParams.setSearchFilter(workspaceConfig.makeFileFilter());
            searchParams.setSearchInDir(workspaceDir);
            searchParams.setWorkspaceDir(workspaceDir);
            new UsageDialog(searchParams).showAndWait();
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
     * Find first parent TreeItem matches the class type of data object for {@code treeItem} recursively.
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

    public void removeTreeNode(NodeData nodeData, boolean safely) {
        TreeItem<NodeData> selectedTreeItem = treeView.getSelectedTreeItem();
        if (safely) {
            if (selectedTreeItem.getValue() != nodeData) {
                return;
            }
        }
        treeView.removeTreeItem(selectedTreeItem);
        treeView.refresh();
    }

    private boolean beforeFilePathChanged(NodeData nodeData) {
        SearchParams searchParams = new SearchParams();
        File workspaceDir = nodeData.getWorkspaceData().getFile();
        searchParams.setKeywords(PathUtils.getRelativePath(nodeData.getFile(), workspaceDir));
        searchParams.setSearchFilter(new WorkspaceConfig().makeFileFilter());
        searchParams.setSearchInDir(workspaceDir);
        searchParams.setWorkspaceDir(workspaceDir);
        return this.searchFileReferences(searchParams);
    }

    private boolean searchFileReferences(SearchParams searchParams) {
        log.debug("reSearch()");
        IOFileFilter newFileFilter = searchParams.getSearchFilter();
        foundFiles = SearchService.getIns().searchLinksInFilesIn(searchParams.getSearchInDir(), newFileFilter, searchParams);
        if (CollectionUtils.isNotEmpty(foundFiles)) {
            FileReferenceDialog dialog = new FileReferenceDialog(searchParams, foundFiles);
            dialog.showAndWait();
            return !dialog.isNegative();
        }
        return true;
    }

    @Override
    public void requestFocus() {
        treeView.scrollToSelected();
        treeView.requestFocus();
    }

    public void subscribeSearchEvent(Consumer<SearchParams> consumer) {
        this.searchEventSource.subscribe(consumer);
    }

    private String createDefaultNote() {
        return "This file is created by Mindolph at " + TimeUtils.createTimestamp();
    }


}
