package com.mindolph.fx.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.dialog.DialogFileFilters;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.editor.BasePreviewEditor;
import com.mindolph.base.editor.Editable;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.FileActivatedEvent;
import com.mindolph.base.event.NotificationType;
import com.mindolph.core.config.EditorConfig;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.fx.TabManager;
import com.mindolph.fx.editor.EditorFactory;
import com.mindolph.markdown.MarkdownEditor;
import com.mindolph.mfx.dialog.ConfirmDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.DesktopUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mindolph.base.event.EventBus.MenuTag.*;
import static org.apache.commons.lang3.StringUtils.replaceOnce;

/**
 * @author mindolph.com@gmail.com
 */
public class FileTabView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(FileTabView.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    @FXML
    private TabPane tabPane;

    /**
     * FileData -> Tab mapping for opened files.
     */
    private final Map<NodeData, Tab> openedFileMap = new HashMap<>();
    /**
     * Tab -> Editor mapping for opened files.
     */
    private final Map<Tab, Editable> tabEditorMap = new HashMap<>();

    public FileTabView() {
        super("/view/file_tab_view.fxml");
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, selectedTab, selectingTab) -> {
            Object oldData = selectedTab == null ? null : selectedTab.getUserData();
            if (selectingTab != null && selectedTab != selectingTab && !selectingTab.isDisabled()) {
                // disabled tab means it is closing, no need to be loaded(for close all or close others from context menu)
                log.debug("Tab selection changed from %s to %s".formatted(selectedTab == null ? "null" : selectedTab.getText(), selectingTab.getText()));
                TabManager.getIns().activeTab(selectingTab);
                BaseEditor editor = (BaseEditor) tabEditorMap.get(selectingTab);
                Object tabUserData = selectingTab.getUserData();
                if (editor == null) {
                    if (tabUserData != null) {
                        NodeData fileData = (NodeData) tabUserData;
                        this.loadEditorToTab(fileData, selectingTab);
                    }
                }
                else {
                    if (editor.isNeedRefresh()) {
                        editor.applyStyles();
                        editor.refresh();
                        editor.setNeedRefresh(false);
                    }
                    editor.requestFocus();
                    editor.locate(((NodeData) tabUserData).getAnchor()); // locate for 'find in files'
                    this.updateMenuState(editor);
                    editor.outline();
                }
                EventBus.getIns().notifyFileActivated(new FileActivatedEvent((NodeData) oldData, (NodeData) tabUserData));
            }
            else {
                this.updateMenuState(null);
                EventBus.getIns().notifyOutline(null); // clear the outline view since there is no tab exists.
                EventBus.getIns().notifyFileActivated(new FileActivatedEvent((NodeData) oldData, null));
            }
        });
        tabPane.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    EventBus.getIns().notify(NotificationType.DOUBLE_CLICKED_TAB);
                }
            }
        });
        // listen locate in file for outline
        EventBus.getIns().subscribeLocateInFile(anchor -> {
            Editable editor = this.tabEditorMap.get(getCurrentTab());
            if (editor != null) {
                editor.locate(anchor);
            }
        });
        // handle file deleted event
        EventBus.getIns().subscribeDeletedFile(this::closeTabSilently);

        // handle snippet applying event
        EventBus.getIns().subscribeSnippetApply(snippet -> {
            if (snippet == null) return;
            log.debug("Apply snippet: %s".formatted(snippet.getTitle()));
            Editable editor = this.tabEditorMap.get(getCurrentTab());
            if (editor != null) {
                editor.onSnippet(snippet);
            }
        });

    }

    /**
     * @param editor null means all no editors opening.
     */
    private void updateMenuState(BaseEditor editor) {
        if (editor == null) {
            EventBus.getIns().disableMenuItems(SAVE_AS, SAVE_ALL, CLOSE_TAB, FIND, REPLACE, UNDO, REDO, CUT, COPY, PASTE, PRINT);
        }
        else {
            EventBus.getIns().notifyMenuStateChange(SAVE, editor.isChanged());
            EventBus.getIns().notifyMenuStateChange(UNDO, editor.isUndoAvailable());
            EventBus.getIns().notifyMenuStateChange(REDO, editor.isRedoAvailable());
            EventBus.getIns().notifyMenuStateChange(CUT, editor.isSelected());
            EventBus.getIns().notifyMenuStateChange(COPY, editor.isSelected());
            EventBus.getIns().notifyMenuStateChange(FIND, editor.isSearchable());
            EventBus.getIns().notifyMenuStateChange(REPLACE, editor.isSearchable());
//            EventBus.getIns().enableMenuItems(PRINT);
            EventBus.getIns().enableMenuItems(PASTE);
        }
    }

    public void openFile(NodeData workspaceData, NodeData fileData, boolean lazy) {
        Tab tab = openedFileMap.get(fileData);
        if (tab == null) {
            log.debug("Open file %s in new tab".formatted(lazy ? "lazily" : "instantly"));
            tab = new Tab();
            tab.setText(fileData.getName());
            fileData.setWorkspaceData(workspaceData); // just workaround
            tab.setUserData(fileData); //
            tab.setTooltip(new Tooltip(fileData.getFile().getPath()));
            tab.setGraphic(FontIconManager.getIns().getIconForFile(fileData));
            tab.setOnCloseRequest(event -> {
                log.debug("On closing tab");
                // retrieve fileData from event because the original fileData is mutable
                if (!closeFileTabSafely((NodeData) ((Tab) event.getSource()).getUserData())) {
                    event.consume();
                }
            });
            this.createContextMenuForTab(tab);
            this.tabPane.getTabs().add(tab);
            this.openedFileMap.put(fileData, tab);
            if (!lazy) {
                tabPane.getSelectionModel().select(tab);
                tabEditorMap.get(tab).requestFocus();
            }
        }
        else {
            log.debug("file tab already exists: %s".formatted(fileData.getFile()));
            log.debug(StringUtils.join(openedFileMap.keySet()));
            tab.setUserData(fileData);// update user data because the file data is probably updated(with Anchor for example)
            tabPane.getSelectionModel().select(tab);
            tabEditorMap.get(tab).requestFocus();
        }
        EventBus.getIns().notifyMenuStateChange(CLOSE_TAB, true);
        EventBus.getIns().notifyMenuStateChange(SAVE_AS, true);
//        EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.PRINT, true);
        EventBus.getIns().notifyMenuStateChange(FIND, true);
        EventBus.getIns().notifyMenuStateChange(REPLACE, true);
    }

    /**
     * Load customized content other than editors.
     *
     * @param content
     * @param title
     */
    public void loadContentToTab(Node content, String title) {
        Tab tab = new Tab();
        tab.setText(title);
        tab.setContent(content);
        tab.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEARCH));
        tabPane.getTabs().add(tab);
        this.createContextMenuForTab(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void loadEditorToTab(NodeData fileData, Tab tab) {
        log.info("Load editor to tab for file: %s".formatted(fileData.getFile()));
        BaseEditor editor = EditorFactory.createEditor(fileData.getWorkspaceData(), fileData);
        ContentView contentView = new ContentView();
        contentView.withEditor(editor);
        tab.setContent(contentView);
        this.tabEditorMap.put(tab, editor);
        this.createContextMenuForTab(tab);

        // do something to init the editor when file is loaded and update menu states.
        EventBus.getIns().subscribeFileLoaded(fileData, nodeData -> {
            Platform.runLater(() -> {
                editor.locate(fileData.getAnchor());

                editor.setOnFileChangedListener(changedFileData -> {
                    if (log.isTraceEnabled()) log.trace("File changed: %s".formatted(changedFileData.getFile()));
                    Tab changedTab = openedFileMap.get(changedFileData);
                    changedTab.setText("*" + changedFileData.getName());
                    // changedTab.setStyle("-fx-font-size: 15"); // seams not work for default font
                    EventBus.getIns().notifyMenuStateChange(SAVE, true);
                    EventBus.getIns().notifyMenuStateChange(SAVE_ALL, true);
                });
                editor.setFileSavedEventHandler(savedFileData -> {
                    log.info("File %s saved.".formatted(savedFileData.getFile()));
                    Tab curTab = getCurrentTab();
                    curTab.setText(fileData.getName());
                    // curTab.setStyle("-fx-font-size: 14"); seams not work for default font
                    EventBus.getIns().notifyMenuStateChange(SAVE, false);
                });

                if (editor instanceof BasePreviewEditor) {
                    // center the splitter on loading only.
                    ((BasePreviewEditor) editor).centerSplitter();
                }

                this.updateMenuState(editor);
            });
        });

        new Thread(() -> {
            try {
                // listen: update the status bar from status msg event
                EventBus.getIns().subscribeStatusMsgEvent(fileData.getFile(), statusMsg -> {
                    Platform.runLater(() -> {
                        contentView.updateStatusBar(statusMsg);
                    });
                });
                editor.loadFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // call to remember opened file.
            EventBus.getIns().notifyOpenedFileChange(tabPane.getTabs().stream()
                    .filter(tb -> tb.getUserData() instanceof NodeData && ((NodeData) tb.getUserData()).isFile())
                    .map(tb -> ((NodeData) tb.getUserData()).getFile()).collect(Collectors.toList()));
            // add to recent files history
            RecentManager.getInstance().addToRecent(fileData.getFile());

            Platform.runLater(editor::requestFocus);
        }, "File Load Thread").start();
    }

//    private void locateInEditor(BaseEditor editor, NodeData fileData) {
//        // locate searched keyword in file
//        if (fileData.getAnchor() != null) {
//            editor.locate(fileData.getAnchor());
//        }
//        else {
//            log.warn("No target to locate");
//        }
//    }

    public ContextMenu createContextMenuForTab(Tab selectedTab) {
        Object selectedTabUserData = selectedTab.getUserData();
        if (selectedTabUserData != null) log.debug(((NodeData) selectedTabUserData).getFile().getPath());
        ContextMenu contextMenu = new ContextMenu();
        MenuItem miClose = new MenuItem("Close", FontIconManager.getIns().getIcon(IconKey.CLOSE));
        MenuItem miCloseOthers = new MenuItem("Close Others");
        MenuItem miCloseAll = new MenuItem("Close All");
        MenuItem miSaveAs = new MenuItem("Save As..");
        MenuItem miSelectInTree = new MenuItem("Select in Workspace", FontIconManager.getIns().getIcon(IconKey.WORKSPACE_TREE));
        MenuItem miOpenInSystem = new MenuItem("Open in System", FontIconManager.getIns().getIcon(IconKey.SYSTEM));

        // actions
        miSaveAs.setOnAction(event -> {
            saveAsFrom((NodeData) selectedTabUserData);
        });
        miClose.setOnAction(event -> {
            closeTab(selectedTab);
        });
        miCloseOthers.setOnAction(event -> {
            this.closeAllTabsExcept(selectedTab);
        });
        miCloseAll.setOnAction(event -> {
            this.closeAllTabs();
        });
        miSelectInTree.setOnAction(event -> {
//            log.debug("selected tab user data: %s".formatted(((NodeData) selectedTabUserData).getFile().getPath()));
            log.debug(((NodeData) selectedTabUserData).getFile().getPath());
            EventBus.getIns().notifyLocateInWorkspace((NodeData) selectedTabUserData);
        });
        miOpenInSystem.setOnAction(event -> {
            File file = ((NodeData) selectedTabUserData).getFile();
            log.info("Try to open file: " + file);
            try {
                DesktopUtils.openInSystem(file, false);
            } catch (Exception e) {
                DialogFactory.warnDialog("Can't open this file in system");
            }
        });
        if (selectedTabUserData instanceof NodeData data && ((NodeData) selectedTabUserData).isFile()) {
            contextMenu.getItems().addAll(miClose, miCloseOthers, miCloseAll,
                    new SeparatorMenuItem(), miSaveAs,
                    new SeparatorMenuItem(), miSelectInTree);
            if (!data.isMindMap()) {
                contextMenu.getItems().add(miOpenInSystem);
            }
        }
        else {
            contextMenu.getItems().addAll(miClose, miCloseOthers, miCloseAll);
        }
        Editable editor = tabEditorMap.get(selectedTab);
        if (editor != null) {
            if (editor instanceof BasePreviewEditor) {
                MenuItem miChangeSplitterOrientation = new MenuItem("Change Splitter Orientation");
                Menu mViewMode = new Menu("View Mode");
                ToggleGroup toggleGroup = new ToggleGroup();
                RadioMenuItem miTextOnly = new RadioMenuItem("Text Only", FontIconManager.getIns().getIcon(IconKey.CODE));
                RadioMenuItem miPreviewOnly = new RadioMenuItem("Preview Only", FontIconManager.getIns().getIcon(IconKey.PREVIEW));
                RadioMenuItem miBoth = new RadioMenuItem("Both");
                miBoth.setSelected(true);
                miTextOnly.setToggleGroup(toggleGroup);
                miPreviewOnly.setToggleGroup(toggleGroup);
                miBoth.setToggleGroup(toggleGroup);
                mViewMode.getItems().addAll(miTextOnly, miPreviewOnly, miBoth);

                miChangeSplitterOrientation.setOnAction(event -> {
                    Editable selectedEditor = tabEditorMap.get(selectedTab);
                    if (selectedEditor instanceof BasePreviewEditor) {
                        ((BasePreviewEditor) selectedEditor).toggleOrientation();
                    }
                });
                miTextOnly.setOnAction(event -> {
                    Editable selectedEditor = tabEditorMap.get(selectedTab);
                    if (selectedEditor instanceof BasePreviewEditor) {
                        ((BasePreviewEditor) selectedEditor).changeViewMode(Editable.ViewMode.TEXT_ONLY);
                    }
                });
                miPreviewOnly.setOnAction(event -> {
                    Editable selectedEditor = tabEditorMap.get(selectedTab);
                    if (selectedEditor instanceof BasePreviewEditor) {
                        ((BasePreviewEditor) selectedEditor).changeViewMode(Editable.ViewMode.PREVIEW_ONLY);
                    }
                });
                miBoth.setOnAction(event -> {
                    Editable selectedEditor = tabEditorMap.get(selectedTab);
                    if (selectedEditor instanceof BasePreviewEditor) {
                        ((BasePreviewEditor) selectedEditor).changeViewMode(Editable.ViewMode.BOTH);
                    }
                });
                BasePreviewEditor previewEditor = (BasePreviewEditor) editor;
                previewEditor.orientationObjectProperty().addListener((observable, oldValue, newValue) -> {
                    Orientation orientation = previewEditor.getOrientation();
                    Text icon = orientation == Orientation.HORIZONTAL ? FontIconManager.getIns().getIcon(IconKey.SWITCH_VERTICAL) : FontIconManager.getIns().getIcon(IconKey.SWITCH_HORIZONTAL);
                    miChangeSplitterOrientation.setGraphic(icon);
                });
                Orientation orientation = previewEditor.getOrientation();
                Text icon = orientation == Orientation.HORIZONTAL ? FontIconManager.getIns().getIcon(IconKey.SWITCH_VERTICAL) : FontIconManager.getIns().getIcon(IconKey.SWITCH_HORIZONTAL);
                miChangeSplitterOrientation.setGraphic(icon);
                contextMenu.getItems().addAll(new SeparatorMenuItem(), miChangeSplitterOrientation, mViewMode);
            }

            if (editor instanceof MarkdownEditor) {
                CheckMenuItem miAutoScroll = new CheckMenuItem("Auto Scroll");
                miAutoScroll.setSelected(true);
                miAutoScroll.setOnAction(actionEvent -> {
                    ((BasePreviewEditor) editor).setIsAutoScroll(miAutoScroll.isSelected());
                });
                contextMenu.getItems().add(miAutoScroll);
            }
        }

        selectedTab.setContextMenu(contextMenu);
        return contextMenu;
    }

    public void requestFocusOnCurrentEditor() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        Editable editor = tabEditorMap.get(selectedTab);
        if (editor != null) editor.requestFocus();
    }

    /**
     * @param fileData
     */
    public void saveAsFrom(NodeData fileData) {
        if (!saveEditorFile(fileData)) {
            return;
        }
        File origFile = fileData.getFile();
        String extension = FilenameUtils.getExtension(origFile.getName());
        FileChooser.ExtensionFilter extensionFilter = DialogFileFilters.getExtensionFilter(extension);
        File saveAsFile = DialogFactory.openSaveFileDialog(this.getScene().getWindow(), origFile.getParentFile(),
                "%sCopy.%s".formatted(FilenameUtils.getBaseName(origFile.getName()), extension), extensionFilter);
        if (saveAsFile == null) {
            log.debug("No file selected to save");
        }
        else {
            // append original extension if user deleted it, otherwise save as user input (which means user can change the extension)
            File saveAsFileWithExt = new File(FileNameUtils.appendFileExtensionIfAbsent(saveAsFile.getPath(), extension));
            log.info("Try to save file as :%s".formatted(saveAsFileWithExt.getPath()));

            // TODO these code are un-implemented, comment for later implementation (to handle save as to an existing(and modified) file)
//            NodeData newNodeData = new NodeData(saveAsFileWithExt);
//            Tab tab = openedFileMap.get(newNodeData);
//            boolean needReload = false;
//            Editable editor = null;
//            if (tab != null) {
//                editor = tabEditorMap.get(tab);
//                if (editor != null) {
//                    if (editor.isChanged()) {
//                        if (DialogFactory.yesNoConfirmDialog("The file you want to save already exists and modified, are you sure to overwrite")) {
//                            needReload = true;
//                        } else {
//                            return;// cancel saving
//                        }
//                    } else {
//                        needReload = true;
//                    }
//                }
//            }

            try {
                if (saveAsFileWithExt.equals(origFile)) {
                    return; // just return since the FileUtils.copyFile will throw exception if source and target are the same.
                }
                else {
                    FileUtils.copyFile(origFile, saveAsFileWithExt);
                }
            } catch (Exception e) {
                e.printStackTrace();
                DialogFactory.errDialog("Failed to save file: " + e.getLocalizedMessage());
                return;
            }
            // Add new file tree item to the parent tree item by file path.
            EventBus.getIns().notifyNewFileToWorkspace(saveAsFileWithExt);

            openFile(fileData.getWorkspaceData(), new NodeData(saveAsFileWithExt), false);
            closeFileTab(openedFileMap.get(fileData), fileData);
        }
    }

    public void updateTabFileParentDir(String origDirPath, String newDirPath) {
        for (NodeData fileData : new HashSet<>(openedFileMap.keySet())) {
            String origFilePath = fileData.getFile().getAbsolutePath();
            if (origFilePath.startsWith(origDirPath)) {
                File newFile = new File(replaceOnce(origFilePath, origDirPath, newDirPath));
                this.updateOpenedTabAndEditor(fileData, newFile);
            }
        }
    }

    /**
     * Update one opened tab and editor with new file path.
     *
     * @param origNodeData
     * @param newFile
     */
    public void updateOpenedTabAndEditor(NodeData origNodeData, File newFile) {
        Tab tab = openedFileMap.get(origNodeData);
        if (tab != null) {
            log.debug("Update opened file %s to new file %s".formatted(origNodeData.getFile(), newFile));
            // update opened file information.
            openedFileMap.remove(origNodeData);
            origNodeData.setFile(newFile);
            // the userData of tab has already been referred by tab context menu, so replace the file instead of calling setUserData().
            // otherwise the context menu cant manipulate the appropriate nodeData. In other word, the nodeData in tab might be equal but not same to the one in TreeView.
            ((NodeData) tab.getUserData()).setFile(newFile);
            // re-add to mapping since the hash is already changed.
            openedFileMap.put(origNodeData, tab);
            // update the editor's context for saving content to new file(path)
            Editable editable = tabEditorMap.get(tab);
            if (editable == null) {
                log.info("Editor for '%s' probably has not been instantiated yet.".formatted(tab.getText()));
                tab.setText(newFile.getName());
            }
            else {
                editable.getEditorContext().getFileData().setFile(newFile);
                tab.setText((editable.isChanged() ? "*" : StringUtils.EMPTY) + newFile.getName());
            }
            tab.setTooltip(new Tooltip(newFile.getPath()));
        }
        else {
            log.warn("No tab opened for file: %s".formatted(origNodeData.getFile()));
        }
    }

    public void reloadEditorsByType(String fileType) {
        log.debug("Reload editors for file: %s".formatted(fileType));
        // reload config for the type of editor.
        EditorConfig config = EditorFactory.getConfig(fileType);
        if (config != null) {
            config.loadFromPreferences();
        }
        // reload editor if it is current editor or request a lazy reload if it is not.
        for (Tab tab : tabEditorMap.keySet()) {
            BaseEditor editor = (BaseEditor) tabEditorMap.get(tab);
            if (tab.isSelected()) {
                editor.applyStyles();
                editor.refresh();
            }
            else {
                if (editor.getFileType().equals(fileType)) {
                    editor.setNeedRefresh(true); // reload later.
                }
            }
        }
    }

    public void saveCurrentTab() {
        Tab tab = this.getCurrentTab();
        NodeData fileData = (NodeData) tab.getUserData();
        try {
            tabEditorMap.get(tab).save();
        } catch (IOException e) {
            e.printStackTrace();
            DialogFactory.errDialog("Saving file %s failed.".formatted(fileData.getFile()));
        }
    }

    public void saveAllTabs() {
        for (Tab tab : tabEditorMap.keySet()) {
            Object userData = tab.getUserData();
            if (userData instanceof NodeData) {
                NodeData nodeData = ((NodeData) userData);
                if (nodeData.isFile()) {
                    try {
                        tabEditorMap.get(tab).save();
                    } catch (IOException e) {
                        e.printStackTrace();
                        DialogFactory.errDialog("Saving file %s failed.".formatted(nodeData.getFile()));
                    }
                    tab.setText(nodeData.getName());
                    tab.setStyle("-fx-font-size: 14");
                }
            }
        }
    }

    /**
     * Close a tab no matter what type of tab it is.
     *
     * @param tab
     * @return
     */
    public boolean closeTab(Tab tab) {
        if (tab == null) return false;
        Object tabData = tab.getUserData();
        if (tabData instanceof NodeData nodeData) {
            if (nodeData.isFile()) {
                return closeFileTabSafely(nodeData);
            }
            log.warn("The data of file tab is not a file");
            return true;
        }
        else {
            log.debug("Closing a non-file tab");
            tabPane.getTabs().remove(tab);
            tabPane.getSelectionModel().select(TabManager.getIns().previousTabFrom(tab));
            return true;
        }
    }

    /**
     * Close tab safely.
     *
     * @param fileData the file data that tab contains.
     * @return true if closed the tab, false if user canceled.
     */
    public boolean closeFileTabSafely(NodeData fileData) {
        Tab tab = openedFileMap.get(fileData);
        if (tab != null && this.saveEditorFile(fileData)) {
            Editable editor = tabEditorMap.get(tab);
            if (editor != null) editor.dispose();// editor may not be loaded
            Tab nextTab = TabManager.getIns().previousTabFrom(tab);
            if (nextTab != null) {
                log.debug("Active tab: %s".formatted(nextTab.getText()));
                tabPane.getSelectionModel().select(nextTab);
            }
            this.closeFileTab(tab, fileData);
            return true;
        }
        else {
            log.warn("No opened tab for file %s".formatted(fileData.getFile()));
        }
        return false;
    }

    /**
     * Close file tab without saving (for cases like deleting a file)
     *
     * @param fileData
     */
    public void closeTabSilently(NodeData fileData) {
        log.debug("Close tab silently for file: %s".formatted(fileData.getFile()));
        Tab tab = openedFileMap.get(fileData);
        if (tab != null) {
            Editable editor = tabEditorMap.get(tab);
            if (editor != null) editor.dispose();
            Tab nextTab = TabManager.getIns().previousTabFrom(tab);
            if (nextTab != null) {
                tabPane.getSelectionModel().select(nextTab);
            }
            closeFileTab(tab, fileData);
        }
        else {
            log.debug("This file is not opened");
        }
    }

    /**
     * Close tab and notify.
     *
     * @param tab
     * @param fileData
     */
    private void closeFileTab(Tab tab, NodeData fileData) {
        tabPane.getTabs().remove(tab);
        openedFileMap.remove(fileData);
        tabEditorMap.remove(tab);
        EventBus.getIns().unsubscribeFileLoaded(fileData);
        EventBus.getIns().notifyOpenedFileChange(tabPane.getTabs().stream()
                .filter(tab1 -> tab1.getUserData() instanceof NodeData && ((NodeData) tab1.getUserData()).isFile())
                .map(tb -> ((NodeData) tb.getUserData()).getFile()).collect(Collectors.toList()));
        EventBus.getIns().notifyMenuStateChange(CLOSE_TAB, getCurrentTab() != null);
        EventBus.getIns().notifyMenuStateChange(SAVE_AS, getCurrentTab() != null);
//        EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.PRINT, getCurrentTab() != null);
        EventBus.getIns().notifyMenuStateChange(FIND, getCurrentTab() != null);
    }

    /**
     * Close all tabs no matter what the lading status is except specified one.
     *
     * @param ignoredTab
     * @since 1.9
     */
    public void closeAllTabsExcept(Tab ignoredTab) {
        List<Tab> otherTabs = tabPane.getTabs().stream().filter(tab -> tab != ignoredTab).toList();
        // disable closing tabs to avoid them to be loaded (in lazy mode).
        for (Tab otherTab : otherTabs) {
            otherTab.setDisable(true);
        }
        for (Tab otherTab : otherTabs) {
            if (!closeTab(otherTab)) break;
        }
    }

    /**
     * Close all tabs no matter what the lading status is.
     *
     * @since 1.9
     */
    public void closeAllTabs() {
        LinkedHashSet<Tab> tabs = new LinkedHashSet<>(tabPane.getTabs());
        // disable closing tabs to avoid them to be loaded (in lazy mode).
        for (Tab tab : tabs) {
            tab.setDisable(true);
        }
        for (Tab tab : tabs) {
            if (!closeTab(tab)) break;
        }
    }

    /**
     * Close all tabs with all opened files.
     *
     * @return false if any tab cancels closing by user.
     */
    public boolean closeAllTabsWithOpenedFiles() {
        LinkedHashSet<Tab> tabs = new LinkedHashSet<>(tabEditorMap.keySet());
        for (Tab tab : tabs) {
            log.trace("Closing tab: %s".formatted(tab.getText()));
            if (!this.closeTab(tab)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param fileData
     * @return False means error occurred or user canceled closing. Following action should be aborted.
     */
    private boolean saveEditorFile(NodeData fileData) {
        Tab tab = openedFileMap.get(fileData);
        Editable editor = tabEditorMap.get(tab);
        if (editor == null) {
            return true; // also close non-editor tab
        }
        if (editor.isChanged()) {
            Boolean needSave = new ConfirmDialogBuilder().title("Confirm unsaved file")
                    .content("You are closing an unsaved file, do you want to save it? " + fileData.getName())
                    .positive("Save").asDefault().no().cancel().showAndWait();
            if (needSave == null) {
                return false;// cancel closing file
            }
            if (needSave) {
                try {
                    editor.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    DialogFactory.errDialog("File save failed");
                    return false;
                }
            }
            return true;
        }
        else {
            return true;
        }
    }

    public Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public List<File> getAllOpenedFiles() {
        return tabPane.getTabs().stream().map(tab -> {
            if (tab.getUserData() instanceof NodeData nd) {
                return nd.getFile();
            }
            return null;
        }).filter(file -> file != null && file.isFile()).toList();
    }
}
