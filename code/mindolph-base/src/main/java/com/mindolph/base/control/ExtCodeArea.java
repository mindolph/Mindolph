package com.mindolph.base.control;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mfx.util.TextUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mindolph.base.constant.ShortcutConstants.*;
import static javafx.scene.input.KeyCode.TAB;

/**
 *
 * @author mindolph.com@gmail.com
 */
public class ExtCodeArea extends CodeArea {

    private final Logger log = LoggerFactory.getLogger(ExtCodeArea.class);

    private final ShortcutManager sm = ShortcutManager.getIns();

    // disable 'paste' shortcut in code area control, just a workaround for the shortcut conflict on macOS.
    private final BooleanProperty disablePaste = new SimpleBooleanProperty(false);

    public ExtCodeArea() {
        // auto scroll when caret goes out of viewport.
        super.caretPositionProperty().addListener((observableValue, integer, t1) -> ExtCodeArea.super.requestFollowCaret());
        this.setOnContextMenuRequested(contextMenuEvent -> {
            ContextMenu codeContextMenu = createContextMenu();
            Node node = (Node) contextMenuEvent.getSource();
            codeContextMenu.show(node.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }

    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem miCut = new MenuItem("Cut", FontIconManager.getIns().getIcon(IconKey.CUT));
        MenuItem miCopy = new MenuItem("Copy", FontIconManager.getIns().getIcon(IconKey.COPY));
        MenuItem miPaste = new MenuItem("Paste", FontIconManager.getIns().getIcon(IconKey.PASTE));
        MenuItem miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
        CheckMenuItem miWordWrap = new CheckMenuItem("Word Wrap", FontIconManager.getIns().getIcon(IconKey.WRAP));
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
            this.replaceSelection(StringUtils.EMPTY);
        });
        miCut.setDisable(this.getSelection().getLength() == 0);
        miCopy.setDisable(this.getSelection().getLength() == 0);
        miDelete.setDisable(this.getSelection().getLength() == 0);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        miPaste.setDisable(!clipboard.hasContent(DataFormat.PLAIN_TEXT));
        miWordWrap.selectedProperty().bindBidirectional(this.wrapTextProperty());
        menu.getItems().addAll(miCut, miCopy, miPaste, miDelete, new SeparatorMenuItem(), miWordWrap);
        return menu;
    }

    public void addFeatures(FEATURE... features) {
        List<InputMap<KeyEvent>> inputMaps = new ArrayList<>();
        for (FEATURE feature : features) {
            switch (feature) {
                case TAB_INDENT:
                    InputMap<KeyEvent> indent = InputMap.consume(EventPattern.keyPressed(TAB), e -> {
                        if (this.getSelection().getLength() > 0) {
                            addOrTrimHeadToParagraphs(true, new Replacement("\t", "\t", "  "));
                        }
                        else {
                            this.insertText(this.getCaretPosition(), "\t");
                        }
                    });
                    InputMap<KeyEvent> unIndent = InputMap.consume(EventPattern.keyPressed(new KeyCodeCombination(TAB, KeyCombination.SHIFT_DOWN)), e ->
                            addOrTrimHeadToParagraphs(false, new Replacement("\t", "\t", "  ", " ")));
                    inputMaps.add(indent);
                    inputMaps.add(unIndent);
                    break;
                case QUOTE:
                    InputMap<KeyEvent> quote2 = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_QUOTE)), e -> {
                        log.debug(e.getText());
                        addToSelectionHeadAndTail("'");
                    });
                    inputMaps.add(quote2);
                    break;
                case DOUBLE_QUOTE:
                    InputMap<KeyEvent> quote = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_DOUBLE_QUOTE)), e -> {
                        log.debug(e.getText());
                        addToSelectionHeadAndTail("\"");
                    });
                    inputMaps.add(quote);
                    break;
                case BACK_QUOTE:
                    InputMap<KeyEvent> backQuote = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_BACK_QUOTE)), keyEvent -> {
                        log.debug("CTRL + back quote key pressed");
                        keyEvent.consume();
                        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
                        if (caretSelectionBind.getEndParagraphIndex() > caretSelectionBind.getStartParagraphIndex()) {
                            addToSelectionHeadAndTail("```");
                        }
                        else {
                            addToSelectionHeadAndTail("`");
                        }
                    });

                    // TODO this doesn't work and don't know why
