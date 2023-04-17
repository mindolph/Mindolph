package com.mindolph.base.control;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * With default index column.
 * Row operations.
 * Column operations.
 * Selected cells operations.
 * Streaming.
 *
 * @author mindolph.com@gmail.com
 * @see Row
 * @see SimpleTextCell
 */
public class ExtTableView extends TableView<Row> {
    private static final Logger log = LoggerFactory.getLogger(ExtTableView.class);
    protected TableColumn<Row, String> indexCol;
    private int stubColIdx;
    private int stubRowIdx;

    public ExtTableView() {
        super.setEditable(true);
        super.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        super.getSelectionModel().setCellSelectionEnabled(true);
        this.createIndexColumn();
        this.disableIndexColumnKeyEvents();
    }


    private void createIndexColumn() {
        log.debug("Create index column");
        indexCol = new TableColumn<>(EMPTY);
        indexCol.setSortable(false);
        indexCol.setEditable(false);
        indexCol.setResizable(false);
        indexCol.setReorderable(false);
        indexCol.setCellFactory(this.createIndexCellFactory());
        indexCol.setCellValueFactory(cellDataFeatures -> {
            int idxRow = cellDataFeatures.getValue().getIndex();
            log.trace("row idx: %d and stub row id: %d".formatted(idxRow, stubRowIdx));
            return idxRow != stubRowIdx ? new SimpleStringProperty(String.valueOf(idxRow + 1)) : null;
        });
        indexCol.getStyleClass().add("index-column");
        super.getColumns().add(indexCol);
    }

    private void disableIndexColumnKeyEvents() {
        this.getFocusModel().focusedCellProperty().addListener((observableValue, number, focused) -> {
            log.trace("Focused changed %d-%d".formatted(focused.getRow(), focused.getColumn()));
            if (focused.getColumn() == 0) {
                log.debug("Unable to select cells of index column");
                getSelectionModel().clearSelection(focused.getRow(), indexCol);
                getSelectionModel().selectRightCell();
                refresh();
            }
        });
    }

    protected Callback<TableColumn<Row, String>, TableCell<Row, String>> createIndexCellFactory() {
        throw new NotImplementedException("Implement me");
    }

    public void appendRows(List<Row> rows) {
        super.getItems().addAll(rows);
        this.afterRowAdded();
    }

    private void afterRowAdded() {
        stubRowIdx = super.getItems().size() - 1;
        Platform.runLater(() -> {
            // set index column width after data loaded.
            int width = String.valueOf(super.getItems().size()).length() * 20;
            indexCol.setPrefWidth(width);
        });
    }

    /**
     * Create new row without index, null value for elements.
     *
     * @param size
     * @return
     */
    public static Row createRow(int size) {
        log.debug("Create a new row");
        String[] stubStrs = new String[size]; // the stub cols index equals columns size.
        Arrays.fill(stubStrs, null); // set default to null because it will be used to indicate the empty line.
        Row stubRow = new Row();
        Collections.addAll(stubRow.getData(), stubStrs);
        return stubRow;
    }

    public Row insertNewRow(int index) {
        Row newRow = createRow(super.getColumns().size() - 1); // excludes index column
        newRow.setIndex(index);
        stubRowIdx++; //must increase stub row index before insert new row, otherwise the last row's moving will trigger redundant new stub row created.
        super.getSelectionModel().clearSelection();
        super.getItems().add(index, newRow);
        this.afterRowAdded();
        this.reOrder();
        super.refresh();
        return newRow;
    }

    /**
     * All columns must be defined before calling this method.
     *
     * @return
     */
    public Row appendStubRow() {
        Row newRow = createRow(super.getColumns().size() - 1); // excludes index column
        newRow.setIndex(++stubRowIdx);
        super.getSelectionModel().clearSelection();
        super.getItems().add(newRow);
//        super.refresh();
        this.afterRowAdded();
        return newRow;
    }

    /**
     * Append a new column to the end of table columns.
     *
     * @param header
     * @return
     */
    public TableColumn<Row, String> appendColumn(String header) {
        log.debug("Append new column '%s'".formatted(header));
        TableColumn<Row, String> column = new TableColumn<>(header);
        column.setMinWidth(80);
        column.setEditable(true);
        column.setSortable(false);
        column.setReorderable(false);
        column.setCellValueFactory(cellData -> {
            Row row = cellData.getValue();
            int colIdx = cellData.getTableView().getColumns().indexOf(cellData.getTableColumn());
            int dataIdx = colIdx - 1;// -1 because of the index column.
            if (dataIdx >= 0 && dataIdx < row.size()) {
                return new SimpleStringProperty(row.getData().get(dataIdx));
            }
            return null;
        });
        super.getColumns().add(column);
        stubColIdx = super.getColumns().size() - 1;
        return column;
    }

