package com.mindolph.core.search;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.swiftboot.util.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Navigation in text forward and backward.
 *
 * @author mindolph.com@gmail.com
 */
public class TextNavigator {

    private String text;
    private List<String> lines;
    private final List<Range<Integer>> ranges = new ArrayList<>();
    private Integer cursor;

    /**
     * Set text for navigating, if totally new text, set resetCursor to be true
     *
     * @param text
     * @param resetCursor false if not reset cursor for some cases like text replacement.
     */
    public void setText(String text, boolean resetCursor) {
        if (!StringUtils.equals(this.text, text)) {
            this.text = text;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes());
            try {
                lines = IOUtils.readLines(byteArrayInputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ranges.clear();
            int pos = 0;
            for (String line : lines) {
                ranges.add(Range.between(pos, pos + line.length()));
                pos += line.length() + 1; //to next line
            }
        }
        if (resetCursor) cursor = null; // reset the cursor
    }

    /**
     * move cursor forward (offset > 0) or backward (offset < 0).
     *
     * @param offset
     */
    public void adjustCursor(int offset) {
        cursor += offset;
    }

    /**
     * move cursor to a new position.
     *
     * @param pos
     */
    public void moveCursor(int pos) {
        cursor = pos;
    }

    /**
     * move cursor to a new position by row and column indices.
     *
     * @param row
     * @param col
     */
    public void moveCursor(int row, int col) {
        // the requested row and col must be in the range, otherwise exception
        if (row < ranges.size()) {
            Range<Integer> range = ranges.get(row);
            if (col < range.getMaximum()) {
                cursor = range.getMinimum() + col;
            }
        }
    }

    /**
     * locate next matching keyword in text from current position.
     *
     * @param keyword
     * @param caseSensitive
     * @return
     */
    public TextLocation locateNext(String keyword, boolean caseSensitive) {
        if (cursor == null) cursor = 0;
        int start = this.locate(keyword, caseSensitive ? StringUtils::indexOf : StringUtils::indexOfIgnoreCase);
        int end = start + keyword.length() - 1;
        cursor = end + 1;
        return this.convert(start, end);
    }

    /**
     * locate previous matching keyword in text from current position.
     *
     * @param keyword
     * @param caseSensitive
     * @return
     */
    public TextLocation locatePrev(String keyword, boolean caseSensitive) {
        if (cursor == null) cursor = text.length() - 1;
        int start = locate(keyword, caseSensitive ? TextUtils::lastIndexOf : TextUtils::lastIndexOfIgnoreCase);
        int end = start + keyword.length() - 1;
        cursor = start - 1;
        return this.convert(start, end);
    }

    /**
     * @param keyword
     * @param indexer the function to find the index of keyword.
     * @return
     */
    private int locate(String keyword, TriFunction<String, String, Integer, Integer> indexer) {
        return indexer.apply(text, keyword, cursor);
    }

    /**
     * Convert the start and end position to TextLocation.
     *
     * @param start start pos in text
     * @param end   end pos in text.
     * @return
     */
    private TextLocation convert(int start, int end) {
        Pair<Integer, Integer> pairStart = this.convert(start);
        Pair<Integer, Integer> pairEnd = this.convert(end);
        if (pairStart == null || pairEnd == null) {
            return null;
        }
        return new TextLocation(pairStart.getLeft(), pairStart.getRight(), pairEnd.getLeft(), pairEnd.getRight());
    }

    /**
     * Convert position in text to row-column pair for text lines.
     *
     * @param position
     * @return
     */
    private Pair<Integer, Integer> convert(int position) {
        for (Range<Integer> range : ranges) {
            if (range.contains(position)) {
                return Pair.of(ranges.indexOf(range), position - range.getMinimum());
            }
        }
        return null;
    }


}
