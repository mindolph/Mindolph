package com.mindolph.fx;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.BaseView;
import com.mindolph.core.Env;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.collection.CollectionManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.container.FixedSplitPane;
import com.mindolph.base.control.snippet.SnippetView;
import com.mindolph.base.editor.Editable;
import com.mindolph.base.editor.ImageViewerEditor;
import com.mindolph.base.editor.PlainTextEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.EventBus.MenuTag;
import com.mindolph.base.event.FileChangedEventHandler;
import com.mindolph.base.event.NotificationType;
import com.mindolph.base.event.WorkspaceViewResizedEventHandler;
import com.mindolph.base.print.PrinterManager;
import com.mindolph.core.constant.NodeType;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.util.MavenUtils;
import com.mindolph.core.util.ReleaseUtils;
import com.mindolph.fx.dialog.*;
import com.mindolph.fx.helper.OpenedFileRestoreListener;
import com.mindolph.fx.helper.SceneRestore;
import com.mindolph.fx.helper.WorkspaceRestoreListener;
import com.mindolph.fx.helper.WorkspaceViewSizeRestoreListener;
import com.mindolph.fx.print.ImagePrintable;
import com.mindolph.fx.print.MindMapPrintable;
import com.mindolph.fx.print.PrintPreviewDialog;
import com.mindolph.fx.print.Printable;
import com.mindolph.fx.view.*;
import com.mindolph.markdown.MarkdownEditor;
import com.mindolph.mfx.BaseController;
import com.mindolph.mfx.dialog.ConfirmDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.DesktopUtils;
import com.mindolph.mindmap.MindMapEditor;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.plantuml.PlantUmlEditor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.mindolph.core.constant.SceneStatePrefs.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.length;

/**
 * @author mindolph.com@gmail.com
 */
