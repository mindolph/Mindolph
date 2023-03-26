package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.NotificationType;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mfx.util.TextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;

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

    private final EventSource<String> historySource = new EventSource<>();

//    protected String fontPrefKey;

    public BaseCodeAreaEditor(String fxmlResourcePath, EditorContext editorContext) {
        super(fxmlResourcePath, editorContext);
//        codeArea.setShowCaret(Caret.CaretVisibility.ON);
        codeArea.getUndoManager().preventMerge();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
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
        codeArea.setContextMenu(createCodeContextMenu());

        historySource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(HISTORY_MERGE_DELAY_IN_MILLIS))
                .subscribe(s -> {
                    this.codeArea.getUndoManager().preventMerge();
                });

//        if (!codeArea.addSelection(extraSelection)) {
//            throw new IllegalStateException("selection was not added to area");
//        }
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
        CheckMenuItem miWordWrap = new CheckMenuItem("Word Wrap");
        miWordWrap.selectedProperty().bindBidirectional(codeArea.wrapTextProperty());
        menu.getItems().add(miWordWrap);
        return menu;
    }

    /**
     * Refresh anything from text if needed.
     *
     * @param text
     */
    protected abstract void refresh(String text);

    @Override
    public void reload() {
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
        codeArea.cut();
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