//                    InputMap<KeyEvent> backQuote = InputMap.sequence(InputMap.consume(EventPattern.keyPressed(BACK_QUOTE), keyEvent -> {
//                        log.debug("Back quote key pressed: " + keyEvent.getText());
//                        keyEvent.consume();
//                        addToSelectionHeadAndTail("`");
//                    }));
                    inputMaps.add(backQuote);
                    break;
                case AUTO_INDENT:
                    InputMap<KeyEvent> enter = InputMap.consume(EventPattern.keyPressed(KeyCode.ENTER), keyEvent -> {
                        int curParIdx = this.getCurrentParagraph();
                        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
                        String head = TextUtils.subStringBlankHead(paragraph.getText());
                        log.trace("add to head: <%s>".formatted(head));
                        this.replaceSelection("\n" + head);
                    });
                    inputMaps.add(enter);
                    break;
                case LINE_DELETE:
                    InputMap<KeyEvent> lineDelete = InputMap.consume(
                            EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_DELETE_LINE)), keyEvent -> {
                                deleteCurrentLine();
                            });
                    inputMaps.add(lineDelete);
                    break;
                case LINES_MOVE:
                    KeyCodeCombination moveUp = sm.getKeyCombination(KEY_EDITOR_MOVE_LINE_UP);
                    KeyCodeCombination moveDown = sm.getKeyCombination(KEY_EDITOR_MOVE_LINE_DOWN);
                    InputMap<KeyEvent> lineMoveUp = InputMap.consume(
                            EventPattern.keyPressed(moveUp), keyEvent -> {
                                log.debug("move lines up");
                                moveCaretSelection(true);
                            });
                    InputMap<KeyEvent> lineMoveDown = InputMap.consume(
                            EventPattern.keyPressed(moveDown), keyEvent -> {
                                log.debug("move lines down");
                                moveCaretSelection(false);
                            });
                    inputMaps.add(lineMoveUp);
                    inputMaps.add(lineMoveDown);
                default:
                    break;
            }
            // somehow on macOS, the shortcut event not only consumed by editor, but also consumed by application,
            // which causes the paste action be performed twice.
            // so disable PASTE shortcut on macOS to avoid conflict with global.
            if (disablePaste.get() && SystemUtils.IS_OS_MAC) {
                inputMaps.add(InputMap.consume(
                        EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_PASTE)), Event::consume
                ));
            }
            Nodes.addInputMap(this, InputMap.sequence(inputMaps.toArray(new InputMap[]{})));
        }
    }

    /**
     * Move caret selected paragraphs up/down.
     *
     * @param isUp
     */
    private void moveCaretSelection(boolean isUp) {
        CaretSelectionBind<Collection<String>, String, Collection<String>> selectionBind = getCaretSelectionBind();
        int caretCol = this.getCaretColumn();
        int startParIndex = selectionBind.getStartParagraphIndex();
        int startColIndex = selectionBind.getStartColumnPosition();
        int endParIndex = selectionBind.getEndParagraphIndex();
        int endColIndex = selectionBind.getEndColumnPosition();
        int endParLen = getParagraphLength(endParIndex);
        int newStartPar = startParIndex + (isUp ? -1 : 1);
        int newEndPar = endParIndex + (isUp ? -1 : 1); // for selection
        int selectionLen = selectionBind.getLength();

        try {
            if (newStartPar >= 0
                    && (newEndPar < this.getParagraphs().size())) {
                String toBeMoved = this.getText(startParIndex, 0, endParIndex, endParLen);
                log.trace("to be moved text: '%s'".formatted(StringUtils.abbreviateMiddle(toBeMoved, "...", 50)));
                log.trace("delete text from %d,%d to %d,%d".formatted(startParIndex, 0, endParIndex, endParLen + 1));
                this.deleteLinesSafely(startParIndex, endParIndex);
                log.trace("insert lines at: %d".formatted(newStartPar));
                this.insertLinesSafely(newStartPar, toBeMoved);
                log.trace("length of selection: " + selectionBind.getLength());
                if (selectionLen > 0) {
                    log.trace("Select text from %d,%d to %d,%d".formatted(newStartPar, startColIndex, newEndPar, endColIndex));
                    this.selectRange(newStartPar, startColIndex, newEndPar, endColIndex);
                }
                else {
                    this.moveTo(newStartPar, caretCol);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete lines from startParIndex to endParIndex.
     *
     * @param startParIndex
     * @param endParIndex
     */
    private void deleteLinesSafely(int startParIndex, int endParIndex) {
        int endParLen = getParagraphLength(endParIndex);
        boolean isLastLine = endParIndex == getParagraphs().size() - 1;
        int endLineLen = isLastLine ? endParLen : endParLen + 1;// condition for last line
        int startColIndex = isLastLine ? -1 : 0;
        this.deleteText(startParIndex, startColIndex, endParIndex, endLineLen);
    }

    /**
     * delete current line and return deleted text.
     *
     * @return without line break.
     */
    private String deleteCurrentLine() {
        int curParIdx = this.getCurrentParagraph();
        int lineStart = this.getAbsolutePosition(curParIdx, 0);
        int lineEnd = this.getAbsolutePosition(curParIdx, this.getCurrentLineEndInParargraph() + 1); // +1 include the line break;
        log.trace("%d-%d".formatted(lineStart, lineEnd));
        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
        String lineText = paragraph.getText();
        this.deleteText(lineStart, lineEnd);
        return lineText;
    }

    /**
     * Insert text to parIndex.
     *
     * @param parIndex
     * @param text
     */
    private void insertLinesSafely(int parIndex, String text) {
        int lines = getParagraphs().size();
        if (parIndex >= lines) {
            // insert new line for end of text area
            this.appendText(StringUtils.repeat(TextConstants.LINE_SEPARATOR, 1));
            this.insertText(parIndex, 0, text);
        }
        else {
            this.insertText(parIndex, 0, text + TextConstants.LINE_SEPARATOR);
        }
    }

    /**
     * if any paragraph in selection has no {@code text} start with, the operation will be trimming instead of adding.
     *
     * @param params text to add to or trim from head of selected paragraphs.
     */
    public void addOrTrimHeadToParagraphsIfAdded(Replacement params) {
        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
        boolean isAlreadyAdded = true;
        for (int i = caretSelectionBind.getStartParagraphIndex(); i < caretSelectionBind.getEndParagraphIndex() + 1; i++) {
            Paragraph<Collection<String>, String, Collection<String>> p = this.getParagraph(i);
            if (!StringUtils.startsWithAny(p.getText(), params.getTargets())) {
                isAlreadyAdded = false;
            }
        }
        boolean needAdd = !isAlreadyAdded;
        addOrTrimHeadToParagraphs(needAdd, params);
    }

    /**
     * Add or trim {@code text} to or from head of selected paragraphs.
     *
     * @param isAdd  true to add, false to trim
     * @param params
     */
    public void addOrTrimHeadToParagraphs(boolean isAdd, Replacement params) {
        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
        int startPar = caretSelectionBind.getStartParagraphIndex();
        int endPar = caretSelectionBind.getEndParagraphIndex();
        boolean hasSelection = this.getSelection().getLength() != 0;
        if (hasSelection) log.debug("Selected from %s to %s".formatted(startPar, endPar));

        List<String> newLines = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>();
        for (int i = startPar; i < endPar + 1; i++) {
            Paragraph<Collection<String>, String, Collection<String>> p = this.getParagraph(i);
            String newLine;
            if (isAdd) {
                newLine = params.getSubstitute() + p.getText();
            }
            else {
                newLine = p.getText();
                for (String target : params.getTargets()) {
                    if (StringUtils.startsWith(p.getText(), target)) {
                        newLine = StringUtils.replaceOnce(p.getText(), target, StringUtils.EMPTY);
                        break;
                    }
                }
            }
            newLines.add(newLine);
            offsets.add(newLine.length() - p.getText().length()); // calc offset for each line(but only head and tail will be used)
        }
        if (CollectionUtils.isEmpty(newLines)) {
            return;
        }

        int startOffset = offsets.get(0);
        int endOffset = offsets.get(offsets.size() - 1);
        int startInFirstPar = Math.max(this.getParagraphSelection(startPar).getStart() + startOffset, 0);
        int endInLastPar = Math.max(this.getParagraphSelection(endPar).getEnd() + endOffset, 0);
        int caretInPar = this.getCaretPosition() + endOffset; // this is for non-selection condition only.
        // calculate for replacement
        int start = this.getAbsolutePosition(startPar, 0);
        int end = this.getAbsolutePosition(endPar, 0) + this.getParagraph(endPar).length();
        String replacement = StringUtils.join(newLines, TextConstants.LINE_SEPARATOR);
        this.replaceText(start, end, replacement);
        // reselect new range
        if (hasSelection) {
            log.debug("Select from %d,%d to %d,%d".formatted(startPar, startInFirstPar, endPar, endInLastPar));
            this.selectRange(startPar, startInFirstPar, endPar, endInLastPar);
        }
        else {
            log.debug("Move caret to %d".formatted(caretInPar));
            this.moveTo(caretInPar);
        }
    }

    public void addToSelectionHead(String text) {
        IndexRange selection = this.getSelection();
        this.insertText(selection.getStart(), text);
        this.selectRange(selection.getStart(), selection.getEnd() + text.length());
    }

    public void addToSelectionTail(String text) {
        IndexRange selection = this.getSelection();
        this.insertText(selection.getEnd() + 1, text);
        this.selectRange(selection.getStart(), selection.getEnd() + text.length());
    }

    public void addToSelectionHeadAndTail(String text) {
        IndexRange selection = this.getSelection();
        String selectedText = super.getSelectedText();
        super.replaceSelection(text + selectedText + text);
        this.selectRange(selection.getStart(), selection.getEnd() + text.length() * 2);
    }

    public boolean isDisablePaste() {
        return disablePaste.get();
    }

    public BooleanProperty disablePasteProperty() {
        return disablePaste;
    }

    public void setDisablePaste(boolean disablePaste) {
        this.disablePaste.set(disablePaste);
    }

    public enum FEATURE {
        // indent or un-indent by TAB key
        TAB_INDENT,
        // quote by '
        QUOTE,
        // quote by "
        DOUBLE_QUOTE,
        // quote by `
        BACK_QUOTE,
        // auto indent when starts new line
        AUTO_INDENT,
        // delete current line
        LINE_DELETE,
        // move current line
        LINES_MOVE
    }

    public static class Replacement {
        String substitute;
        String[] targets;

        public Replacement(String substitute, String... targets) {
            this.substitute = substitute;
            if (targets == null || targets.length == 0) {
                this.targets = new String[]{substitute};
            }
            else {
                this.targets = targets;
            }
        }

        public String getSubstitute() {
            return substitute;
        }

        public void setSubstitute(String substitute) {
            this.substitute = substitute;
        }

        public String[] getTargets() {
            return targets;
        }

        public void setTargets(String[] targets) {
            this.targets = targets;
        }
    }
}
