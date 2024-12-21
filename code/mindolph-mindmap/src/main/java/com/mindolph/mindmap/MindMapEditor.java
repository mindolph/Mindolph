package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.model.OutlineItemData;
import com.mindolph.core.model.Snippet;
import com.mindolph.core.search.Anchor;
import com.mindolph.core.search.SearchUtils;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.ModelManager;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.search.MindMapAnchor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.TransferMode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.mindolph.mindmap.constant.MindMapConstants.FILELINK_ATTR_OPEN_IN_SYSTEM;

/**
 * @author mindolph.com@gmail.com
 * @see ExtraMindMapView
 * @see ScalableScrollPane
 */
public class MindMapEditor extends BaseEditor {

    private static final Logger log = LoggerFactory.getLogger(MindMapEditor.class);

    @FXML
    private ScalableScrollPane scrollPane;

    private final MindMapView mindMapView;

    public MindMapEditor(EditorContext editorContext, MindMapView mindMapView) {
        super("/mindmap_editor.fxml", editorContext);
        super.threadPoolService = Executors.newSingleThreadExecutor();
        super.fileType = SupportFileTypes.TYPE_MIND_MAP;
        this.mindMapView = mindMapView;
        this.mindMapView.setParentPane(this);

        // invalidate the mind-map panel when become focused.
        this.focusedProperty().addListener((observableValue, wasFocused, isFocused) -> {
            log.debug("Mind Map editor focused");
            if (wasFocused != isFocused && isFocused) {
                mindMapView.forceRefresh();
            }
        });

        this.mindMapView.setOnDragOver(dragEvent -> {
            if (CollectionUtils.isEmpty(dragEvent.getDragboard().getFiles())
                    || dragEvent.getDragboard().getFiles().size() > 1) {
                return;
            }
            Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(dragEvent.getDragboard().getFiles().get(0));
            if (optPath.isPresent()) {
                BaseElement ele = mindMapView.findTopicForDragging(dragEvent);
                if (ele != null) {
                    dragEvent.acceptTransferModes(TransferMode.LINK);
                    mindMapView.requestFocus();
                    if (mindMapView.getSelectedTopics().size() > 1
                            || !mindMapView.getSelectedTopics().contains(ele.getModel())) {
                        mindMapView.removeAllSelection();
                        mindMapView.selectAndUpdate(ele.getModel(), false);
                    }
                }
            }
        });
        this.mindMapView.setOnDragDropped(dragEvent -> {
            BaseElement ele = mindMapView.findTopicForDragging(dragEvent);
            if (ele != null) {
                // TODO should only accept one file for now
                for (File file : dragEvent.getDragboard().getFiles()) {
                    Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(file);
                    if (optPath.isPresent()) {
                        TopicNode topic = ele.getModel();
                        ExtraFile extraFile = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
                        if (extraFile != null) {
                            if (!DialogFactory.okCancelConfirmDialog("File already exist in this topic, are you sure to replace?")) {
                                return;
                            }
                        }
                        Properties props = new Properties();
                        props.put(FILELINK_ATTR_OPEN_IN_SYSTEM, "false");
                        MMapURI fileUri = MMapURI.makeFromFilePath(editorContext.getWorkspaceData().getFile(), optPath.get(), props);
                        log.info(String.format("Path %s converted to uri: %s", optPath.get(), fileUri.asString(false, true)));
                        topic.setExtra(new ExtraFile(fileUri));
                        mindMapView.onMindMapModelChanged(true);
                        mindMapView.updateStatusBarForTopic(topic);
                    }
                    else {
                        log.warn("Link files not in same workspace are not supported yet");
                    }
                }
            }
        });

//        Platform.runLater(() -> {
//            Bounds viewportBounds = scrollPane.getViewportBounds();
//            mindMapView.setViewportRectangle(new Rectangle2D(0, 0, viewportBounds.getWidth(), viewportBounds.getHeight()));
//        });
    }

