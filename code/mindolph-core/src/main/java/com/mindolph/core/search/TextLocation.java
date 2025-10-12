package com.mindolph.core.search;

import java.util.Objects;

/**
 * @author mindolph.com@gmail.com
 */
public class TextLocation {

    public static TextLocation DEFAULT = new TextLocation(0, 0, 0, 0);

    private int startRow = 0;
    private int startCol = 0;
    private int endRow = 0; // inclusive
    private int endCol = 0; // inclusive

    public TextLocation(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }

    @Override
    public String toString() {
        return "%d,%d %d,%d".formatted(startRow, startCol, endRow, endCol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextLocation that = (TextLocation) o;
        return startRow == that.startRow && startCol == that.startCol && endRow == that.endRow && endCol == that.endCol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startRow, startCol, endRow, endCol);
    }
}