public class MainController extends BaseController implements Initializable,
        WorkspaceRestoreListener, OpenedFileRestoreListener,
        FileChangedEventHandler, WorkspaceViewSizeRestoreListener {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    private final CollectionManager cm = CollectionManager.getIns();

    private final Map<Tab, BaseView> tabViewMap = new HashMap<>();

    @FXML
    private FileTabView fileTabView;
    @FXML
    private FixedSplitPane splitPane;
    @FXML
    private WorkspaceViewEditable workspaceView;
    @FXML
    private RecentView recentView;
    @FXML
    private OutlineView outlineView;
    @FXML
    private SnippetView snippetView;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuRecentWorkspaces;
    @FXML
    private Menu menuCollections;
    @FXML
    private MenuItem menuManageWorkspaces;
    @FXML
    private TabPane leftTabPane;
    @FXML
    private Tab tabWorkspaces;
    @FXML
    private Tab tabRecentFiles;
    @FXML
    private Tab tabOutline;
    @FXML
    private Tab tabSnippet;
    @FXML
    private CheckMenuItem miToggleWorkspaceView;
    @FXML
    private MenuItem miUndo;
    @FXML
    private MenuItem miRedo;
    @FXML
    private MenuItem miCut;
    @FXML
    private MenuItem miCopy;
    @FXML
    private MenuItem miPaste;
    //    @FXML
//    private MenuItem miNewFile;
    //    @FXML
//    private MenuItem miOpenFile;
    @FXML
    private MenuItem miSave;
    @FXML
    private MenuItem miSaveAs;
    @FXML
    private MenuItem miSaveAll;
    //    @FXML
//    private MenuItem miPrint;
    @FXML
    private MenuItem miCloseTab;
    @FXML
    private MenuItem miFind;
    @FXML
    private MenuItem miReplace;
    @FXML
    private MenuItem miRemoveCollection;

    private RadioMenuItem rmiCollectionDefault;

    private ToggleGroup collectionToggleGroup;

    private WorkspaceList workspaceList;

    private WorkspaceViewResizedEventHandler workspaceViewResizedEventHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initialize main scene controller");
        menuBar.setUseSystemMenuBar(true);

        // handle the file collections.
        this.loadCollections();

        SceneRestore sceneRestore = SceneRestore.getInstance();

        // register state changes to store.
        workspaceViewResizedEventHandler = sceneRestore;
        tabWorkspaces.setGraphic(FontIconManager.getIns().getIcon(IconKey.WORKSPACE));
        tabRecentFiles.setGraphic(FontIconManager.getIns().getIcon(IconKey.RECENT_LIST));
        tabOutline.setGraphic(FontIconManager.getIns().getIcon(IconKey.OUTLINE));
        tabSnippet.setGraphic(FontIconManager.getIns().getIcon(IconKey.SNIPPET));

        EventBus.getIns().subscribeOpenFile(openFileEvent -> onOpenFile(openFileEvent.getNodeData(), openFileEvent.getSearchParams(), openFileEvent.isVisibleInWorkspace()));
        workspaceView.subscribeSearchEvent(this::onSearchStart);
        EventBus.getIns().subscribeWorkspaceRenamed(event -> {
            onFileRenamed(new NodeData(NodeType.WORKSPACE, new File(event.getOriginal().getBaseDirPath())), new File(event.getTarget().getBaseDirPath()));
        }).subscribeFilePathChanged(filePathChangedEvent -> {
            onFileRenamed(filePathChangedEvent.getNodeData(), filePathChangedEvent.getNewFile());
        });
        // listen restore events
        sceneRestore.setWorkspacesRestoreListener(this);
        sceneRestore.setOpeningFileRestoreListener(this);
        sceneRestore.setWorkspaceViewSizeRestoreListener(this);

        // handle double click on file tabs.
        EventBus.getIns().subscribe(notificationType -> {
            if (notificationType == NotificationType.DOUBLE_CLICKED_TAB) {
                if (splitPane.isPrimaryHidden()) {
                    splitPane.showAll();
                    miToggleWorkspaceView.setSelected(true);
                }
                else {
                    splitPane.hidePrimary();
                    miToggleWorkspaceView.setSelected(false);
                }
                fileTabView.requestFocusOnCurrentEditor();
            }
        }).subscribe(notificationType -> {
            if (notificationType == NotificationType.NEW_WORKSPACE) // not used but kept for later
                this.onMenuNewWorkspace();
        });

        splitPane.setFixed(splitPane.getPrimary());

        this.initRecentWorkspacesMenu();

        EventBus.getIns().subscribeMenuStateChange(MenuTag.UNDO, enable -> miUndo.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.REDO, enable -> miRedo.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.CUT, enable -> miCut.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.COPY, enable -> miCopy.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.PASTE, enable -> miPaste.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.SAVE, enable -> miSave.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.SAVE_AS, enable -> miSaveAs.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.SAVE_ALL, enable -> miSaveAll.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.CLOSE_TAB, enable -> miCloseTab.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.FIND, enable -> miFind.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.REPLACE, enable -> miReplace.setDisable(!enable))
                .subscribeMenuStateChange(MenuTag.REMOVE_COLLECTION, enable -> miRemoveCollection.setDisable(!enable));
//        EventBus.getIns().subscribeMenuStateChange(MenuTag.PRINT, enable -> miPrint.setDisable(!enable))
//        EventBus.getIns().subscribeMenuStateChange(MenuTag.NEW_FILE, enable -> miNewFile.setDisable(!enable));
//        EventBus.getIns().subscribeMenuStateChange(MenuTag.OPEN_FILE, enable -> miOpenFile.setDisable(!enable))

        EventBus.getIns().subscribeLocateInWorkspace(nodeData -> {
            splitPane.showAll(); // show project view if hidden
            log.debug("Select file in workspace view: %s".formatted(nodeData.getFile()));
            Platform.runLater(() -> {
                tabWorkspaces.getTabPane().getSelectionModel().select(tabWorkspaces);
                workspaceView.selectByNodeDataInAppropriateWorkspace(nodeData);
                // force to scroll and focus if current workspace is target workspace.
                workspaceView.requestFocus();
            });
        });

        EventBus.getIns().subscribePreferenceChanged(fileType -> fileTabView.reloadEditorsByType(fileType));

        tabViewMap.put(tabOutline, outlineView);
        tabViewMap.put(tabWorkspaces, workspaceView);
        tabViewMap.put(tabRecentFiles, recentView);
        tabViewMap.put(tabSnippet, snippetView);
        leftTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedTab) -> {
            if (selectedTab == tabRecentFiles) {
                // load recent files only at the first time
                if (!recentView.hasData()) {
                    recentView.load();
                }
            }
            // switch the activation of all views with tabs.
            for (Tab tab : tabViewMap.keySet()) {
                tabViewMap.get(tab).setActive(tab == selectedTab);
            }
        });
        log.debug("Main controller initialized");
    }

    private void initRecentWorkspacesMenu() {
        // init menu
        menuRecentWorkspaces.getItems().clear();
        List<String> recentWorkspaces = fxPreferences.getPreference(MINDOLPH_PROJECTS_RECENT, new LinkedList<>());
        for (String recentWorkspace : recentWorkspaces) {
            File workspaceDir = new File(recentWorkspace);
            if (!workspaceDir.exists()) {
                continue;
            }
            MenuItem miWorkspace = new MenuItem(recentWorkspace);
            miWorkspace.setMnemonicParsing(false);
            miWorkspace.setOnAction(event -> {
                // open workspace
                openWorkspace(workspaceDir, false);
                SceneRestore.getInstance().saveScene(this.workspaceList);
            });
            menuRecentWorkspaces.getItems().add(miWorkspace);
        }
    }

    @Override
    public void onWorkspacesRestore(WorkspaceList workspaceList) {
        this.workspaceList = workspaceList;
        workspaceView.toggleButtons(workspaceList == null || workspaceList.isEmpty());
        if (workspaceList != null && !workspaceList.isEmpty()) {
            log.info("Restore workspaces: %d".formatted(workspaceList.getSize()));
            workspaceList.getProjects().forEach(ws -> {
                log.debug(ws.getBaseDirPath());
            });
            workspaceView.loadWorkspaces(workspaceList);
        }
        else {
            if (DialogFactory.yesNoConfirmDialog("Before starting to use Mindolph, you should create your first workspace, do you want to proceed?")) {
                onMenuNewWorkspace();
            }
        }
    }

    /**
     * @since 1.9.x
     */
    private void loadCollections() {
        collectionToggleGroup = new ToggleGroup();
        rmiCollectionDefault = new RadioMenuItem("default");
        rmiCollectionDefault.setUserData("default");
        rmiCollectionDefault.setToggleGroup(collectionToggleGroup);
        rmiCollectionDefault.setOnAction(event -> {
            // emit event to close all files of current collection and load default collection.
            this.onSelectCollection("default");
        });
        menuCollections.getItems().add(rmiCollectionDefault);

        Map<String, List<String>> fileCollectionMap = this.cm.getFileCollectionMap();
        if (fileCollectionMap.isEmpty()) {
            // init the default collection from opened file list for the first time.
            List<String> openedFileList = fxPreferences.getPreference(MINDOLPH_OPENED_FILE_LIST, new ArrayList<>());
            this.cm.saveCollectionFilePaths("default", openedFileList);
            this.cm.saveActiveCollectionName("default");
            if (openedFileList != null) {
                rmiCollectionDefault.setText("default(%d)".formatted(openedFileList.size()));
            }
            log.info("The 'default' collection is created from last opened file list");
        }
        else {
            log.info("Load %d collections.".formatted(fileCollectionMap.size()));

            List<String> filesInDefault = fileCollectionMap.get("default");
            if (filesInDefault != null) {
                rmiCollectionDefault.setText("default(%d)".formatted(filesInDefault.size()));
            }

            String activeCollName = this.cm.getActiveCollectionName();
            rmiCollectionDefault.setSelected("default".equals(activeCollName));
            // load sorted collection names to menu except the default one.
            fileCollectionMap.keySet().stream()
                    .sorted(Comparator.comparing(String::toString))
                    .filter(collName -> !"default".equals(collName)).forEach(collName -> {
                        List<String> filePaths = fileCollectionMap.get(collName);
                        RadioMenuItem rmi = new RadioMenuItem("%s(%d)".formatted(collName, filePaths.size()));
                        rmi.setUserData(collName);
                        rmi.setOnAction(event -> {
                            onSelectCollection((String) rmi.getUserData());
                        });
                        if (collName.equals(activeCollName)) {
                            rmi.setSelected(true);
                        }
                        rmi.setToggleGroup(collectionToggleGroup);
                        menuCollections.getItems().add(rmi);
                    });
        }
    }

    public void onOpenFile(NodeData fileData, SearchParams searchParams, boolean visibleInWorkspace) {
        boolean autoSelect = FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_AUTO_SELECT_AFTER_FILE_OPENED, true) == Boolean.TRUE;
        // the file existence should be validated before this handler for its consequences are different.
        if (fileData.getFile().isFile()) {
            fileData.setSearchParams(searchParams);
            this.openFile(fileData, false);
            if (autoSelect)
                workspaceView.selectByNodeDataInAppropriateWorkspace(fileData);
        }
        else if (fileData.getFile().isDirectory()) {
//            if (autoSelect)
            workspaceView.selectByNodeDataInAppropriateWorkspace(fileData);
        }
        if (visibleInWorkspace && autoSelect) {
            splitPane.showAll();
            tabWorkspaces.getTabPane().getSelectionModel().select(tabWorkspaces);
        }
    }

    /**
     * @param fileData
     * @deprecated ??
     */
    @Override
    public void onFileChanged(NodeData fileData) {
        fileTabView.closeFileTabSafely(fileData);
    }

    public void onSearchStart(SearchParams searchParams) {
        String keyword = searchParams.getKeywords();
        SearchResultPane searchResultPane = new SearchResultPane();
        searchResultPane.init(searchParams);
        String title = "Search Results for '%s'".formatted(length(keyword) < 10 ? keyword : abbreviate(keyword, 10));
        fileTabView.loadContentToTab(searchResultPane, title);
    }

    @Override
    public void onOpenedFileRestore(List<File> files) {
        Platform.runLater(() -> {
            for (File file : files) {
                NodeData fileData = new NodeData(file);
                openFile(fileData, true);
            }
        });
    }

    /**
     * Open file with the workspace that is found by the file path.
     *
     * @param fileData
     */
    private void openFile(NodeData fileData, boolean lazy) {
        if (!fileData.getFile().exists()) {
            log.warn("File %s failed to open because the file doesn't exist anymore.".formatted(fileData.getName()));
            return;
        }
        WorkspaceMeta workspaceMeta = this.workspaceList.matchByFilePath(fileData.getFile().getPath());
        NodeData workspaceData = null; // for external file like attached image link, there is no related workspace.
        if (workspaceMeta != null) {
            File workspaceDir = new File(workspaceMeta.getBaseDirPath());
            workspaceData = new NodeData(NodeType.WORKSPACE, workspaceDir);
        }
        fileTabView.openFile(workspaceData, fileData, lazy);
    }

    private ContentView getCurrentContentView() {
        Tab selectedTab = fileTabView.getCurrentTab();
        return getContentView(selectedTab);
    }

    private ContentView getContentView(Tab tab) {
        if (tab != null) {
            if (tab.getContent() instanceof ContentView) {
                return (ContentView) tab.getContent();
            }
        }
        return null;
    }

    private Editable getCurrentEditor() {
        ContentView currentContentView = getCurrentContentView();
        if (currentContentView != null) {
            return getCurrentContentView().getEditor();
        }
        return null;
    }

    private Editable getEditor(Tab tab) {
        ContentView currentContentView = getContentView(tab);
        if (currentContentView != null) {
            return getCurrentContentView().getEditor();
        }
        return null;
    }

    @FXML
    public void onMenuNewWorkspace() {
        File saveDir = DialogFactory.openSaveFileDialog(workspaceView.getScene().getWindow(), SystemUtils.getUserHome());
        if (saveDir != null) {
            if (saveDir.exists()) {
                DialogFactory.warnDialog("Dir is already exist: " + saveDir);
            }
            else {
                if (!saveDir.mkdirs()) {
                    DialogFactory.warnDialog("Failed create new workspace");
                    return;
                }
                openWorkspace(saveDir, true);
                SceneRestore.getInstance().saveScene(this.workspaceList);
            }
        }
    }

    @FXML
    public void onMenuOpenWorkspace(ActionEvent event) {
        File workspaceDir = DialogFactory.openDirDialog(workspaceView.getScene().getWindow(), SystemUtils.getUserHome());
        if (workspaceDir != null && workspaceDir.exists()) {
            this.openWorkspace(workspaceDir, true);
        }
        SceneRestore.getInstance().saveScene(this.workspaceList);
    }

    @FXML
    public void onMenuManageWorkspaces(ActionEvent event) {
        WorkspaceManagementDialog dialog = new WorkspaceManagementDialog();
        dialog.showAndWait();
    }


