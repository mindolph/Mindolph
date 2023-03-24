package com.mindolph.csv;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author mindolph.com@gmail.com
 */
public class CsvNavigator {

    private static final Logger log = LoggerFactory.getLogger(CsvNavigator.class);

    private final List<String> cells; // cells in total
    private final List<String> reversedCells;
    private final int rowSize;
    private final int total;
    private Integer cursor = 0;

    public CsvNavigator(List<String> cells, int rowSize) {
        this.cells = cells;
        this.rowSize = rowSize;
        this.total = cells.size();
        this.reversedCells = new LinkedList<>();
        CollectionUtils.addAll(reversedCells, cells);
        Collections.reverse(reversedCells); // reverse for search back
    }

    public void moveCursor(int pos) {
        this.cursor = pos;
    }

    public void moveCursor(int row, int col) {
        this.cursor = CellPos.getIndexOfAll(new CellPos(row, col), rowSize);
    }

    public void moveCursor(CellPos cellPos) {
        this.cursor = CellPos.getIndexOfAll(cellPos, rowSize);
    }

    public void moveCursorNext() {
        this.cursor += 1;
        this.cursor = (cursor > total - 1 ? 0 : cursor);
    }

    public void moveCursorPrev() {
        this.cursor -= 1;
        this.cursor = (cursor < 0 ? total - 1 : cursor);
    }

    public CellPos locateNext(String keyword, boolean caseSensitive) {
        int idx = this.locate(keyword, caseSensitive, false);
        if (idx < 0) {
            return null;
        }
        return CellPos.fromIndexOfAll(idx, rowSize);
    }

    public CellPos locatePrev(String keyword, boolean caseSensitive) {
        int idx = this.locate(keyword, caseSensitive, true);
        if (idx < 0) {
            return null;
        }
        return CellPos.fromIndexOfAll((total - idx - 1), rowSize);
    }

    private int locate(String keyword, boolean caseSensitive, boolean reverse) {
        log.debug("locate from %d %s".formatted(cursor, (reverse ? "backward" : "forward")));
        BiFunction<String, String, Boolean> contains = caseSensitive ? StringUtils::contains : StringUtils::containsIgnoreCase;
        for (int i = (reverse ? (total - cursor - 1) : cursor);
             i < total;
             i++) {
            String s = (reverse ? reversedCells : cells).get(i);
            if (contains.apply(s, keyword)) {
                return i;
            }
        }
        return -1;
    }

    public int getTotal() {
        return total;
    }
}
