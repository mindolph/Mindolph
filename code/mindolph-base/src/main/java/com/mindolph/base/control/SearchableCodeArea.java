package com.mindolph.base.control;

import com.mindolph.core.search.TextLocation;
import com.mindolph.core.search.TextNavigator;
import com.mindolph.core.search.TextSearchOptions;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.Selection;
import org.fxmisc.richtext.SelectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Code area with text locating functionality.
 *
 * @author mindolph.com@gmail.com
 */
public class SearchableCodeArea extends ExtCodeArea {

    private static final Logger log = LoggerFactory.getLogger(SearchableCodeArea.class);

    /**
     * Used to show search highlighting.
     */
    private final TextNavigator textNavigator = new TextNavigator();

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
            this.requestFollowCaret();
        }
        else {
            this.moveTo(0); // move to the start of doc
        }
    }

    public void searchPrev(String keyword, TextSearchOptions options) {
        textNavigator.setText(this.getText(), !options.isForReplacement());
        IndexRange selection = this.getSelection();
        int row = this.getCurrentParagraph();
        int col = this.getCaretColumn() - selection.getLength() - 1; // -1 for the caret column is exclusive
        if (this.getParagraph(row).length() == 0) {
            row--; // last empty line is special
        }
        textNavigator.moveCursor(row, col); // move to row and col to start this search.
        log.debug("Search previous from caret: %d %d".formatted(row, col));
        TextLocation tloc = textNavigator.locatePrev(keyword, options.isCaseSensitive());
        if (tloc != null) {
            log.debug("found previous at: %s,%s -> %s,%s".formatted(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol()));
            this.selectRange(tloc.getStartRow(), tloc.getStartCol(), tloc.getEndRow(), tloc.getEndCol() + 1);
            this.scrollXToPixel(0);
            this.requestFollowCaret();
        }
        else {
            this.moveTo(getText().length()); // move to the end of doc
        }
    }

    public boolean replaceSelection(String keywords, boolean isCaseSensitivity, String replacement) {
        if (StringUtils.isBlank(keywords)) {
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
            replacedText = StringUtils.replace(this.getText(), keywords, replacement == null ? "" : replacement);
        }
        else {
            replacedText = StringUtils.replaceIgnoreCase(this.getText(), keywords, replacement == null ? "" : replacement);
        }
        super.replaceText(replacedText);
    }
}
