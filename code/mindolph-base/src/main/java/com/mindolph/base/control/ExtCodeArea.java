package com.mindolph.base.control;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.TextUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.*;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.mindolph.base.constant.ShortcutConstants.*;
import static com.mindolph.core.constant.SyntaxConstants.BLANK_CHAR;
import static javafx.scene.input.KeyCode.TAB;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Supports:
 * Context Menu.
 * Features registration and handling.
 * Specific shortcuts for macOS.
 * Common methods for handing text.
 *
 * @author mindolph.com@gmail.com
 */
public class ExtCodeArea extends CodeArea {

    public static final int HISTORY_MERGE_DELAY_IN_MILLIS = 200;
    public static final int INPUT_HELP_DELAY_IN_MILLIS = 3000;

    static final int DIRECTION_UP = -1;
    static final int DIRECTION_DOWN = 1;

    private static final Logger log = LoggerFactory.getLogger(ExtCodeArea.class);

    private final ShortcutManager sm = ShortcutManager.getIns();

    // disable 'paste' shortcut in code area control, just a workaround for the shortcut conflict on macOS.
    // update: those issues are fixed since JavaFX 24, no need anymore.
    private final BooleanProperty disablePaste = new SimpleBooleanProperty(false);
    private final BooleanProperty disableRedo = new SimpleBooleanProperty(false);
    private final BooleanProperty disableUndo = new SimpleBooleanProperty(false);

    // used to control the merging of editing history.
    private final EventSource<String> historySource = new EventSource<>();


