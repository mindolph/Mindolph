package com.mindolph.base.control;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.base.util.EventUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mfx.util.TextUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.pref.PreferenceManager;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static com.mindolph.base.constant.ShortcutConstants.*;
import static javafx.scene.input.KeyCode.TAB;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author mindolph.com@gmail.com
 */
public class ExtCodeArea extends CodeArea {

    public static final int HISTORY_MERGE_DELAY_IN_MILLIS = 200;
    public static final int INPUT_HELP_DELAY_IN_MILLIS = 3000;

    private static final Logger log = LoggerFactory.getLogger(ExtCodeArea.class);

    private final ShortcutManager sm = ShortcutManager.getIns();

    // disable 'paste' shortcut in code area control, just a workaround for the shortcut conflict on macOS.
    private final BooleanProperty disablePaste = new SimpleBooleanProperty(false);
    private final BooleanProperty disableRedo = new SimpleBooleanProperty(false);
    private final BooleanProperty disableUndo = new SimpleBooleanProperty(false);

    // used to control the merging of editing history.
    private final EventSource<String> historySource = new EventSource<>();

    private final EventSource<String> inputHelpSource = new EventSource<>();

    private final InputHelperManager inputHelperManager;

    // Indicate that whether the input method is using, if true, the input helper is paused until it becomes false.
    private boolean isInputMethod = false;


