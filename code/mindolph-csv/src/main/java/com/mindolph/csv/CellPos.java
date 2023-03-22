package com.mindolph.csv;

import javafx.scene.control.TablePosition;

import java.util.Objects;

/**
 * Position of editable table cell(excludes index column and any other)
 *
 * @author mindolph.com@gmail.com
 */
public class CellPos {
    private int rowIdx;
    private int colIdx;

    public static CellPos zero(){
        return new CellPos(0,0);
    }

    public static CellPos fromTablePosition(TablePosition<?, ?> tablePosition) {
        return new CellPos(tablePosition.getRow(), tablePosition.getColumn());
    }

    public static int getIndexOfAll(CellPos cellPos, int columns) {
        return cellPos.getRowIdx() * columns + cellPos.getColIdx();
    }

    public static CellPos fromIndexOfAll(int idx, int columns) {
        return new CellPos(idx / columns, idx % columns);
    }


    public CellPos(int rowIdx, int colIdx) {
        this.rowIdx = rowIdx;
        this.colIdx = colIdx;
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public CellPos setRowIdx(int rowIdx) {
        this.rowIdx = rowIdx;
        return this;
    }

    public int getColIdx() {
        return colIdx;
    }

    public CellPos setColIdx(int colIdx) {
        this.colIdx = colIdx;
        return this;
    }

    @Override
    public String toString() {
        return "[%d,%d]".formatted(rowIdx, colIdx);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellPos cellPos = (CellPos) o;
        return rowIdx == cellPos.rowIdx && colIdx == cellPos.colIdx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowIdx, colIdx);
    }
}
