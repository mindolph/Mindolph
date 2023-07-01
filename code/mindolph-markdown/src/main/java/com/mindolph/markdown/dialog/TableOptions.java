package com.mindolph.markdown.dialog;


/**
 *
 *
 */
public class TableOptions {

    private int rows = 1;
    private int cols = 1;

    public TableOptions() {
    }

    public TableOptions(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public TableOptions(TableOptions that) {
        this.rows = that.rows;
        this.cols = that.cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
}
