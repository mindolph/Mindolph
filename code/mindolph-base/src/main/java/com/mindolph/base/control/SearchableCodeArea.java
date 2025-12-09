package com.mindolph.base.control;

import com.mindolph.core.search.TextLocation;
import com.mindolph.core.search.TextNavigator;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.util.BoundsUtils;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.Selection;
import org.fxmisc.richtext.SelectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * Code area with text locating functionality.
 *
 * @author mindolph.com@gmail.com
 */
public class SearchableCodeArea extends SmartCodeArea {

    private static final Logger log = LoggerFactory.getLogger(SearchableCodeArea.class);

    /**
     * Used to show search highlighting.
     */
    private final TextNavigator textNavigator = new TextNavigator();

    private boolean searchAtEnd = false;
    private boolean searchAtBeginning = false;

    // TODO
    Selection<Collection<String>, String, Collection<String>> extraSelection = new SelectionImpl<>("main selection", this,
            path -> {
                // make rendered selection path look like a yellow highlighter
                path.setStrokeWidth(0);
                path.setFill(Color.YELLOW);
            }
    );

    public SearchableCodeArea() {
        super();
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            textNavigator.setText(newValue, false);
        });
    }

    public void setText(String text) {
        this.replaceText(text);
        this.displaceCaret(0);
        this.getUndoManager().forgetHistory();
        this.getUndoManager().mark();
    }

    /**
     * @param keyword
     * @param options
     */
    public void searchNext(String keyword, TextSearchOptions options) {
        boolean isFromBeginning = super.getCaretPosition() == 0; // to avoid infinite loop when nothing found in whole content.
        textNavigator.setText(this.getText(), !options.isForReplacement());
        int curRow = this.getCurrentParagraph();
        int curCol = this.getCaretColumn();
        textNavigator.moveCursor(curRow, curCol);
        log.debug("Search next from: %d %d".formatted(curRow, curCol));
        TextLocation tloc = textNavigator.locateNext(keyword, options.isCaseSensitive());
        if (tloc != null) {
            log.debug("found next at: %s,%s -> %s,%s".formatted(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol()));
            this.selectRange(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol() + 1);
            this.scrollXToPixel(0);
            this.centerTheCaretY();
        }
        else {
            if (!isFromBeginning && searchAtEnd) {
                this.moveTo(0); // move to the start of doc
                searchNext(keyword, options); // and restart searching.
                searchAtEnd = false;
            }
            else {
                searchAtEnd = true; // setup flag and skip one search step
            }
        }
    }

    public void searchPrev(String keyword, TextSearchOptions options) {
        boolean isFromEnd = super.getCaretPosition() == getText().length(); // to avoid infinite loop when nothing found in whole content.
        textNavigator.setText(this.getText(), !options.isForReplacement());
        IndexRange selection = this.getSelection();
        int row = this.getCurrentParagraph();
        int col = this.getCaretColumn() - selection.getLength() - 1; // -1 for the caret column is exclusive
        if (this.getParagraph(row).length() == 0) {
            row--; // last empty line is special
        }
        if (row < 0) {
            return; // do not search if no text content
        }
        textNavigator.moveCursor(row, col); // move to row and col to start this search.
        log.debug("Search previous from caret: %d %d".formatted(row, col));
        TextLocation tloc = textNavigator.locatePrev(keyword, options.isCaseSensitive());
        if (tloc != null) {
            log.debug("found previous at: %s,%s -> %s,%s".formatted(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol()));
            this.selectRange(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol() + 1);
            this.scrollXToPixel(0);
            this.centerTheCaretY();
        }
        else {
            if (!isFromEnd && searchAtBeginning) {
                this.moveTo(getText().length()); // move to the end of doc
                this.searchPrev(keyword, options);
                searchAtBeginning = false;
            }
            else {
                searchAtBeginning = true; // setup flag and skip one search step
            }
        }
    }

    /**
     * @since 1.8.5
     */
    public void centerTheCaretY() {
        // Try to center the Y of caret but doesn't work well since the getParagraphBoundsOnScreen()
        // does not always return the correct caret bounds(probably a bug). // TODO
        Platform.runLater(() -> {
            CaretNode caret = this.getCaretSelectionBind().getUnderlyingCaret();
            log.debug("caret index in paragraph %d".formatted(caret.getParagraphIndex()));
            Optional<Bounds> paragraphBoundsOnScreen = this.getParagraphBoundsOnScreen(caret.getParagraphIndex());
            if (paragraphBoundsOnScreen.isPresent()) {
                log.debug("caret bounds on screen: %s".formatted(BoundsUtils.boundsInString(paragraphBoundsOnScreen.get())));
                Bounds caretBounds = this.screenToLocal(paragraphBoundsOnScreen.get());
                log.debug("caret bounds in viewport: %s".formatted(BoundsUtils.boundsInString(caretBounds)));
                double viewportHeight = this.getViewportHeight();
                double scrollTo = caretBounds.getCenterY() - viewportHeight / 2;
                log.debug("caret Y: %s".formatted(caretBounds.getCenterY()));
                log.debug("scroll to: %s".formatted(scrollTo));
                this.scrollYBy(scrollTo);
            }
            else {
                log.debug("no bounds exists for caret %s".formatted(caret));
//                for (int i = 0; i < super.getParagraphs().size(); i++) {
//                    Optional<Bounds> opBounds = super.getParagraphBoundsOnScreen(i);
//                    if (opBounds.isPresent()) {
//                        log.debug("%d - %s".formatted(i, BoundsUtils.boundsInString(this.screenToLocal(opBounds.get()))));
//                    }
//                }
            }
        });
    }

    public void searchAndReplaceSelection(String keywords, TextSearchOptions searchOptions, String replacement) {
        if (StringUtils.isEmpty(keywords)) {
            return;
        }
        if (this.getSelection().getLength() == 0) {
            this.searchNext(keywords, searchOptions); // select first for replacement
        }
        if (!this.replaceSelection(keywords, searchOptions.isCaseSensitive(), replacement)) {
            log.debug("no text replaced");
        }
        this.searchNext(keywords, searchOptions);
    }

    private boolean replaceSelection(String keywords, boolean isCaseSensitivity, String replacement) {
        // blank string CAN be replaced.
        if (StringUtils.isEmpty(keywords)) {
            return false;
        }
        boolean match = isCaseSensitivity ? keywords.equals(getSelectedText()) : keywords.equalsIgnoreCase(getSelectedText());
        if (match) {
            if (StringUtils.isEmpty(replacement)) {
                super.deleteText(super.getSelection());
            }
            else {
                textNavigator.moveCursor(super.getSelection().getStart()); // make sure that the cursor is initialized if no searching was executed before replacing.
                int offset = replacement.length() - super.getSelection().getLength();
                super.replaceSelection(replacement);
                textNavigator.adjustCursor(offset);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void replaceAllMatch(String keywords, TextSearchOptions searchOptions, String replacement) {
        super.moveTo(0);// starts from head of content
        if (StringUtils.isBlank(keywords)) {
            return;
        }
        String replacedText;
        if (searchOptions.isCaseSensitive()) {
            replacedText = Strings.CS.replace(this.getText(), keywords, replacement == null ? "" : replacement);
        }
        else {
            replacedText = Strings.CI.replace(this.getText(), keywords, replacement == null ? "" : replacement);
        }
        super.replaceText(replacedText);
    }
}
