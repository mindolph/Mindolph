package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.model.OutlineItemData;
import com.mindolph.core.model.Snippet;
import com.mindolph.core.search.*;
import com.mindolph.mfx.util.TextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.TransferMode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;

/**
 * RichTextFX References:
 * <a href="https://github.com/FXMisc/RichTextFX">RichTextFX</a>
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BaseCodeAreaEditor extends BaseEditor {

    private static final Logger log = LoggerFactory.getLogger(BaseCodeAreaEditor.class);

    @FXML
    protected SearchableCodeArea codeArea;

    protected boolean acceptDraggingFiles = false;

    private Pattern outlinePattern; // used to extract outline

//    protected String fontPrefKey;

    public BaseCodeAreaEditor(String fxmlResourcePath, EditorContext editorContext, boolean acceptDraggingFiles) {
        super(fxmlResourcePath, editorContext);
        this.acceptDraggingFiles = acceptDraggingFiles;
//        codeArea.setShowCaret(Caret.CaretVisibility.ON);
        codeArea.getUndoManager().preventMerge();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setDisablePaste(true); // only works for macOS
        codeArea.setDisableUndo(true);
        codeArea.setDisableRedo(true);
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

        if (acceptDraggingFiles) {
            // handles drag&drop files
            codeArea.setOnDragOver(dragEvent -> {
                if (CollectionUtils.isEmpty(dragEvent.getDragboard().getFiles())) {
                    return;
                }
                dragEvent.acceptTransferModes(TransferMode.LINK);
                CharacterHit hit = codeArea.hit(dragEvent.getX(), dragEvent.getY());
                codeArea.requestFocus();
                codeArea.moveTo(hit.getInsertionIndex());
            });
            codeArea.setOnDragDropped(dragEvent -> {
                CharacterHit hit = codeArea.hit(dragEvent.getX(), dragEvent.getY());
                log.info("Dropped %d files to code area.".formatted(dragEvent.getDragboard().getFiles().size()));
                onFilesDropped(hit, dragEvent.getDragboard().getFiles());
            });
        }

//        if (!codeArea.addSelection(extraSelection)) {
//            throw new IllegalStateException("selection was not added to area");
//        }
    }

    /**
     * @param file
     * @param filePath final path to present the provided file in this editor.
     */
    protected void onFileDropped(CharacterHit hit, File file, String filePath) {
        // INHERIT ME TO HANDLE DROPPED FILE
    }

    protected void onFilesDropped(CharacterHit hit, List<File> files) {
        // INHERIT ME TO HANDLE DROPPED FILES
    }

    @Override
    public void loadFile() throws IOException {
        log.debug("Load file: %s".formatted(editorContext.getFileData().getFile()));
        String text = super.loadByOs(FileUtils.readFileToString(editorContext.getFileData().getFile(), StandardCharsets.UTF_8));
        if (StringUtils.isNoneBlank(this.getOutlinePattern()))
            outlinePattern = Pattern.compile(this.getOutlinePattern());
        Platform.runLater(() -> {
            this.codeArea.replaceText(text);
            this.applyStyles();
            this.codeArea.displaceCaret(0); // caret starts at head of the file.
            this.codeArea.getUndoManager().forgetHistory();
            this.codeArea.getUndoManager().mark();

            // refresh before listening the text change event.
            this.refresh(text);

            // add text change listener should after CodeArea init content.
            this.codeArea.textProperty().addListener((observable, oldValue, newText) -> {
                if (!StringUtils.equals(oldValue, newText)) {
                    this.codeArea.doHistory();
                    log.debug("Refresh editor since text are changed.");
                    refresh(newText);
                    isChanged = true;
                    fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                    this.outline();
                    EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.UNDO, this.codeArea.getUndoManager().isUndoAvailable());
                }
            });
            this.codeArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
                log.trace("%s-%s within %d".formatted(newValue.getStart(), newValue.getEnd(), codeArea.getText().length()));
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

            // emit file content loaded event for like scrolling to pos of anchor or updating menu state, etc.
            EventBus.getIns().notifyFileLoaded(editorContext.getFileData());
            this.outline();
        });
    }


    /**
     * Refresh anything from text if needed.
     *
     * @param text
     */
    protected abstract void refresh(String text);

    protected abstract String getOutlinePattern();

    protected abstract String getHeadingLevelTag();

    protected abstract String extractOutlineTitle(String heading, TextLocation location, TextLocation nextBlockLocation);

    @Override
    public void outline() {
        if (StringUtils.isBlank(this.getOutlinePattern()) || outlinePattern == null) {
            Tree tree = new Tree();
            tree.init("");
            tree.getRootNode().addChild(new Node(new OutlineItemData("No outlines for this file")));
            EventBus.getIns().notifyOutline(tree);
            return;
        }
        threadPoolService.execute(() -> {
            Matcher matcher = outlinePattern.matcher(codeArea.getText());
            if (!matcher.hasMatch()) {
                log.warn("No outline found by: %s".formatted(getOutlinePattern()));
            }
            Tree tree = new Tree();
            Node root = new Node("Stub");
            root.setLevel(0);
            tree.init(root);

            Node curNode = root;
            TextNavigator tn = new TextNavigator(); // locating the found heading
            tn.setText(codeArea.getText(), true);
            List<HeadingLocation> l = new ArrayList<>();

            while (matcher.find()) {
                String heading = matcher.group(2);// TODO this 2 should be...
                int n = TextUtils.countInStarting(heading, this.getHeadingLevelTag());
                TextLocation textLocation = tn.locateNext(heading, true);
                textLocation.setStartCol(0);
                textLocation.setEndRow(textLocation.getStartRow());
                textLocation.setEndCol(textLocation.getEndCol() + 1);
                l.add(new HeadingLocation(heading, n, textLocation));
            }
            for (int i = 0; i < l.size(); i++) {
                HeadingLocation hl = l.get(i);
                String title = this.extractOutlineTitle(hl.heading, hl.textLocation(), (i + 1) < l.size() ? l.get(i + 1).textLocation : null);
                if (log.isTraceEnabled()) log.trace("  %d - %s".formatted(hl.level(), title));

                TextAnchor ta = new TextAnchor(hl.textLocation);
                OutlineItemData outlineItemData = new OutlineItemData(title, ta);

                Node newNode = new Node(outlineItemData);
                newNode.setLevel(hl.level);

                if (hl.level > curNode.getLevel()) {
                    // as child
                    log.trace("    as child");
                    curNode.addChild(newNode);
                    newNode.setParent(curNode);
                }
                else {
                    // as sibling
                    log.trace("    as sibling");
                    Node ancestor = curNode.findAncestor(node -> node.getLevel() <= newNode.getLevel() - 1);
                    ancestor.addChild(newNode);
                    newNode.setParent(ancestor);
                }
                curNode = newNode;
            }
            EventBus.getIns().notifyOutline(tree);
        });
    }

    @Override
    public void locate(Anchor anchor) {
        log.debug("Locate anchor: %s".formatted(anchor));
        if (anchor instanceof TextAnchor ta) {
            TextLocation tl = ta.getTextLocation();
            log.debug("Select range:  %s".formatted(tl));
            codeArea.selectRange(tl.getStartRow(), tl.getStartCol(), tl.getEndRow(), tl.getEndCol());
            codeArea.scrollXToPixel(0);
            codeArea.centerTheCaretY();
        }
        else {
            log.warn("No anchor to locate");
        }
    }

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
        if (StringUtils.isEmpty(keywords)) {
            return;
        }
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
    public void onSnippet(Snippet snippet) {
        // replacing selected text with snippet code
        String code = snippet.getCode();
        int caretPos = StringUtils.indexOf(code, "⨁");
        String codeToInsert = StringUtils.remove(code, "⨁");
        codeArea.replaceSelection(codeToInsert);
        if (caretPos > 0) {
            codeArea.moveTo(codeArea.getCaretPosition() - (codeToInsert.length() - caretPos));
        }
        codeArea.requestFocus();
    }

    @Override
    public void save() throws IOException {
        log.info("Save file: " + editorContext.getFileData().getFile());
        FileUtils.write(editorContext.getFileData().getFile(),
                this.convertByOs(codeArea.getText()), StandardCharsets.UTF_8);
        super.isChanged = false;
        fileSavedEventHandler.onFileSaved(this.editorContext.getFileData());
    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> codeArea.requestFocus());
    }

    @Override
    public void dispose() {
        log.info("Dispose editor: %s(%s)".formatted(this.getClass().getName(), this.hashCode()));
        if (threadPoolService != null) threadPoolService.close();
        codeArea.dispose();
    }

    @Override
    public String getSelectionText() {
        return codeArea.getSelectedText();
    }


    private record HeadingLocation(String heading, int level, TextLocation textLocation) {
    }
}