    public boolean isStubColumn(TableColumn<Row, String> column) {
        return super.getColumns().indexOf(column) == stubColIdx;
    }

    public boolean isStubColumn(int colIdx) {
        return stubColIdx == colIdx;
    }

    public boolean deleteSelectedRows() {
        ObservableList<Row> selectedRows = getSelectedRows();
        log.trace("stubRowIdx=%d".formatted(stubRowIdx));
        List<Row> rowsWithoutStub = selectedRows.stream().filter(r -> r.getIndex() != stubRowIdx).toList();
        log.debug("Delete %d lines".formatted(rowsWithoutStub.size()));
        super.getItems().removeAll(rowsWithoutStub);
        this.reOrder();
        stubRowIdx = super.getItems().size() - 1;
        super.getSelectionModel().clearSelection();
        return true;
    }

    public void reOrder() {
        ObservableList<Row> items = super.getItems();
        log.debug("%d rows left.".formatted(items.size()));
        for (int i = 0; i < items.size(); i++) {
            Row item = items.get(i);
            item.setIndex(i);
        }
    }

    public boolean isFirstColumn(TableColumn<Row, String> column) {
        return super.getColumns().indexOf(column) == 1;
    }

    public void selectRow(int rowIdx) {
        this.selectRows(rowIdx, rowIdx);
    }

    /**
     * @param rowIdxFrom
     * @param rowIdxTo   inclusive
     */
    public void selectRows(int rowIdxFrom, int rowIdxTo) {
        ObservableList<TableColumn<Row, ?>> columns = super.getColumns();
        if (columns.size() <= 1) {
            return;
        }
        TableViewSelectionModel<Row> selectionModel = super.getSelectionModel();
        if (rowIdxFrom < 0 || rowIdxTo >= super.getItems().size()) {
            return;
        }

        log.debug("Select from %d,%d to %d,%d".formatted(rowIdxFrom, columns.indexOf(columns.get(1)), rowIdxTo, columns.indexOf(columns.get(columns.size() - 1))));
        selectionModel.selectRange(rowIdxFrom, columns.get(1), rowIdxTo, columns.get(columns.size() - 1));
    }

    public ObservableList<Row> getSelectedRows() {
        return super.getSelectionModel().getSelectedItems();
    }

    public ObservableList<TablePosition> getSelectedCells() {
        return super.getSelectionModel().getSelectedCells();
    }

    public Map<Integer, List<TablePosition>> getSelectedCellsByRow() {
        ObservableList<TablePosition> selectedCells = this.getSelectedCells();
        log.debug(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        LinkedHashMap<Integer, List<TablePosition>> map =
                selectedCells.stream().collect(Collectors.groupingBy(TablePositionBase::getRow, LinkedHashMap::new, Collectors.toList()));
        return map;
    }

    public List<String> getSelectedCellsData() {
        ObservableList<TablePosition> selectedCells = getSelectedCells();
        log.debug(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        return selectedCells.stream().map(pos -> {
            List<String> data = super.getItems().get(pos.getRow()).getData();
            if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                if (pos.getColumn() < 0) return null;
                return data.get(pos.getColumn());
            }
            return null;
        }).toList();
    }

    public String getFirstSelectedText() {
        Optional<TablePosition> first = super.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            TablePosition pos = first.get();
            Row row = super.getItems().get(pos.getRow());
            return row.getData().get(pos.getColumn() - 1);
        }
        return EMPTY;
    }

    public TablePosition<Row, String> setFirstSelectedCell(String text) {
        Optional<TablePosition> first = super.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            TablePosition<Row, String> pos = first.get();
            List<String> data = super.getItems().get(pos.getRow()).getData();
            int dataPos = pos.getColumn() - 1;
            if (CollectionUtils.isNotEmpty(data) && dataPos < data.size()) {
                data.set(Math.max(0, dataPos), text);
            }
            super.refresh();
            return pos;
        }
        return null;
    }

    public List<TablePosition> setAllSelectedCells(String text) {
        List<TablePosition> selected = super.getSelectionModel().getSelectedCells().stream().toList();
        for (TablePosition pos : selected) {
            List<String> data = super.getItems().get(pos.getRow()).getData();
            int dataPos = pos.getColumn() - 1;
            if (CollectionUtils.isNotEmpty(data) && dataPos < data.size()) {
                data.set(Math.max(0, dataPos), text);
            }
        }
        super.refresh();
        return selected;
    }

    /**
     * Stream all data
     *
     * @return
     */
    public Stream<String> stream() {
        return super.getItems().stream().flatMap(row -> row.getData().stream());
    }

    public int getColumnSize() {
        return this.getColumns().size() - 1;
    }

    /**
     * Return the index of data columns (excludes the index column)
     *
     * @return
     */
    public int getStubColIdx() {
        return stubColIdx - 1;
    }

    public int getStubRowIdx() {
        return stubRowIdx;
    }
}