    @Override
    public Map<String, SearchBar.ExtraOption> createSearchOptions(boolean[] enables) {
        SearchBar.ExtraOption opTopic = new SearchBar.ExtraOption(FontIconManager.getIns().getIcon(IconKey.TOPIC), true, "In Topic");
        SearchBar.ExtraOption opNote = new SearchBar.ExtraOption(FontIconManager.getIns().getIcon(IconKey.NOTE), true, "In Note");
        SearchBar.ExtraOption opFileLink = new SearchBar.ExtraOption(FontIconManager.getIns().getIcon(IconKey.FILE_LINK), true, "In File Link");
        SearchBar.ExtraOption opUri = new SearchBar.ExtraOption(FontIconManager.getIns().getIcon(IconKey.URI), true, "In URI");
        LinkedHashMap<String, SearchBar.ExtraOption> extraOptions = new LinkedHashMap<>() {
            {
                put(IconID.ICON_FIND_IN_TOPIC.name(), opTopic);
                put(IconID.ICON_FIND_IN_NOTE.name(), opNote);
                put(IconID.ICON_FIND_IN_LINK.name(), opFileLink);
                put(IconID.ICON_FIND_IN_URI.name(), opUri);
            }
        };
        List<SearchBar.ExtraOption> optionList = Stream.of(opTopic, opNote, opFileLink, opUri).toList();

        if (enables != null) {
            for (int i = 0; i < enables.length; i++) {
                boolean enable = enables[i];
                optionList.get(i).setEnabled(enable);
            }
        }
        return extraOptions;
    }

    @Override
    public void locate(Anchor anchor) {
        Optional<TopicNode> firstInTree = Optional.empty();
        if (anchor instanceof MindMapAnchor ma) {
            log.debug("Try to locate topic: %s(%s)".formatted(ma.getText(), ma.getParentText()));
            firstInTree = this.getMindMapModel().findFirstInTree(t -> {
                log.debug("  %s".formatted(t.getText()));
                // TODO within extra contents if how to display them in the result has been resolved.
                return ma.getText().equals(t.getText())
                        && (t.getParent() == null || ma.getParentText().equals(t.getParent().getText()));
            });
        }
        if (firstInTree.isPresent()) {
            log.debug("Find topic node: %s".formatted(firstInTree.get().getText()));
            this.mindMapView.getSelection().clear();
            this.mindMapView.selectAndUpdate(firstInTree.get(), false);
        }
        else {
            log.warn("Couldn't locate any topic for '%s', select the root topic".formatted(anchor));
            this.mindMapView.select(this.mindMapView.getModel().getRoot());
        }
    }

