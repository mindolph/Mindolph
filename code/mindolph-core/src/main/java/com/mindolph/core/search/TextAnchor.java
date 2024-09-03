package com.mindolph.core.search;

/**
 *
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class TextAnchor implements Anchor {

    private TextLocation textLocation;

    public TextAnchor(TextLocation textLocation) {
        this.textLocation = textLocation;
    }

    public TextLocation getTextLocation() {
        return textLocation;
    }

    public void setTextLocation(TextLocation textLocation) {
        this.textLocation = textLocation;
    }

    @Override
    public String toString() {
        return "TextAnchor{" +
                "textLocation=" + textLocation +
                '}';
    }
}
