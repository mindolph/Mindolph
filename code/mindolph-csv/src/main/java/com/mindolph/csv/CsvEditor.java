package com.mindolph.csv;

import com.mindolph.base.EditorContext;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.util.ClipBoardUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author mindolph.com@gmail.com
 */
public class CsvEditor extends BaseEditor implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CsvEditor.class);
    @FXML
    private TableView<Row> tableView;
    private ContextMenu contextMenu;

    private final CSVFormat csvFormat;

    private String text; // cache
    private int stubColIdx;
    private int stubRowIdx;
    private int clickedRowIdx = -1;

    private Callback<TableColumn<Row, String>, TableCell<Row, String>> cellFactory;

    public CsvEditor(EditorContext editorContext) {
        super("/editor/csv_editor.fxml", editorContext);
        super.fileType = SupportFileTypes.TYPE_CSV;

        csvFormat = CSVFormat.DEFAULT.builder().build();

        this.reload();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Initialize");
        tableView.setPlaceholder(new Label("No content of this CSV file"));
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
    }

    @Override
    public void loadFile(Runnable afterLoading) throws IOException {
        FileReader fileReader = new FileReader(editorContext.getFileData().getFile(), StandardCharsets.UTF_8);
        CSVParser parsed = csvFormat.parse(fileReader);
        List<CSVRecord> records = parsed.getRecords();
        Platform.runLater(() -> {
            // == init headers ==
            // init index column
            createIndexColumn();
            // init data column
            if (!records.isEmpty()) {
                CSVRecord headers = records.get(0);
                for (String header : headers) {
                    appendColumn(header);
                }
            }
            // init stub column
            appendColumn("");

            // init data content
            List<Row> rows = new LinkedList<>();
            for (CSVRecord record : records) {
                log.debug(StringUtils.join(record, ", "));
                Row row = new Row();
                row.setIndex(records.indexOf(record));
                CollectionUtils.addAll(row.getData(), record);
                rows.add(row);
            }
            // stub row
            stubRowIdx = records.size();
            rows.add(createStubRow());
            tableView.getItems().addAll(rows);

            cellFactory = param -> {
                EditCell<Row, String> textCell = EditCell.createStringEditCell();
                textCell.setMinHeight(32);
                textCell.setEditable(true);
                textCell.textProperty().addListener((observable, oldValue, newValue) -> {
                    log.debug("%s - Text changed from '%s' to '%s'".formatted(textCell.getIndex(), oldValue, newValue));
                    if (oldValue != null && !StringUtils.equals(oldValue, newValue) && StringUtils.isNotBlank(newValue)) {
                        int colIdx = tableView.getColumns().indexOf(textCell.getTableColumn());
                        int dataIdx = colIdx - 1;// -1 because of the index column.
                        int rowIdx = textCell.getIndex();
                        Row row = tableView.getItems().get(rowIdx);
                        row.updateValue(dataIdx, newValue);
                        Platform.runLater(() -> {
                            if (rowIdx == 0) {
                                textCell.getTableColumn().setText(newValue); // for all columns
                            }
                            if (rowIdx == stubRowIdx) {
                                stubRowIdx++;
                                Row stubRow = createStubRow();// last row editing activates new row.
                                tableView.getItems().add(stubRow);
                            }
                            if (colIdx == stubColIdx - 1) {
                                appendColumn(EMPTY).setCellFactory(cellFactory); // append a stub column
                            }
                            log.debug("Text of cell changed");
                            fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                        });
                    }
                });
                textCell.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    // log.debug("Selection changed for %s, %d".formatted(textCell.getTableColumn().getText(), textCell.getIndex()));
                    EventBus.getIns().notifyMenuStateChange(EventBus.MenuTag.COPY, !textCell.getTableView().getSelectionModel().isEmpty());
                });
                return textCell;
            };
            // NOTE: call setCellFactory here otherwise the textProperty change event emitting messily.
            Platform.runLater(() -> {
                        ObservableList<TableColumn<Row, ?>> columns = tableView.getColumns();
                        for (int i = 1; i < columns.size(); i++) {
                            TableColumn<Row, String> column = (TableColumn<Row, String>) columns.get(i);
                            column.setCellFactory(cellFactory);
                        }
                    }
            );

            log.debug("%d columns and %d row initialized".formatted(tableView.getColumns().size() - 1, tableView.getItems().size()));

            this.saveToCache();

            afterLoading.run();
            this.editorReadyEventHandler.onEditorReady();
        });
    }

    private void createIndexColumn() {
        TableColumn<Row, String> indexCol = new TableColumn<>(EMPTY);
        indexCol.setSortable(false);
        indexCol.setEditable(false);
        indexCol.setCellFactory(column -> {
            TableCell<Row, String> indexCell = new SimpleTextCell();
            indexCell.setTextAlignment(TextAlignment.CENTER);
            indexCell.setAlignment(Pos.BASELINE_CENTER);
            indexCell.setOnMouseReleased(event -> {
                log.debug("Mouse pressed");
                Platform.runLater(() -> {
                    TableView.TableViewSelectionModel<Row> selectionModel = tableView.getSelectionModel();
                    if (event.isShiftDown()) {
                        int start = Math.min(indexCell.getIndex(), clickedRowIdx);
                        int end = Math.max(indexCell.getIndex(), clickedRowIdx);
                        log.debug("Select rows from %d to %d".formatted(start, end));
                        selectionModel.selectRange(start, end + 1);
                    }
                    else {
                        if (!selectionModel.isSelected(indexCell.getIndex())) {
                            log.debug("Select row: " + indexCell.getIndex());
                            tableView.getSelectionModel().select(indexCell.getIndex());
                        }
                        clickedRowIdx = indexCell.getIndex();
                    }
                });
            });
            indexCell.setContextMenu(createContextMenu());
            return indexCell;
        });
        indexCol.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(String.valueOf(cellDataFeatures.getValue().getIndex() + 1)));
        tableView.getColumns().add(indexCol);
    }

    // Append a new column to the end of table columns.
    private TableColumn<Row, String> appendColumn(String header) {
        log.debug("Append new column '%s'".formatted(header));
        TableColumn<Row, String> column = new TableColumn<>(header);
        column.setMinWidth(80);
        column.setEditable(true);
        column.setSortable(false);
        column.setCellValueFactory(cellData -> {
            Row row = cellData.getValue();
            int colIdx = cellData.getTableView().getColumns().indexOf(cellData.getTableColumn());
            int dataIdx = colIdx - 1;// -1 because of the index column.
            if (dataIdx >= 0 && dataIdx < row.size()) {
                return new SimpleStringProperty(row.getData().get(dataIdx));
            }
            return null;
        });
        tableView.getColumns().add(column);
        stubColIdx = tableView.getColumns().size();
        return column;
    }


    private Row createStubRow() {
        log.debug("Append new row(stub)");
        String[] stubStrs = new String[stubColIdx]; // the stub cols index equals columns size.
        Arrays.fill(stubStrs, null); // set default to null because it will be used to indicate the empty line.
        Row stubRow = new Row();
        stubRow.setIndex(stubRowIdx);
        Collections.addAll(stubRow.getData(), stubStrs);
        return stubRow;
    }

    private ContextMenu createContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem miCopy = new MenuItem("Copy Row(s)");
        MenuItem miDelete = new MenuItem("Delete Row(s)");
        miCopy.setOnAction(event -> {
            this.copy();
        });
        miDelete.setOnAction(event -> {
            // TODO last line should be kept back
            deleteSelectedRows();
        });
        contextMenu.getItems().addAll(miCopy, miDelete);
        return contextMenu;
    }

    @Override
    public boolean copy() {
        log.debug("Copy selected cells");
        String selectionText = getSelectionText();
        ClipBoardUtils.textToClipboard(selectionText);
        return true;
    }

    @Override
    public boolean paste() {
        setFirstSelectedCell(ClipBoardUtils.textFromClipboard());
        tableView.refresh();
        return true;
    }

    @Override
    public boolean cut() {
        return false;
    }

    private void deleteSelectedRows() {
        Platform.runLater(() -> {
            ObservableList<Row> selectedRows = getSelectedRows();
            log.trace("stubRowIdx=" + stubRowIdx);
            List<Row> rowsWithoutStub = selectedRows.stream().filter(r -> r.getIndex() != stubRowIdx).toList();
            log.debug("Delete %d lines".formatted(rowsWithoutStub.size()));
            tableView.getItems().removeAll(rowsWithoutStub);
            ObservableList<Row> items = tableView.getItems();
            log.debug("%d rows left.".formatted(items.size()));
            for (int i = 0; i < items.size(); i++) {
                Row item = items.get(i);
                item.setIndex(i);
            }
            stubRowIdx = items.size() - 1;
            tableView.getSelectionModel().clearSelection();
        });
    }

    private ObservableList<Row> getSelectedRows() {
        return tableView.getSelectionModel().getSelectedItems();
    }

    private ObservableList<TablePosition> getSelectedCells() {
        return tableView.getSelectionModel().getSelectedCells();
    }

    private List<String> getSelectedCellsData() {
        ObservableList<TablePosition> selectedCells = getSelectedCells();
        System.out.println(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        return selectedCells.stream().map(pos -> {
            List<String> data = tableView.getItems().get(pos.getRow()).getData();
            if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                if (pos.getColumn() < 0) return null;
                return data.get(pos.getColumn());
            }
            return null;
        }).toList();
    }

    @Override
    public void searchNext(String keyword, TextSearchOptions options) {

    }

    @Override
    public void searchPrev(String keyword, TextSearchOptions options) {

    }

    @Override
    public void replaceSelection(String keywords, TextSearchOptions searchOptions, String replacement) {

    }

    @Override
    public void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement) {

    }

    @Override
    public void undo() {

    }

    @Override
    public void redo() {

    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public boolean isUndoAvailable() {
        return false;
    }

    @Override
    public boolean isRedoAvailable() {
        return false;
    }

    private String rowsToCsv(ObservableList<Row> rows) {
        Optional<String> reduced = rows.stream().map(row -> {
                    if (row.getData().stream().allMatch(StringUtils::isBlank)) {
                        return EMPTY;
                    }
                    else {
                        return StringUtils.join(row.getData(), ", ");
                    }
                })
                .reduce((s, s2) -> "%s\n%s".formatted(s, s2));
        return reduced.orElse(EMPTY);
    }

    private boolean saveToCache() {
        String csv = rowsToCsv(tableView.getItems());
        if (isNotBlank(csv)) {
            log.debug("Save to cache:");
            log.debug(csv);
            this.text = csv;
            return true;
        }
        return false;
    }

    @Override
    public void save() throws IOException {
        log.info("Save file: " + editorContext.getFileData().getFile());
        if (saveToCache()) {
            // TODO
//            FileUtils.write(editorContext.getFileData().getFile(),
//                    TextUtils.convertToWindows(this.text), StandardCharsets.UTF_8);
            super.isChanged = false;
            fileSavedEventHandler.onFileSaved(this.editorContext.getFileData());
        }
    }

    @Override
    public void export() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public String getSelectionText() {
        ObservableList<TablePosition> selectedCells = getSelectedCells();
        System.out.println(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        LinkedHashMap<Integer, List<TablePosition>> map =
                selectedCells.stream().collect(Collectors.groupingBy(TablePositionBase::getRow, LinkedHashMap::new, Collectors.toList()));
        return map.values().stream().map(tablePositions -> {
            return tablePositions.stream().map(pos -> {
                List<String> data = tableView.getItems().get(pos.getRow()).getData();
                if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                    if (pos.getColumn() < 0) return null;
                    String ret = data.get(pos.getColumn());
                    System.out.printf("[%d,%d]%s%n", pos.getRow(), pos.getColumn(), ret);
                    return ret;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.joining(", "));
        }).collect(Collectors.joining(LINE_SEPARATOR));
    }

    private void setFirstSelectedCell(String text) {
        Optional<TablePosition> first = tableView.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            TablePosition pos = first.get();
            List<String> data = tableView.getItems().get(pos.getRow()).getData();
            if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                data.set(Math.max(0, pos.getColumn() - 1), text);
            }
        }
    }

}