    public ExtCodeArea() {
        // auto scroll when caret goes out of viewport.
        super.caretPositionProperty().addListener((observableValue, integer, t1) -> super.requestFollowCaret());
        this.setOnContextMenuRequested(contextMenuEvent -> {
            // change the caret to where user click before popup context menu
            this.moveCaretByScreen(contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            ContextMenu codeContextMenu = this.createContextMenu();
            Node node = (Node) contextMenuEvent.getSource();
            codeContextMenu.show(node.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
        // the event should be emitted when the text is changed in the editor
        historySource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(HISTORY_MERGE_DELAY_IN_MILLIS))
                .subscribe(s -> this.getUndoManager().preventMerge());

    }


    public void refresh() {
        // DO NOTHING
    }

    public void refreshAsync() {
        // DO NOTHING
    }

    public void doHistory() {
        historySource.push(null);
    }

    /**
     * Override and call this method in subclass if you want to add more menu items.
     *
     * @return
     */
    protected ContextMenu createContextMenu() {
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
            this.replaceSelection(EMPTY);
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
//                case HELPER:
//                    InputMap<KeyEvent> showHelper = InputMap.consume(EventPattern.keyPressed(new KeyCodeCombination(ENTER, KeyCombination.META_DOWN)), e -> {
//
//                    });
//                    inputMaps.add(showHelper);
//                    break;
                case TAB_INDENT:
                    InputMap<KeyEvent> indent = InputMap.consume(EventPattern.keyPressed(TAB), e -> {
                        if (this.getSelection().getLength() > 0) {
                            addOrTrimHeadToParagraphs(new Replacement("\t", Arrays.asList("\t", "  ")), true);
                        }
                        else {
                            this.insertText(this.getCaretPosition(), "\t");
                        }
                    });
                    InputMap<KeyEvent> unIndent = InputMap.consume(EventPattern.keyPressed(new KeyCodeCombination(TAB, KeyCombination.SHIFT_DOWN)), e ->
                            addOrTrimHeadToParagraphs(new Replacement("\t", Arrays.asList("\t", "  ", " ")), false));
                    inputMaps.add(indent);
                    inputMaps.add(unIndent);
                    break;
                case QUOTE:
                    InputMap<KeyEvent> quote2 = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_QUOTE)), e -> {
                        log.debug(e.getText());
                        addToSelectionHeadAndTail("'", true);
                    });
                    inputMaps.add(quote2);
                    break;
                case DOUBLE_QUOTE:
                    InputMap<KeyEvent> quote = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_DOUBLE_QUOTE)), e -> {
                        log.debug(e.getText());
                        addToSelectionHeadAndTail("\"", true);
                    });
                    inputMaps.add(quote);
                    break;
                case BACK_QUOTE:
                    InputMap<KeyEvent> backQuote = InputMap.consume(EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_BACK_QUOTE)), keyEvent -> {
                        log.debug("CTRL + back quote key pressed");
                        keyEvent.consume();
                        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
                        if (caretSelectionBind.getEndParagraphIndex() > caretSelectionBind.getStartParagraphIndex()) {
                            addToSelectionHeadAndTail("```\n", "\n```", true);
                        }
                        else {
                            addToSelectionHeadAndTail("`", true);
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
                        String head = TextUtils.subStringBlankHead(getCurrentParagraphText());
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
        }
        // somehow on macOS, the shortcut event not only consumed by editor, but also consumed by application,
        // which causes the paste action be performed twice.
        // so disable (by consuming key event)  PASTE shortcut on macOS to avoid conflict with global.
        if (disablePaste.get() && SystemUtils.IS_OS_MAC) {
            inputMaps.add(InputMap.consume(
                    EventPattern.keyPressed(sm.getKeyCombination(KEY_EDITOR_PASTE)), Event::consume
            ));
        }
        // disable undo to avoid conflict with global shortcut
        if (disableUndo.get() && SystemUtils.IS_OS_MAC) {
            inputMaps.add(InputMap.consume(
                    EventPattern.keyPressed(sm.getKeyCombination(KEY_UNDO)), Event::consume
            ));
        }
        // disable redo to avoid conflict with global shortcut
        if (disableRedo.get() && SystemUtils.IS_OS_MAC) {
            inputMaps.add(InputMap.consume(
                    EventPattern.keyPressed(sm.getKeyCombination(KEY_REDO)), Event::consume
            ));
        }
        Nodes.addInputMap(this, InputMap.sequence(inputMaps.toArray(new InputMap[]{})));
    }

    protected String extractLastWordFromCaret() {
        int caretPosition = getCaretPosition();
        StringBuilder sb = new StringBuilder();
        String text = this.getText();
        if (caretPosition < 0 || caretPosition > text.length()) {
            return EMPTY;
        }
        if (StringUtils.isEmpty(text)) {
            return EMPTY;
        }
        while (caretPosition > 0 && isCharInWord(text.charAt(--caretPosition))) {
            sb.append(text.charAt(caretPosition));
        }
        return sb.reverse().toString();
    }

    private boolean isCharInWord(char c) {
        // somehow the space is printable, so just ignore it.
        return CharUtils.isAsciiPrintable(c) && c != ' ';
    }

    // @deprecated
    public static String extractLastWord(String text) {
        int i = StringUtils.lastIndexOfAny(text, " ", "\t") + 1;
        return StringUtils.substring(text, Math.max(0, i), text.length());
    }

    /**
     * Move caret up or down explicitly even in wrapped text.
     *
     * @param direction
     * @since 1.6.4
     */
    protected void moveCaret(int direction) {
        int idx = this.getCurrentParagraph();
        int currentLine = super.lineIndex(idx, super.getCaretColumn());
        int lineCount = super.getParagraphLinesCount(idx);
        if (idx == 0 && currentLine == 0 && direction == DIRECTION_UP) {
            return;
        }
        if (idx == this.getParagraphs().size() - 1
                && currentLine == lineCount - 1
                && direction == DIRECTION_DOWN) {
            return;
        }
        boolean isDirectionDown = direction == DIRECTION_DOWN;

        int lineCaret = this.getCaretInCurrentLine();
        boolean isMoveToPar = currentLine == (isDirectionDown ? lineCount - 1 : 0);
        int targetIdx = isMoveToPar ? idx + direction : idx;
        if (isMoveToPar) {
            log.debug("Move caret to next/prev paragraph: %d".formatted(targetIdx));
            super.moveTo(targetIdx, isDirectionDown ? 0 : Math.max(super.getParagraphLength(targetIdx) - 1, 0)); // move to head or tail of paragraph first
        }
        else {
            log.debug("Move caret to next/prev line");
            int targetCaret = isDirectionDown ? super.getCurrentLineEndInParargraph() + 1 : super.getCurrentLineStartInParargraph() - 1;
            super.moveTo(targetIdx, targetCaret); // move to end of prev line or start of next line first
        }
        super.moveTo(targetIdx, Math.min(super.getCurrentLineStartInParargraph() + lineCaret, super.getParagraphLength(targetIdx)));
    }

    /**
     * @param newParIdx
     * @deprecated
     */
    private void moveCaretToParagraph(int newParIdx) {
        Paragraph<Collection<String>, String, Collection<String>> targetPar = super.getParagraph(newParIdx);
        if (targetPar != null) {
            super.moveTo(newParIdx, Math.min(super.getCaretColumn(), targetPar.length()));
        }
    }

    /**
     * move caret to the text position by screen coordinate if the coordinate is not on selected text.
     *
     * @param x of screen
     * @param y of screen
     * @since 1.12.10
     */
    private void moveCaretByScreen(double x, double y) {
        Point2D p = super.screenToLocal(x, y);
        CharacterHit hit = super.hit(p.getX(), p.getY());
        if (super.getSelection().getLength() > 0) {
            Range<Integer> selectedRange = Range.of(super.getSelection().getStart(), super.getSelection().getEnd() - 1);
            if (selectedRange.contains(hit.getInsertionIndex())) {
                // don't move caret if click on selected text.
                return;
            }
        }
        if (log.isDebugEnabled()) log.debug("Hit (%f-%f) %d".formatted(p.getX(), p.getY(), hit.getInsertionIndex()));
        super.moveTo(hit.getInsertionIndex());
    }

    private int getCaretInCurrentLine() {
        return super.getCaretColumn() - super.getCurrentLineStartInParargraph();
    }

    private int getCurrentLineLength() {
        return super.getCurrentLineEndInParargraph() - super.getCurrentLineStartInParargraph();
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
                if (log.isTraceEnabled())
                    log.trace("to be moved text: '%s'".formatted(StringUtils.abbreviateMiddle(toBeMoved, "...", 50)));
                if (log.isTraceEnabled())
                    log.trace("delete text from %d,%d to %d,%d".formatted(startParIndex, 0, endParIndex, endParLen + 1));
                this.deleteLinesSafely(startParIndex, endParIndex);
                if (log.isTraceEnabled()) log.trace("insert lines at: %d".formatted(newStartPar));
                this.insertLinesSafely(newStartPar, toBeMoved);
                if (log.isTraceEnabled()) log.trace("length of selection: " + selectionBind.getLength());
                if (selectionLen > 0) {
                    if (log.isTraceEnabled())
                        log.trace("Select text from %d,%d to %d,%d".formatted(newStartPar, startColIndex, newEndPar, endColIndex));
                    this.selectRange(newStartPar, startColIndex, newEndPar, endColIndex);
                }
                else {
                    this.moveTo(newStartPar, caretCol);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Delete lines from startParIndex to endParIndex.
     *
     * @param startParIndex
     * @param endParIndex
     */
    private void deleteLinesSafely(int startParIndex, int endParIndex) {
        if (!super.isEditable()) return;
        int endParLen = super.getParagraphLength(endParIndex);
        boolean isLastLine = endParIndex == super.getParagraphs().size() - 1;
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
        if (!super.isEditable()) return null;
        int curParIdx = this.getCurrentParagraph();
        int lineStart = this.getAbsolutePosition(curParIdx, 0);
        int lineEnd = this.getAbsolutePosition(curParIdx, this.getCurrentLineEndInParargraph() + 1); // +1 include the line break;
        log.trace("%d-%d".formatted(lineStart, lineEnd));
        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
        String lineText = paragraph.getText();
        this.deleteText(lineStart, lineEnd);
        return lineText;
    }

    public void insertText(String text) {
        if (!super.isEditable()) return;
        this.insertText(this.getCaretPosition(), text);
    }

    /**
     * Insert text to parIndex.
     *
     * @param parIndex
     * @param text
     */
    private void insertLinesSafely(int parIndex, String text) {
        if (!super.isEditable()) return;
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
     * if any paragraph in selection has no {@code params.targets} start with, the operation will be trimming instead of adding.
     *
     * @param params text to add to or trim from the head of selected paragraphs.
     */
    public void addOrTrimHeadToParagraphsIfAdded(Replacement params) {
        if (!super.isEditable()) return;
        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
        // find if any paragraph already has the head that needs to be added.
        boolean isAlreadyAdded = true;
        for (int i = caretSelectionBind.getStartParagraphIndex(); i < caretSelectionBind.getEndParagraphIndex() + 1; i++) {
            Paragraph<Collection<String>, String, Collection<String>> p = this.getParagraph(i);
            if (params.getTargets() != null
                    && !startsWithAnyIgnoreBlank(p.getText(), params.getTargets())) {
                isAlreadyAdded = false;
            }
        }
        boolean needAddToHead = !isAlreadyAdded;
        this.addOrTrimHeadToParagraphs(params, needAddToHead);
    }

    // Whether starts with any target ignoring the blank in the front.
    private boolean startsWithAnyIgnoreBlank(String text, List<String> targets) {
        for (String target : targets) {
            Pattern pattern = Pattern.compile("^%s*%s.*".formatted(BLANK_CHAR, Pattern.quote(target)));
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param params
     * @param needAddToHead true to add to head, false to trim from head.
     */
    public void addOrTrimHeadToParagraphs(Replacement params, boolean needAddToHead) {
        if (!super.isEditable()) return;
        String tail = ObjectUtils.getIfNull(params.getTail(), EMPTY);
        addOrTrimHeadToParagraphs(params, true, parText -> {
            String tailOfLine = (parText.endsWith(tail) ? EMPTY : tail);
            if (needAddToHead) {
                // add substitute to the beginning of the visible characters in the paragraph (ignoring blank chars).
                return RegExUtils.replaceFirst(parText, "(^%s*)".formatted(BLANK_CHAR), "$1%s".formatted(params.getSubstitute())) + tailOfLine;
            }
            else {
                // replace targets with blank if given.
                for (String target : params.getTargets()) {
                    // check the beginning of the visible characters in the paragraph (ignoring blank chars).
                    Pattern pattern = Pattern.compile("^%s*%s.*".formatted(BLANK_CHAR, Pattern.quote(target)));
                    if (pattern.matcher(parText).find()) {
                        return Strings.CS.replaceOnce(parText, target, EMPTY) + tailOfLine;
                    }
                }
            }
            return parText;
        });
    }

    /**
     * Add or trim {@code text} to or from head of selected paragraphs.
     *
     * @param params
     * @param skipEmptyLine true to skip the empty line when processing, but if there is only one line, ignore this.
     * @param converter     Convert the text line,
     */
    public void addOrTrimHeadToParagraphs(Replacement params, boolean skipEmptyLine, Function<String, String> converter) {
        if (!super.isEditable()) return;
        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
        int startPar = caretSelectionBind.getStartParagraphIndex();
        int endPar = caretSelectionBind.getEndParagraphIndex();
        int parCount = endPar - startPar;
        boolean hasSelection = this.getSelection().getLength() != 0;
        if (hasSelection) log.debug("Selected from %s to %s".formatted(startPar, endPar));

        List<String> newPars = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>(); // offsets for each paragraph.
        for (int i = startPar; i < endPar + 1; i++) {
            String newPar;
            Paragraph<Collection<String>, String, Collection<String>> p = this.getParagraph(i);
            if (parCount > 1 && skipEmptyLine && StringUtils.isBlank(p.getText())) {
                newPar = EMPTY;
            }
            else {
                newPar = converter.apply(p.getText());
            }
            newPars.add(newPar);
            // calc offset for each line (but only head will be used)
            int offset = newPar.length() - p.getText().length() - ((hasSelection || params.tail == null) ? 0 : params.tail.length());
            offsets.add(offset);
        }
        if (CollectionUtils.isEmpty(newPars)) {
            return;
        }

        int startOffset = offsets.getFirst();
        int endOffset = offsets.getLast();
        int startInFirstPar = Math.max(super.getParagraphSelection(startPar).getStart() + startOffset, 0);
        int endInLastPar = Math.max(super.getParagraphSelection(endPar).getEnd() + endOffset, 0);
        int caretInPar = this.getCaretPosition() + endOffset; // this is for non-selection condition only.
        // calculate for replacement
        int start = this.getAbsolutePosition(startPar, 0);
        int end = this.getAbsolutePosition(endPar, 0) + this.getParagraph(endPar).length();
        String replacement = StringUtils.join(newPars, TextConstants.LINE_SEPARATOR);
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
        if (!super.isEditable()) return;
        IndexRange selection = this.getSelection();
        this.insertText(selection.getStart(), text);
        if (selection.getLength() > 0) {
            this.selectRange(selection.getStart(), selection.getEnd() + text.length());
        }
    }

    public void addToSelectionTail(String text) {
        if (!super.isEditable()) return;
        IndexRange selection = this.getSelection();
        super.replaceSelection(super.getSelectedText() + text);
        if (selection.getLength() > 0) {
            this.selectRange(selection.getStart(), selection.getEnd() + text.length());
        }
    }

    public void addToSelectionHeadAndTail(String text, boolean select) {
        if (!super.isEditable()) return;
        IndexRange selection = this.getSelection();
        String selectedText = super.getSelectedText();
        super.replaceSelection(text + selectedText + text);
        if (selection.getLength() > 0) {
            this.selectRange(selection.getStart(), selection.getEnd() + text.length() * 2);
        }
        else {
            this.moveTo(super.getCaretPosition() - text.length());
        }
    }

    public void addToSelectionHeadAndTail(String head, String tail, boolean select) {
        if (!super.isEditable()) return;
        IndexRange selection = this.getSelection();
        String selectedText = super.getSelectedText();
        super.replaceSelection(head + selectedText + tail);
        if (selection.getLength() > 0) {
            this.selectRange(selection.getStart(), selection.getEnd() + head.length() + tail.length());
        }
        else {
            this.moveTo(super.getCaretPosition() - tail.length());
        }
    }

    protected String getCurrentParagraphText() {
        int curParIdx = this.getCurrentParagraph();
        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
        return paragraph.getText();
    }

    // @since 1.7
    public Bounds getCaretInLocal() {
        return super.screenToLocal(getCaretBounds().orElse(BoundsUtils.newZero()));
    }

    // @since 1.7
    public double getLineHeight() {
        VirtualFlow<?, ?> vf = (VirtualFlow<?, ?>) this.lookup(".virtual-flow");
        return vf.visibleCells().getFirst().getNode().getLayoutBounds().getHeight();
    }

    /**
     * @return
     * @since 1.8.5
     */
    protected Bounds getFirstVisibleBoundsOnScreen() {
        for (int i = 0; i < super.getParagraphs().size(); i++) {
            Optional<Bounds> opBounds = super.getParagraphBoundsOnScreen(i);
            if (opBounds.isPresent()) {
                return opBounds.get();
            }
        }
        return null;
    }

    /**
     * @return
     * @since 1.8.5
     */
    protected Bounds getFirstVisibleBounds() {
        return super.screenToLocal(this.getFirstVisibleBoundsOnScreen());
    }

    public String getFileType() {
        return SupportFileTypes.TYPE_PLAIN_TEXT;
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

    public boolean isDisableRedo() {
        return disableRedo.get();
    }

    public BooleanProperty disableRedoProperty() {
        return disableRedo;
    }

    public void setDisableRedo(boolean disableRedo) {
        this.disableRedo.set(disableRedo);
    }

    public boolean isDisableUndo() {
        return disableUndo.get();
    }

    public BooleanProperty disableUndoProperty() {
        return disableUndo;
    }

    public void setDisableUndo(boolean disableUndo) {
        this.disableUndo.set(disableUndo);
    }

    public enum FEATURE {
        // input helper
        HELPER,
        // indent or un-indent by TAB key
        TAB_INDENT,
        // quote by '
        QUOTE,
        // quote by "
        DOUBLE_QUOTE,
        // quote by ```
        BACK_QUOTE,
        // auto indent when starts new line
        AUTO_INDENT,
        // delete current line
        LINE_DELETE,
        // move current line
        LINES_MOVE
    }

    public static class Replacement {
        // substitute to be added in the head.
        String substitute;
        // targets to be trimmed in the head.
        List<String> targets;
        // tail to be added to the end (always add if not exist, no trimming).
        String tail;

        public Replacement(String substitute) {
            this(substitute, null, null);
        }

        public Replacement(String substitute, String tail) {
            this(substitute, tail, null);
        }

        public Replacement(String substitute, List<String> targets) {
            this(substitute, null, targets);
        }

        public Replacement(String substitute, String tail, List<String> targets) {
            this.substitute = substitute;
            this.tail = tail;
            if (targets == null || targets.isEmpty()) {
                this.targets = Collections.singletonList(substitute);
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

        public List<String> getTargets() {
            return targets;
        }

        public void setTargets(List<String> targets) {
            this.targets = targets;
        }

        public String getTail() {
            return tail;
        }

        public void setTail(String tail) {
            this.tail = tail;
        }
    }
}
