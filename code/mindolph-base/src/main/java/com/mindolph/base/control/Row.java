package com.mindolph.base.control;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @see ExtTableView
 */
public class Row {
    private int index;
    private List<String> data = new ArrayList<>();

    public int size() {
        return data.size();
    }

    /**
     *
     * @param colIdx column index from 0 in data
     * @param value
     */
    public void updateValue(int colIdx, String value) {
        if (colIdx >= 0 && colIdx < data.size()) {
            data.remove(colIdx);
            data.add(colIdx, value);
        }
        else {
            int padding = colIdx - data.size();
            for (int i = 0; i < padding; i++) {
                data.add("");
            }
            data.add(value);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = new ArrayList<>(data);
    }
}
