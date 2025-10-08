package com.mindolph.base.control;

import com.mindolph.core.util.DebugUtils;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author mindolph.com@gmail.com
 */
public abstract class HighlightCodeArea extends SearchableCodeArea {

    private static final Logger log = LoggerFactory.getLogger(HighlightCodeArea.class);

    protected LinkedList<StyleRange> styleRanges = new LinkedList<>();

    protected abstract StyleSpans<Collection<String>> computeHighlighting(String text);


    /**
     * Append style class to the list.
     *
     * @param styleClass
     * @param start
     * @param end        exclusive
     *
     */
    protected void append(String styleClass, int start, int end) {
        styleRanges.add(new StyleRange(IntegerRange.of(start, end - 1), styleClass));
    }

    /**
     * Cut in new style class to the styles from the major processing.
     *
     * @param newStyleClass
     * @param start
     * @param end           exclusive
     */
    protected void cutInNewStyle(String newStyleClass, int start, int end) {
        int inclusiveEnd = end - 1;
        if (styleRanges.isEmpty()) {
            styleRanges.add(StyleRange.of(start, inclusiveEnd, newStyleClass));
            return;
        }
        for (StyleRange styleRange : styleRanges) {
            int idx = styleRanges.indexOf(styleRange);
            String oldStyleClass = styleRange.styleClass();
            IntegerRange oldRange = styleRange.range();

            if (oldRange.isBefore(start)) {
                continue; // try next until find conflict.
            }
            if (oldRange.contains(start) && oldRange.contains(inclusiveEnd)) {
                // if the new style is within the old range, like inline code in the quote block.
                styleRanges.remove(styleRange);
                if (start > oldRange.getMinimum()) {
                    StyleRange sr0 = StyleRange.of(oldRange.getMinimum(), start - 1, oldStyleClass);
                    styleRanges.add(idx++, sr0);
                }
                StyleRange sr1 = StyleRange.of(start, inclusiveEnd, newStyleClass);
                styleRanges.add(idx++, sr1);
                if ((inclusiveEnd) < oldRange.getMaximum()) {
                    StyleRange sr2 = StyleRange.of(inclusiveEnd + 1, oldRange.getMaximum(), oldStyleClass);
                    styleRanges.add(idx, sr2);
                }
            }
            else if (IntegerRange.of(start, inclusiveEnd).containsRange(oldRange)) {
                // if the new style is over the old range, like inline code contains bold/italic chars.
                // just overwrite the old one.
                styleRanges.remove(styleRange);
                styleRanges.add(idx, StyleRange.of(start, inclusiveEnd, newStyleClass));
            }
            else {
                // no conflict, just add as new style range at the index.
                styleRanges.add(idx, StyleRange.of(start, inclusiveEnd, newStyleClass));
            }
            return;
        }
        // add new style if no conflict.
        styleRanges.add(StyleRange.of(start, inclusiveEnd, newStyleClass));
    }

    /**
     * Build the {@link StyleSpans} for code are syntax highlighting.
     *
     * @param text
     * @return
     */
    protected StyleSpans<Collection<String>> buildStyleSpans(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastKwEnd = 0;

        for (StyleRange styleRange : styleRanges) {
            String styleClass = styleRange.styleClass();
            IntegerRange range = styleRange.range();
            int start = range.getMinimum();
            int end = range.getMaximum() + 1;

            if (start > lastKwEnd) {
                if (log.isTraceEnabled())
                    log.trace("default: (%d-%d) - '%s'".formatted(lastKwEnd, start, DebugUtils.visible(StringUtils.substring(text, lastKwEnd, start))));
                spansBuilder.add(Collections.emptyList(), start - lastKwEnd);
            }
            if (log.isTraceEnabled())
                log.trace("%s: (%d-%d) - '%s'%n".formatted(styleClass, start, end, DebugUtils.visible(StringUtils.substring(text, start, end))));
            spansBuilder.add(Collections.singleton(styleClass), end - start);
            lastKwEnd = end;
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     *
     * @param range
     * @param styleClass
     */
    public record StyleRange(IntegerRange range, String styleClass) {

        public static StyleRange of(int start, int end, String styleClass) {
            return new StyleRange(IntegerRange.of(start, end), styleClass);
        }
    }
}