//    @FXML
//    public void onMenuOpenFile(ActionEvent event) {
//        // should open external file
//    }

    private void openWorkspace(File workspaceDir, boolean saveToRecent) {
        WorkspaceMeta workspaceMeta = new WorkspaceMeta(workspaceDir.getPath());
        fxPreferences.savePreference(MINDOLPH_ACTIVE_WORKSPACE, workspaceDir.getPath());
        this.workspaceList.getProjects().add(workspaceMeta);
        this.workspaceView.loadWorkspaces(this.workspaceList);
        if (saveToRecent) {
            // save recent workspaces
            List<String> recentWorkspacePaths = fxPreferences.getPreference(MINDOLPH_PROJECTS_RECENT, new LinkedList<>());
            recentWorkspacePaths.remove(workspaceDir.getPath()); // remove only if contains.
            recentWorkspacePaths.addFirst(workspaceDir.getPath()); // add to the first.
            fxPreferences.savePreference(MINDOLPH_PROJECTS_RECENT, recentWorkspacePaths);
            initRecentWorkspacesMenu();
        }
    }

    @Override
    public void onWorkspaceViewSizeRestored(double size) {
        log.debug("Restore the workspace view size: " + size);
        splitPane.setFixedSize(size);
        log.debug("Listen split partial size changes"); // listen after position restore to make sure it won't be affected by default fixed size;
        splitPane.fixedSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                log.debug("Store changed workspace view size: " + newValue);
                workspaceViewResizedEventHandler.onWorkspaceViewResized(newValue.doubleValue());
            }
        });
    }

    public void onFileRenamed(NodeData nodeData, File renamedFile) {
        log.debug("file renamed from %s to %s".formatted(nodeData.getFile(), renamedFile));
        if (nodeData.isFile()) {
            fileTabView.updateOpenedTabAndEditor(nodeData, renamedFile);
            this.cm.updateFilePath(nodeData.getFile().getPath(), renamedFile.getPath());
        }
        else if (nodeData.isFolder() || nodeData.isWorkspace()) {
            // update all opened file under this folder.
            String origDirPath = nodeData.getFile().getAbsolutePath();
            String newDirPath = renamedFile.getAbsolutePath();
            fileTabView.updateTabFileParentDir(origDirPath, newDirPath);
        }
    }

    @FXML
    public void onMenuSave(ActionEvent event) {
        fileTabView.saveCurrentTab();
    }

    @FXML
    public void onMenuSaveAs(ActionEvent event) {
        Object userData = fileTabView.getCurrentTab().getUserData();
        if (userData instanceof NodeData && ((NodeData) userData).isFile()) {
            fileTabView.saveAsFrom((NodeData) userData);
        }
    }

    @FXML
    public void onMenuSaveAll(ActionEvent event) {
        fileTabView.saveAllTabs();
    }

    @FXML
    public void onMenuPrint(ActionEvent event) {
        Editable editor = getCurrentEditor();
        Printable printable = null;
        if (editor instanceof MindMapEditor) {
            MindMap<TopicNode> model = new MindMap<>(((MindMapEditor) editor).getMindMapModel());
            Printer firstPrinter = PrinterManager.getInstance().getFirstPrinter();
            if (firstPrinter == null) {
                DialogFactory.errDialog("Unable to find printers");
                return;
            }
            printable = new MindMapPrintable(model, firstPrinter.getDefaultPageLayout());
        }
        else if (editor instanceof PlantUmlEditor) {
            Image image = ((PlantUmlEditor) editor).getImage();
            printable = new ImagePrintable(image, PrinterManager.getInstance().getFirstPrinter().getDefaultPageLayout());
        }
        else if (editor instanceof MarkdownEditor) {
            ((MarkdownEditor) editor).print();
            return;
        }
        else if (editor instanceof ImageViewerEditor) {
            Image image = ((ImageViewerEditor) editor).getImage();
            printable = new ImagePrintable(image, PrinterManager.getInstance().getFirstPrinter().getDefaultPageLayout());
        }
        else if (editor instanceof PlainTextEditor) {
            Image image = ((PlainTextEditor) editor).getImage();
            printable = new ImagePrintable(image, PrinterManager.getInstance().getFirstPrinter().getDefaultPageLayout());
        }
        else {
            return;
        }
        PrintPreviewDialog dialog = new PrintPreviewDialog(printable);
        dialog.showAndWait();
    }

    @FXML
    public void onMenuClose(ActionEvent event) {
        Tab selectedTab = fileTabView.getCurrentTab();
        fileTabView.closeTab(selectedTab);
    }

    @FXML
    public void onMenuExit(ActionEvent event) {
        Boolean requireConfirm = FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_CONFIRM_BEFORE_QUITTING, true);
        if (requireConfirm) {
            Boolean quit = new ConfirmDialogBuilder().positive("Quit").cancel().asDefault().content("Are you sure to quit Mindolph?").showAndWait();
            if (quit == null || !quit) {
                return;
            }
        }
        log.debug("exit Mindolph");
        event.consume(); // this probably avoid the default quitting action, but actually doesn't work.
        SceneRestore.getInstance().stop(); // stop store state to avoid clean all the opened files state.
        if (fileTabView.closeAllTabsWithOpenedFiles()) {
            this.dispose();
            System.exit(0);
        }
    }

    @FXML
    public void onMenuGotoFile() {
        GotoFileDialog dialog = new GotoFileDialog();
        dialog.showAndWait();
    }

    @FXML
    public void onMenuUndo(ActionEvent event) {
        Editable currentEditor = this.getCurrentEditor();
        if (currentEditor != null) currentEditor.undo();
    }

    @FXML
    public void onMenuRedo(ActionEvent event) {
        Editable currentEditor = this.getCurrentEditor();
        if (currentEditor != null) currentEditor.redo();
    }

    @FXML
    public void onMenuFind(ActionEvent event) {
        ContentView currentContentView = this.getCurrentContentView();
        if (currentContentView != null) {
            currentContentView.showSearchBar(this.getCurrentEditor().getSelectionText(), false);
        }
    }

    @FXML
    public void onMenuReplace(ActionEvent event) {
        ContentView currentContentView = this.getCurrentContentView();
        if (currentContentView != null) {
            currentContentView.showSearchBar(this.getCurrentEditor().getSelectionText(), true);
        }
    }

    @FXML
    public void onMenuCopy(ActionEvent event) {
        Editable currentEditor = this.getCurrentEditor();
        if (currentEditor != null) currentEditor.copy();
    }

    @FXML
    public void onMenuPaste(ActionEvent event) {
        Editable currentEditor = this.getCurrentEditor();
        if (currentEditor != null) currentEditor.paste();
    }

    @FXML
    public void onMenuCut(ActionEvent event) {
        Editable currentEditor = this.getCurrentEditor();
        if (currentEditor != null) currentEditor.cut();
    }

    @FXML
    public void onMenuPreferences(ActionEvent event) {
        new PreferencesDialog().show(param -> {
            log.info("Preferences loaded");
        });
    }

    @FXML
    public void onMenuToggleWorkspaceView(ActionEvent event) {
        CheckMenuItem mi = (CheckMenuItem) event.getSource();
        if (mi.isSelected()) {
            splitPane.showAll();
        }
        else {
            splitPane.hidePrimary();
        }
    }

    /**
     * @param selectCollectionName null means not select any collection
     */
    private void resetCollectionSelection(String selectCollectionName) {
        // select the selected one
        menuCollections.getItems().stream().filter(mi -> mi instanceof RadioMenuItem)
                .forEach(cmi -> ((RadioMenuItem) cmi).setSelected(cmi.getUserData().equals(selectCollectionName)));
    }

    @FXML
    public void onCreateCollection() {
        if (fileTabView.getAllOpenedFiles().isEmpty()) {
            DialogFactory.warnDialog("No files is opened for creating new collection.");
            return;
        }
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Create a new collection with all opened files")
                .content("New Collection Name")
                .width(500)
                .build();
        Optional<String> optColName = dialog.showAndWait();
        if (optColName.isPresent()) {
            String newColName = optColName.get().trim();
            if (this.cm.getFileCollectionMap().containsKey(newColName)) {
                DialogFactory.warnDialog("The name '%s' already exists!".formatted(newColName));
                return;
            }

            this.cm.saveCollectionFilePaths(newColName, fileTabView.getAllOpenedFiles().stream().map(File::toString).toList());
            this.cm.saveActiveCollectionName(newColName);

            // remove for resorting first.
            menuCollections.getItems().removeIf(mi -> mi instanceof RadioMenuItem);

            // load all again to resort collections.
            this.loadCollections();

            Platform.runLater(() -> {
                Notifications.create().title("Create collection")
                        .text("Collection '%s' is created with %d files successfully.".formatted(newColName, fileTabView.getAllOpenedFiles().size())).showWarning();
            });
        }
    }

    @FXML
    public void onSaveCollection() {
        // save current collection with opened files
        String activeCollectionName = this.cm.getActiveCollectionName();
        if (StringUtils.isBlank(activeCollectionName)) {
            log.warn("No active collection found");
            return;
        }
        if (fileTabView.getAllOpenedFiles().isEmpty()) {
            DialogFactory.warnDialog("No files is opened for saving collection.");
            return;
        }
        this.cm.saveCollectionFilePaths(activeCollectionName, fileTabView.getAllOpenedFiles().stream().map(File::toString).toList());

        // reset the file counter in the text of menu item.
        Optional<MenuItem> first = menuCollections.getItems().stream().filter(menuItem ->
                menuItem.getUserData() != null && menuItem.getUserData().equals(activeCollectionName)).findFirst();
        first.ifPresent(menuItem -> menuItem.setText("%s(%d)".formatted(activeCollectionName, fileTabView.getAllOpenedFiles().size())));

        Platform.runLater(() -> {
            Notifications.create().title("Save collection")
                    .text("Collection '%s' is saved with %d files successfully.".formatted(activeCollectionName, fileTabView.getAllOpenedFiles().size())).showWarning();
        });
    }

    @FXML
    public void onRemoveCollection() {
        // remove current user defined collection.
        String activeCollectionName = this.cm.getActiveCollectionName();
        if (StringUtils.isBlank(activeCollectionName) || "default".equalsIgnoreCase(activeCollectionName)) {
            DialogFactory.warnDialog("The default collection cannot be removed.");
            return;
        }
        boolean confirmRemove = DialogFactory.yesNoConfirmDialog("Are you sure to remove collection '%s' \n(NO files will be deleted) ".formatted(activeCollectionName));
        if (confirmRemove) {
            // switch to default collection first
            this.onSelectCollection("default");
            // delete current user-defined collection
            this.cm.deleteCollection(activeCollectionName);
            menuCollections.getItems().removeIf(mi -> activeCollectionName.equals(mi.getUserData()));
            this.resetCollectionSelection("default");
            Platform.runLater(() -> {
                Notifications.create().title("Remove collection")
                        .text("Collection '%s' is removed successfully.".formatted(activeCollectionName)).showWarning();
            });
        }
    }

    @FXML
    public void onSelectCollection(String collectionName) {
        List<String> files;
        if (StringUtils.isNotBlank(collectionName)) {
            // get files of selected collection.
            files = this.cm.getCollectionFilePaths(collectionName);
        }
        else {
            files = fxPreferences.getPreference(MINDOLPH_OPENED_FILE_LIST, new ArrayList<>());
        }

        if (files == null || files.isEmpty()) {
            log.warn("No files found in collection %s".formatted(collectionName));
            return;
        }

        log.info("Load collection %s".formatted(collectionName));

        // close current opened files(no matter what collection is).
        fileTabView.closeAllTabs();

        // load files
        if (!files.isEmpty()) {
            onOpenedFileRestore(files.stream().map(File::new).toList());
        }
        this.cm.saveActiveCollectionName(collectionName);

        EventBus.getIns().notifyMenuStateChange(MenuTag.REMOVE_COLLECTION, !"default".equals(collectionName));
    }


    @FXML
    public void onMenuShortcuts() {
        new ShortcutsDialog().showAndWait();
    }

    @FXML
    public void onMenuCheckUpdate() {
        ReleaseUtils.getLatestReleaseVersion(latest -> {
            if (latest == null || StringUtils.isBlank(latest.getVersion())) {
                showCheckUpdatesToast("Unable to retrieve the latest release information.");
                return;
            }
            String currentVersion = Env.isDevelopment ? "1.3.5" : MavenUtils.getVersionInPomProperties();
            if (currentVersion == null || StringUtils.isBlank(currentVersion)) {
                log.warn("Can't get current version");
                return;
            }

            currentVersion = "v" + currentVersion;

            if (currentVersion.compareTo(latest.getVersion()) < 0) {
                if (StringUtils.isNotBlank(latest.getUrl())) {
                    String msg = "Found new release %s, current version is %s, do you wan to download and install?".formatted(latest.getVersion(), currentVersion);
                    Platform.runLater(() -> {
                        if (DialogFactory.yesNoConfirmDialog("New updates", msg)) {
                            DesktopUtils.openURL(latest.getUrl());
                        }
                    });
                    return;
                }
            }

            showCheckUpdatesToast("You already have the latest version installed.");
        });
    }

    private void showCheckUpdatesToast(String msg) {
        Platform.runLater(() -> {
            Notifications.create().title("Check updates").text(msg).showWarning();
        });
    }

    @FXML
    public void onMenuAbout(ActionEvent event) {
        String appVersion = getClass().getPackage().getImplementationVersion();
        log.info("app version: %s".formatted(appVersion));
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }

    public void dispose() {

    }

}
