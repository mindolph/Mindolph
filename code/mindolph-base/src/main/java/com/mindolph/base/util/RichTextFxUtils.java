package com.mindolph.base.util;

import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * @author mindolph.com@gmail.com
 */
public class RichTextFxUtils {

    /**
     * Get selected text in a paragraph,
     *
     * @param textArea
     * @param paragraph
     * @return
     */
    public static String getSelectedParagraphText(StyleClassedTextArea textArea, int paragraph) {
        IndexRange ps = textArea.getParagraphSelection(paragraph);
        return textArea.getText(paragraph, ps.getStart(), paragraph, ps.getEnd());
    }
}