    @Override
    public void loadFile() throws IOException {
        try {
            Platform.runLater(() -> {
                mindMapView.setFocusTraversable(true);
                mindMapView.setWorkspaceDir(editorContext.getWorkspaceData() == null ? null : editorContext.getWorkspaceData().getFile());
                mindMapView.setFile(editorContext.getFileData().getFile());
                scrollPane.setScalableView(mindMapView);
                mindMapView.setModelChangedEventHandler(() -> {
                    isChanged = true;
                    fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                    this.outline();
                });
                // TODO this handler probably set after event raised(from setModel() of MindMapView,
                //  which might causes the mind map not in center during loading. (TBD)
                mindMapView.setDiagramEventHandler((rootToCenter) -> {
                    // run later to ensure the viewport of scroll panel is ready. TODO
                    Platform.runLater(() -> {
                        log.debug("calculateAndUpdateViewportRectangle()");
                        scrollPane.calculateAndUpdateViewportRectangle();
                        if (rootToCenter) {
                            mindMapView.rootToCentre();
                        }
                        EventBus.getIns().notifyFileLoaded(editorContext.getFileData());
                        this.outline();
                        mindMapView.requestFocus();
                    });
                });

                MindmapEvents.subscribeMmdSaveEvent(mindMapView, unused -> {
                    try {
                        save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                mindMapView.undoAvailableProperty().addListener((observable, oldValue, undoAvailable) -> {
                    if (oldValue != undoAvailable)
                        EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.UNDO, undoAvailable);
                });

                mindMapView.redoAvailableProperty().addListener((observable, oldValue, redoAvailable) -> {
                    if (oldValue != redoAvailable)
                        EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.REDO, redoAvailable);
                });

                mindMapView.selectionProperty().addListener((observable, oldValue, selectedTopics) -> {
                    EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.CUT, !CollectionUtils.isEmpty(selectedTopics));
                    EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.COPY, !CollectionUtils.isEmpty(selectedTopics));
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void outline() {
        super.threadPoolService.execute(() -> {
            Tree tree = new Tree();
            tree.init(new Node("Stub"));
            Node firstNode = new Node(new OutlineItemData(getMindMapModel().getRoot().getText()));
            tree.getRootNode().addChild(firstNode);
            this.makeOutlinesRecursively(getMindMapModel().getRoot(), firstNode);
            EventBus.getIns().notifyOutline(tree);
        });
    }

    private void makeOutlinesRecursively(TopicNode rootTopic, Node rootNode) {
        // only root, second and third level topics are accepted.
        if (rootTopic.getTopicLevel() >= 2) {
            return;
        }
        for (TopicNode childTopic : rootTopic.getChildren()) {
            String content = StringUtils.isNotBlank(childTopic.getText()) ? StringUtils.abbreviate(childTopic.getText(), 30)
                    : "[Empty Topic]";
            MindMapAnchor mindMapAnchor = new MindMapAnchor();
            mindMapAnchor.setText(childTopic.getText());
            mindMapAnchor.setParentText(rootTopic.getText());
            Node newNode = new Node(new OutlineItemData(content, mindMapAnchor));
            rootNode.addChild(newNode);
            this.makeOutlinesRecursively(childTopic, newNode);
        }
    }

    @Override
    public void applyStyles() {
        // TODO do applying styles after mind map editor is refactored.
    }

    @Override
    public void refresh() {
        mindMapView.reload();
    }

    @Override
    public void searchNext(String keyword, TextSearchOptions options) {
        // The case-sensitive is Weired here, but it works!
        Pattern pattern = SearchUtils.string2pattern(keyword, options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
        mindMapView.findTopicByPattern(pattern, options, false);
    }

    @Override
    public void searchPrev(String keyword, TextSearchOptions options) {
        // The case-sensitive is Weired here, but it works!
        Pattern pattern = SearchUtils.string2pattern(keyword, options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
        mindMapView.findTopicByPattern(pattern, options, true);
    }

    @Override
    public void replaceSelection(String keywords, TextSearchOptions options, String replacement) {
        mindMapView.replaceSelection(keywords, options, replacement);
        Pattern pattern = SearchUtils.string2pattern(keywords, options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
        mindMapView.findTopicByPattern(pattern, options, false);
    }

    @Override
    public void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement) {
        mindMapView.replaceAll(keywords, searchOptions, replacement);
    }

    @Override
    public boolean isSelected() {
        return !CollectionUtils.isEmpty(mindMapView.getSelection());
    }

    @Override
    public boolean isUndoAvailable() {
        return mindMapView.isUndoAvailable();
    }

    @Override
    public boolean isRedoAvailable() {
        return mindMapView.isRedoAvailable();
    }

    @Override
    public void undo() {
        if (mindMapView.isFocused()) {
            mindMapView.undo();
        }
    }

    @Override
    public void redo() {
        if (mindMapView.isFocused()) {
            mindMapView.redo();
        }
    }

    @Override
    public boolean copy() {
        // only focused can copy, cut and paste, otherwise the focused dialog will not work corretly.
        if (mindMapView.isFocused()) {
            mindMapView.copy();
            return true;
        }
        return false;
    }

    @Override
    public boolean paste() {
        if (mindMapView.isFocused()) {
            mindMapView.paste();
            return true;
        }
        return false;
    }

    @Override
    public boolean cut() {
        if (mindMapView.isFocused()) {
            mindMapView.cut();
            return true;
        }
        return false;
    }

    @Override
    public void save() throws IOException {
//        if (mindMapView.isFocused()) {
        ModelManager.fixWrongTopics(mindMapView.getModel().getRoot());
        mindMapView.save(editorContext.getFileData().getFile());
        isChanged = false;
        fileSavedEventHandler.onFileSaved(this.editorContext.getFileData());
//        }
    }

    @Override
    public void onSnippet(Snippet snippet) {
        if (snippet instanceof ImageSnippet is) {
            String selectedIconCode = is.getCode();
            ((ExtraMindMapView) this.mindMapView).setIconForSelectedTopics(selectedIconCode);
            this.mindMapView.requestFocus();
        }
        else {
            // since the ImageSnippet only works for JFX, it cant be imported to core module, so the custom
            // image snippet needs to be handled separately.
            if (StringUtils.isNotBlank(snippet.getFilePath())) {
                AttributeUtils.loadImageFileToSelectedTopics((ExtensionContext) mindMapView, new File(snippet.getFilePath()), false);
            }
            else {
                mindMapView.appendTextAsTopicTree(snippet.getCode(), "");
            }
        }
    }

    @Override
    public void export() {

    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> {
            if (mindMapView != null) {
                mindMapView.requestFocus();
            }
        });
    }

    @Override
    public void dispose() {
        if (threadPoolService != null) threadPoolService.close();
        if (mindMapView != null) mindMapView.dispose();
        MindmapEvents.unsubscribeMmdSaveEvent(mindMapView);
        System.gc();
    }

    @Override
    public String getSelectionText() {
        return mindMapView.getSelectedInputText() == null ? null : mindMapView.getSelectedInputText();
    }

    public MindMap<TopicNode> getMindMapModel() {
        return mindMapView.getModel();
    }
}
