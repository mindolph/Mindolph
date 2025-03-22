package com.mindolph.core.search;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class TextLocator {
    private static final Logger log = LoggerFactory.getLogger(TextLocator.class);

    private String text;
    private List<String> lines; // text as lines.
    private String keyword = StringUtils.EMPTY; // preserve keyword to reset locating state if it was changed.

    private int startRow = 0;
    private int startCol = 0;
    // end position of last locating.
    private int endRow = 0;
    private int endCol = 0;

    /**
     * Set the text for searching.
     *
     * @param text
     * @param needReset false if in the case that replacement in text happened.
     */
    public void setText(String text, boolean needReset) {
        if (!StringUtils.equals(this.text, text)) {
            this.text = text;
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))){
                lines = IOUtils.readLines(byteArrayInputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (needReset) this.reset(0, 0);
            // log.debug("loaded %d lines.".formatted(lines.size()));
        }
    }

    /**
     * Move end column index after replacement.
     *
     * @param count
     */
    public void moveEndCol(int count) {
        endCol += count;
    }

    public void moveStartCol(int count) {
        int offset = startCol + count;
        if ((offset) <= 0) {
            startRow--;
            if (startRow >= 0) {
                String l = lines.get(startRow);
                startCol = (l.length() - 1) + offset;
            }
        }
        startCol += count;
    }

    /**
     * @param curRow last line break will not be recognized as a line, but the CodeArea's caret will give a line position.
     * @param curCol
     */
    private void reset(int curRow, int curCol) {
        int lastRow = Math.min(curRow, lines.size() - 1); // here is for CodeArea.
        this.startRow = curRow == -1 ? lines.size() - 1 : lastRow; // -1 to set start/end row to last line, or given row.
        this.startCol = curCol == -1 ? lines.get(lines.size() - 1).length() - 1 : curCol; // -1 to set start/end column to last of last line, or given column
        this.endRow = this.startRow;
        this.endCol = this.startCol;
    }

    private boolean isValid() {
        log.debug("check the search result: (%d, %d) -> (%d, %d)".formatted(startRow, startCol, endRow, endCol));
        return startRow >= 0 && startCol >= 0 && endRow >= 0 && endCol >= 0;
    }

    /**
     * @param keyword
     * @param caseSensitive
     * @param curRow
     * @param curCol
     * @return
     */
    public TextLocation locateNext(String keyword, boolean caseSensitive, int curRow, int curCol) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        // the first time for a keyword
        if (!this.keyword.equals(keyword)) {
            this.keyword = keyword;
            this.reset(curRow, curCol);
        }
        log.debug("locating next from: (%d, %d)".formatted(endRow, endCol));
        for (int i = endRow; i < lines.size(); i++) { // keep locating from last end row
            String line = lines.get(i);
            // keep locating from last end col.
            int lastEndCol = i == endRow ? endCol : 0;
            if (caseSensitive) {
                startCol = line.indexOf(keyword, lastEndCol);
            }
            else {
                startCol = StringUtils.indexOfIgnoreCase(line, keyword, lastEndCol);
            }
            if (startCol >= 0) {
                startRow = i;
                endRow = startRow;
                endCol = startCol + keyword.length();
                break;
            }
        }
        if (!isValid()) {
            this.reset(0, 0); // reset to start over.
            return null;
        }
        return new TextLocation(startRow, startCol, endRow, endCol);
    }

    /**
     * @param keyword
     * @param caseSensitive
     * @param curRow        -1 means from last row
     * @param curCol        -1 means from last column
     * @return
     */
    public TextLocation locatePrev(String keyword, boolean caseSensitive, int curRow, int curCol) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        // the first time for a keyword
        if (!this.keyword.equals(keyword)) {
            this.keyword = keyword;
            this.reset(curRow, curCol);
        }
        log.debug("locating previous from: (%d, %d)".formatted(startRow, startCol));
        for (int i = startRow; i >= 0; i--) { // reverse
            String line = lines.get(i);
            if (startCol == -1) {
                // different from method locateNext, the startCol will be -1 if no matching,
                // which make locating unable to proceed, so let it points to the end of whole text.
                startCol = line.length();
            }
            if (caseSensitive) {
//                if (startCol < keyword.length()) {
//                    startCol = -1;
//                }
//                else {
                startCol = TextUtils.lastIndexOf(line, keyword, startCol);
//                }
            }
            else {
                startCol = StringUtils.lastIndexOfIgnoreCase(line, keyword, startCol - 1);
            }
            // found?
            if (startCol >= 0) {
                startRow = i;
                endRow = startRow;
                endCol = startCol + keyword.length();
                break;
            }
            else {
//                startRow--;
//                if (startRow >= 0)
//                    startCol = lines.get(startRow).length() - 1;
            }
        }
        if (!isValid()) {
            this.reset(-1, -1);
            return null;
        }
        return new TextLocation(startRow, startCol, endRow, endCol);
    }


    public static void main(String[] args) {
        System.out.println(StringUtils.lastIndexOf("###", "##", 0));
    }
}