    public ExtCodeArea() {
        this.inputHelperManager = new InputHelperManager(getFileType());
        // auto scroll when caret goes out of viewport.
        super.caretPositionProperty().addListener((observableValue, integer, t1) -> ExtCodeArea.super.requestFollowCaret());
        this.setOnContextMenuRequested(contextMenuEvent -> {
            ContextMenu codeContextMenu = createContextMenu();
            Node node = (Node) contextMenuEvent.getSource();
            codeContextMenu.show(node.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
        // the event should be emitted when text changed in editor
        historySource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(HISTORY_MERGE_DELAY_IN_MILLIS))
                .subscribe(s -> this.getUndoManager().preventMerge());

        this.bindCaretCoordinate();
        this.bindInputHelperContextMenu();
    }

    public void refresh() {
        // DO NOTHING
    }

    public void doHistory() {
        historySource.push(null);
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

    // @since 1.6
    private void bindCaretCoordinate() {
        this.caretBoundsProperty().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(bounds -> inputHelperManager.updateCaret(this, bounds.getMaxX(), bounds.getMaxY()));
        });
    }

    // @since 1.6
    private void bindInputHelperContextMenu() {
        // delay update context text to reduce redundant calculating.
        // TODO better way is to do updating when new word completed and any other actions that makes text change completed.
        inputHelpSource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(INPUT_HELP_DELAY_IN_MILLIS))
                .subscribe(s -> {
                    Collection<Plugin> plugins = PluginManager.getIns().findPlugin(getFileType());
                    for (Plugin plugin : plugins) {
                        plugin.getInputHelper().updateContextText(s);
                    }
                });

        // prepare the context words
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isInputHelperEnabled()) {
                return;
            }
            if (!StringUtils.equals(oldValue, newValue)) inputHelpSource.push(newValue);
        });

        // stop helping when paragraph is changed by like mouse click.
        this.currentParagraphProperty().addListener((observable, oldValue, newValue) -> {
            if (!isInputHelperEnabled()) {
                return;
            }
            inputHelperManager.consume(InputHelperManager.UNKNOWN_INPUT, null);
        });

        // Insert selected text from input helper.
        inputHelperManager.onSelected((selection) -> {
            if (StringUtils.startsWithIgnoreCase(selection.selected(), selection.input())) {
                int start = selection.input().length();
                this.insertText(this.getCaretPosition(), StringUtils.substring(selection.selected(), start));
            }
        });
    }

    @Override
    protected void handleInputMethodEvent(InputMethodEvent event) {
        super.handleInputMethodEvent(event);
        if (!isInputHelperEnabled()) {
            System.out.printf("'%s'%n", event.getCommitted());
            return;
        }
        if (StringUtils.isBlank(event.getCommitted())) {
            isInputMethod = true;
            log.debug("in input method");
            this.insertText(this.getCaretPosition(), event.getCommitted());
        }
        else {
            System.out.println(event.getCommitted());
            isInputMethod = false;
            log.debug("not in input method");
            inputHelperManager.consume(InputHelperManager.UNKNOWN_INPUT, extractLastWordFromCaret());
        }
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
                            addToSelectionHeadAndTail("```", true);
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
            // somehow on macOS, the shortcut event not only consumed by editor, but also consumed by application,
            // which causes the paste action be performed twice.
            // so disable PASTE shortcut on macOS to avoid conflict with global.
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

            inputMaps.add(InputMap.consume(EventPattern.keyReleased(), keyEvent -> {
                if (isInputHelperEnabled() && !isInputMethod) {
                    if (EventUtils.isEditableInput(keyEvent)) {
                        inputHelperManager.consume(keyEvent, extractLastWordFromCaret());
                    }
                }
            }));
            Nodes.addInputMap(this, InputMap.sequence(inputMaps.toArray(new InputMap[]{})));
        }
    }


    private boolean isInputHelperEnabled() {
        return PreferenceManager.getInstance().getPreference(PrefConstants.GENERAL_EDITOR_ENABLE_INPUT_HELPER, true);
    }

    private String extractLastWordFromCaret() {
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
        int curParIdx = this.getCurrentParagraph();
        int lineStart = this.getAbsolutePosition(curParIdx, 0);
        int lineEnd = this.getAbsolutePosition(curParIdx, this.getCurrentLineEndInParargraph() + 1); // +1 include the line break;
        log.trace("%d-%d".formatted(lineStart, lineEnd));
        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
        String lineText = paragraph.getText();
        this.deleteText(lineStart, lineEnd);
        return lineText;
    }

    public void insertText(String text){
        this.insertText(this.getCaretPosition(), text);
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
            if (params.getTargets() != null
                    && !StringUtils.startsWithAny(p.getText(), params.getTargets().toArray(new String[]{}))) {
                isAlreadyAdded = false;
            }
        }
        boolean needAddToHead = !isAlreadyAdded;
        this.addOrTrimHeadToParagraphs(params, needAddToHead);
    }

    public void addOrTrimHeadToParagraphs(Replacement params, boolean needAddToHead) {
        String tail = params.getTail() == null ? EMPTY : params.getTail();
        addOrTrimHeadToParagraphs(params, true, s -> {
            String tailOfLine = (s.endsWith(tail) ? EMPTY : tail);
            if (needAddToHead) {
                return params.getSubstitute() + s + tailOfLine;
            }
            else {
                for (String target : params.getTargets()) {
                    if (StringUtils.startsWith(s, target)) {
                        return StringUtils.replaceOnce(s, target, EMPTY) + tailOfLine;
                    }
                }
            }
            return s;
        });
    }

    /**
     * Add or trim {@code text} to or from head of selected paragraphs.
     *
     * @param params
     * @param skipEmptyLine true to skip the empty line when processing, but if there is only one line, ignore this.
     * @param callback      Convert the text line,
     */
    public void addOrTrimHeadToParagraphs(Replacement params, boolean skipEmptyLine, Function<String, String> callback) {
        CaretSelectionBind<Collection<String>, String, Collection<String>> caretSelectionBind = this.getCaretSelectionBind();
        int startPar = caretSelectionBind.getStartParagraphIndex();
        int endPar = caretSelectionBind.getEndParagraphIndex();
        int lineCount = endPar - startPar;
        boolean hasSelection = this.getSelection().getLength() != 0;
        if (hasSelection) log.debug("Selected from %s to %s".formatted(startPar, endPar));

        List<String> newLines = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>();
        for (int i = startPar; i < endPar + 1; i++) {
            String newLine;
            Paragraph<Collection<String>, String, Collection<String>> p = this.getParagraph(i);
            if (lineCount > 1 && skipEmptyLine && StringUtils.isBlank(p.getText())) {
                newLine = EMPTY;
            }
            else {
                newLine = callback.apply(p.getText());// params.getSubstitute() + p.getText();
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

    public void addToSelectionHeadAndTail(String text, boolean select) {
        IndexRange selection = this.getSelection();
        String selectedText = super.getSelectedText();
        super.replaceSelection(text + selectedText + text);
        if (select) {
            this.selectRange(selection.getStart(), selection.getEnd() + text.length() * 2);
        }
    }

    protected String getCurrentParagraphText() {
        int curParIdx = this.getCurrentParagraph();
        Paragraph<Collection<String>, String, Collection<String>> paragraph = this.getParagraph(curParIdx);
        return paragraph.getText();
    }

    // The file type is needed to
//    private String fileType;

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
