package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.NotificationType;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mfx.util.TextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;

/**
 * RichTextFX References:
 * <a href="https://github.com/FXMisc/RichTextFX">RichTextFX</a>
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BaseCodeAreaEditor extends BaseEditor {

    public static final int HISTORY_MERGE_DELAY_IN_MILLIS = 200;
    private final Logger log = LoggerFactory.getLogger(BaseCodeAreaEditor.class);

    @FXML
    protected SearchableCodeArea codeArea;

    protected boolean acceptDraggingFiles = false;

    private final EventSource<String> historySource = new EventSource<>();

//    protected String fontPrefKey;

    public BaseCodeAreaEditor(String fxmlResourcePath, EditorContext editorContext, boolean acceptDraggingFiles) {
        super(fxmlResourcePath, editorContext);
        this.acceptDraggingFiles = acceptDraggingFiles;
//        codeArea.setShowCaret(Caret.CaretVisibility.ON);
        codeArea.getUndoManager().preventMerge();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setDisablePaste(true); // only works for macOS
//        codeArea.setStyle("-fx-tab-size: 2"); doesn't work

        codeArea.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, LINE_DELETE, LINES_MOVE);

        codeArea.undoAvailableProperty().addListener((observable, oldValue, undoAvailable) -> {
            if (oldValue != undoAvailable) {
                EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.UNDO, undoAvailable);
            }
        });

        codeArea.redoAvailableProperty().addListener((observable, oldValue, redoAvailable) -> {
            if (oldValue != redoAvailable) {
                EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.REDO, redoAvailable);
            }
        });

        codeArea.setWrapText(true);
        codeArea.setOnContextMenuRequested(event -> {
            ContextMenu codeContextMenu = createCodeContextMenu();
            Node node = (Node) event.getSource();
            codeContextMenu.show(node.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        });

        if (acceptDraggingFiles) {
            // handles drag&drop files
            codeArea.setOnDragOver(dragEvent -> {
                if (CollectionUtils.isEmpty(dragEvent.getDragboard().getFiles())) {
                    return;
                }
                Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(dragEvent.getDragboard().getFiles().get(0));
                if (optPath.isPresent()) {
                    dragEvent.acceptTransferModes(TransferMode.LINK);
                    CharacterHit hit = codeArea.hit(dragEvent.getX(), dragEvent.getY());
                    codeArea.requestFocus();
                    codeArea.moveTo(hit.getInsertionIndex());
                }
            });
            codeArea.setOnDragDropped(dragEvent -> {
                CharacterHit hit = codeArea.hit(dragEvent.getX(), dragEvent.getY());
                for (File file : dragEvent.getDragboard().getFiles()) {
                    Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(file);
                    if (optPath.isPresent()) {
                        onFilesDropped(hit, file, optPath.get());
                    }
                    else {
                        log.warn("Link files not in same workspace are not supported yet");
                    }
                }
            });
        }

        historySource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(HISTORY_MERGE_DELAY_IN_MILLIS))
                .subscribe(s -> {
                    this.codeArea.getUndoManager().preventMerge();
                });

//        if (!codeArea.addSelection(extraSelection)) {
//            throw new IllegalStateException("selection was not added to area");
//        }
    }

    /**
     * @param file
     * @param filePath final path to present the provided file in this editor.
     */
    protected void onFilesDropped(CharacterHit hit, File file, String filePath) {
        // INHERIT ME
    }

    @Override
    public void loadFile(Runnable afterLoading) throws IOException {
        String text = TextUtils.convertFromWindows(FileUtils.readFileToString(editorContext.getFileData().getFile(), StandardCharsets.UTF_8));
        Platform.runLater(() -> {
            this.codeArea.replaceText(text);
            this.codeArea.displaceCaret(0); // caret starts at head of the file.
            this.codeArea.getUndoManager().forgetHistory();
            this.codeArea.getUndoManager().mark();
            // notify file loaded event for scrolling to top after file content is loaded.
            EventBus.getIns().notify(NotificationType.FILE_LOADED);

            this.refresh(text);
            // add text change listener should after CodeArea init content.
            this.codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!StringUtils.equals(oldValue, newValue)) {
                    historySource.push(newValue);
                    refresh(newValue);
                    isChanged = true;
                    fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                    EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.UNDO, this.codeArea.getUndoManager().isUndoAvailable());
                }
            });
            this.codeArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
                log.debug("%s-%s within %d".formatted(newValue.getStart(), newValue.getEnd(), codeArea.getText().length()));
                if (newValue.getEnd() > codeArea.getText().length()) {
                    return; // there is a bug in RichTextFx, which is, when selection to the end of text, this listener will be called twice, and the first one is wrong.
                }
                EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.CUT, newValue.getLength() > 0);
                EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.COPY, newValue.getLength() > 0);
            });
            int i = 0;
            for (Paragraph<Collection<String>, String, Collection<String>> paragraph : codeArea.getParagraphs()) {
                i++;
                if (log.isTraceEnabled()) log.trace("%d: %s".formatted(i, paragraph.getText()));
            }
            afterLoading.run();
            this.editorReadyEventHandler.onEditorReady();
        });
    }

    private ContextMenu createCodeContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem miCut = new MenuItem("Cut", FontIconManager.getIns().getIcon(IconKey.CUT));
        MenuItem miCopy = new MenuItem("Copy", FontIconManager.getIns().getIcon(IconKey.COPY));
        MenuItem miPaste = new MenuItem("Paste", FontIconManager.getIns().getIcon(IconKey.PASTE));
        MenuItem miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
        CheckMenuItem miWordWrap = new CheckMenuItem("Word Wrap");
        miCut.setOnAction(event -> {
            this.cut();
        });
        miCopy.setOnAction(event -> {
            this.copy();
        });
        miPaste.setOnAction(event -> {
            this.paste();
        });
        miDelete.setOnAction(event -> {
            codeArea.replaceSelection(StringUtils.EMPTY);
        });
        miCut.setDisable(codeArea.getSelection().getLength() == 0);
        miCopy.setDisable(codeArea.getSelection().getLength() == 0);
        miDelete.setDisable(codeArea.getSelection().getLength() == 0);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        miPaste.setDisable(!clipboard.hasContent(DataFormat.PLAIN_TEXT));
        miWordWrap.selectedProperty().bindBidirectional(codeArea.wrapTextProperty());
        menu.getItems().addAll(miCut, miCopy, miPaste, miDelete, new SeparatorMenuItem(), miWordWrap);
        return menu;
    }

    /**
     * Refresh anything from text if needed.
     *
     * @param text
     */
    protected abstract void refresh(String text);

    @Override
    public void refresh() {
        String fontPrefKey = getFontPrefKey();
        if (getFontPrefKey() != null) {
            Font defFont = FontConstants.DEFAULT_FONTS.get(fontPrefKey);
            Font font = fxPreferences.getPreference(fontPrefKey, Font.class, defFont);
            log.debug("set font: %s".formatted(font));
            codeArea.setStyle(FontUtils.fontToCssStyle(font));
        }
    }

    public abstract String getFontPrefKey();

    @Override
    public void searchNext(String keyword, TextSearchOptions options) {
        codeArea.searchNext(keyword, options);
    }

    @Override
    public void searchPrev(String keyword, TextSearchOptions options) {
        codeArea.searchPrev(keyword, options);
    }

    @Override
    public void replaceSelection(String keywords, TextSearchOptions searchOptions, String replacement) {
        if (!isSelected()) {
            codeArea.searchNext(keywords, searchOptions); // select first for replacement
        }
        if (!codeArea.replaceSelection(keywords, searchOptions.isCaseSensitive(), replacement)) {
            log.debug("no text replaced");
        }
        codeArea.searchNext(keywords, searchOptions);
    }

    @Override
    public void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement) {
        codeArea.replaceAllMatch(keywords, searchOptions, replacement);
    }

    @Override
    public boolean isSelected() {
        return codeArea.getSelection().getLength() > 0;
    }

    @Override
    public boolean isUndoAvailable() {
        return codeArea.isUndoAvailable();
    }

    @Override
    public boolean isRedoAvailable() {
        return codeArea.isRedoAvailable();
    }

    @Override
    public void undo() {
        codeArea.undo();
    }

    @Override
    public void redo() {
        codeArea.redo();
    }

    @Override
    public boolean cut() {
        if (codeArea.isFocused()) {
            codeArea.cut();
        }
        return true;
    }

    @Override
    public boolean copy() {
        if (codeArea.isFocused()) {
            codeArea.copy();
        }
        return true;
    }

    @Override
    public boolean paste() {
        if (codeArea.isFocused()) {
            codeArea.paste();
        }
        return true;
    }

    @Override
    public void save() throws IOException {
        log.info("Save file: " + editorContext.getFileData().getFile());
        FileUtils.write(editorContext.getFileData().getFile(),
                TextUtils.convertToWindows(codeArea.getText()), StandardCharsets.UTF_8);
        super.isChanged = false;
        fileSavedEventHandler.onFileSaved(this.editorContext.getFileData());
    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> codeArea.requestFocus());
    }

    @Override
    public void dispose() {
        log.info("Dispose editor: %s".formatted(this.getClass().getName()));
        codeArea.dispose();
    }

    @Override
    public String getSelectionText() {
        return codeArea.getSelectedText();
    }
}
