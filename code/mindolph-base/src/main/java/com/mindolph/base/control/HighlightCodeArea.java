package com.mindolph.base.control;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class HighlightCodeArea extends SearchableCodeArea {

    /**
     * Pattern for highlighting.
     */
    protected Pattern pattern;

    protected abstract StyleSpans<Collection<String>> computeHighlighting(String text);
}
