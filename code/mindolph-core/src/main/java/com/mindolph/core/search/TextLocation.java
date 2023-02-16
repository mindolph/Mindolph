package com.mindolph.core.search;

/**
 * @author mindolph.com@gmail.com
 */
public class TextLocation {
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
        return "%d %d %d %d".formatted(startRow, startCol, endRow, endCol);
    }
}
