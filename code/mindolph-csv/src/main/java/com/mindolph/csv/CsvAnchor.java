package com.mindolph.csv;

import com.mindolph.core.search.Anchor;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class CsvAnchor implements Anchor {

    private int row;
    private int col;

    public CsvAnchor(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public CsvAnchor setRow(int row) {
        this.row = row;
        return this;
    }

    public int getCol() {
        return col;
    }

    public CsvAnchor setCol(int col) {
        this.col = col;
        return this;
    }
}
