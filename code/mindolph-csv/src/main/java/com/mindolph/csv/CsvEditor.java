package com.mindolph.csv;

import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.EventBus.MenuTag;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.core.util.IoUtils;
import com.mindolph.csv.undo.UndoService;
import com.mindolph.csv.undo.UndoServiceImpl;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.TextUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author mindolph.com@gmail.com
 */
public class CsvEditor extends BaseEditor implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CsvEditor.class);
    public static final int ROW_CURRENT = 0;
    public static final int ROW_NEXT = 1;
    @FXML
    private TableView<Row> tableView;
    private ContextMenu contextMenu;

    private final CSVFormat csvFormat;
    private Callback<TableColumn<Row, String>, TableCell<Row, String>> cellFactory;
    private EventHandler<TableColumn.CellEditEvent<Row, String>> commitEditHandler;

    private final UndoService<String> undoService;
    private final EventSource<Void> dataChangedEvent = new EventSource<>();
    private CellPos selectedCellPos;
    private CsvNavigator csvNavigator;

    private String text; // cache
    private int stubColIdx;
    private int stubRowIdx;
    private int clickedRowIdx = -1;

    public CsvEditor(EditorContext editorContext) {
        super("/editor/csv_editor.fxml", editorContext);
        super.fileType = SupportFileTypes.TYPE_CSV;
        this.undoService = new UndoServiceImpl<>(o -> {
            this.text = o;
            this.reload();
            this.dataChangedEvent.push(null);
            fileChangedEventHandler.onFileChanged(editorContext.getFileData());
        }, s -> "%s[%d]".formatted(StringUtils.abbreviate(s, 5), s.length()));
        csvFormat = CSVFormat.DEFAULT.builder().build();
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
        this.text = IoUtils.readAll(fileReader);
        this.undoService.push(this.text);
        this.initTableView();
        dataChangedEvent.subscribe(unused -> {
            // todo only available for searching
            int rowSize = tableView.getColumns().size() - 1 - 1; // excludes index column and stub column
            if (csvNavigator == null) {
                csvNavigator = new CsvNavigator(this.stream().filter(Objects::nonNull).toList(), rowSize);
            }
            else {
                csvNavigator.setData(this.stream().filter(Objects::nonNull).toList(), rowSize);
            }
        });
        Platform.runLater(() -> {
            afterLoading.run();
        });
    }

    private void initTableView() throws IOException {
        StringReader stringReader = new StringReader(this.text);
        CSVParser parsed = csvFormat.parse(stringReader);
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
            appendColumn(EMPTY);

            this.loadData(records);

            commitEditHandler = event -> {
                Platform.runLater(() -> {
                    saveToCache();
                });
            };
            // the cell factory will be used later.
            cellFactory = param -> {
                EditTableCell<Row, String> textCell = EditTableCell.createStringEditCell();
                textCell.setMinHeight(32);
                textCell.setEditable(true);
                textCell.textProperty().addListener((observable, oldText, newText) -> {
                    // log.debug("%s - Text changed from '%s' to '%s'".formatted(textCell.getIndex(), oldText, newText));
                    if (oldText != null && !StringUtils.equals(oldText, newText) && StringUtils.isNotBlank(newText)) {
                        log.debug("Text from '%s' to '%s'".formatted(oldText, newText));
                        this.onCellTextChanged(textCell.getIndex(), textCell.getTableColumn(), newText);
                    }
                });
                textCell.selectedProperty().addListener((observable, oldValue, newSelected) -> {
                    if (newSelected) {
                        selectedCellPos = new CellPos(textCell.getIndex(), tableView.getColumns().indexOf(textCell.getTableColumn()) - 1);
                        log.trace("selectedCellPos: %s".formatted(selectedCellPos));
                        if (csvNavigator != null) {
                            csvNavigator.moveCursor(textCell.getIndex(), tableView.getColumns().indexOf(textCell.getTableColumn()) - 1);
                        }
                        EventBus.getIns()
                                .notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg("Selected cell [%d-%d]".formatted(selectedCellPos.getRowIdx() + 1, selectedCellPos.getColIdx() + 1)))
                                .notifyMenuStateChange(MenuTag.COPY, !textCell.getTableView().getSelectionModel().isEmpty());
                    }
                });
                return textCell;
            };
            // NOTE: call setCellFactory here otherwise the textProperty change event emitting messily.
            Platform.runLater(() -> {
                        ObservableList<TableColumn<Row, ?>> columns = tableView.getColumns();
                        for (int i = 1; i < columns.size(); i++) { // from 1 to exclude index column
                            TableColumn<Row, String> column = (TableColumn<Row, String>) columns.get(i);
                            column.setCellFactory(cellFactory);
                            column.setOnEditCommit(commitEditHandler);
                        }
                    }
            );

            log.debug("%d columns and %d row initialized".formatted(tableView.getColumns().size() - 1, tableView.getItems().size()));

            this.editorReadyEventHandler.onEditorReady();
        });
    }

    // wherever text changed, this will be called. like commit editing or paste text to cell.
    private void onCellTextChanged(int rowIdx, TableColumn<Row, String> column, String newText) {
        int colIdx = tableView.getColumns().indexOf(column);
        int dataIdx = colIdx - 1;// -1 because of the index column.
        Row row = tableView.getItems().get(rowIdx);
        row.updateValue(dataIdx, newText);
        Platform.runLater(() -> {
            if (rowIdx == 0) {
                column.setText(newText); // for all columns
            }
            if (rowIdx == stubRowIdx) {
                stubRowIdx++;
                Row stubRow = createStubRow(tableView.getColumns().size() - 1);// last row editing activates new row.
                tableView.getItems().add(stubRow);
            }
            if (colIdx == stubColIdx - 1) {
                TableColumn<Row, String> stubCol = appendColumn(EMPTY);
                stubCol.setCellFactory(cellFactory); // append a stub column
                stubCol.setOnEditCommit(commitEditHandler);
            }
            fileChangedEventHandler.onFileChanged(editorContext.getFileData());
        });
    }


    @Override
    public void reload() {
        log.debug("Reload data");
        tableView.getItems().clear();
        // reload all data from cached text;
        StringReader stringReader = new StringReader(this.text);
        try {
            CSVParser parsed = csvFormat.parse(stringReader);
            List<CSVRecord> records = parsed.getRecords();
            loadData(records);
            tableView.refresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.debug("done");
    }

    // update data with updating table columns.
    private void loadData(List<CSVRecord> records) {
        // update column title with records.
        ObservableList<TableColumn<Row, ?>> columns = tableView.getColumns();
        if (!records.isEmpty()) {
            CSVRecord headers = records.get(0);
            List<String> headerList = headers.stream().toList();
            for (int i = 0; i < headerList.size(); i++) {
                String header = headerList.get(i);
                if (i < columns.size() - 1) {
                    columns.get(i + 1).setText(header);
                }
            }
        }
        else {
            columns.forEach(col -> col.setText(EMPTY));
        }
        // init data content
        List<Row> rows = new LinkedList<>();
        for (CSVRecord record : records) {
            log.debug("* " + StringUtils.join(record, ", "));
            Row row = new Row();
            row.setIndex(records.indexOf(record));
            CollectionUtils.addAll(row.getData(), record);
            row.getData().add(null); // for stub column
            rows.add(row);
        }
        // stub row
        stubRowIdx = records.size();
        rows.add(createStubRow(columns.size() - 1)); // excludes index column
        tableView.getItems().addAll(rows);
    }

    private void createIndexColumn() {
        log.debug("Create index column");
        TableColumn<Row, String> indexCol = new TableColumn<>(EMPTY);
        indexCol.setSortable(false);
        indexCol.setEditable(false);
//        indexCol.setStyle("-fx-background-color: lightgrey;");
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
                            log.debug("Select row: %d".formatted(indexCell.getIndex()));
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


    private Row createStubRow(int size) {
        log.debug("Append new row(stub)");
        String[] stubStrs = new String[size]; // the stub cols index equals columns size.
        Arrays.fill(stubStrs, null); // set default to null because it will be used to indicate the empty line.
        Row stubRow = new Row();
        stubRow.setIndex(stubRowIdx);
        Collections.addAll(stubRow.getData(), stubStrs);
        return stubRow;
    }

    private ContextMenu createContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem miInsertBefore = new MenuItem("Insert New Line Before");
        MenuItem miInsertAfter = new MenuItem("Insert New Line After");
        MenuItem miCopy = new MenuItem("Copy Row(s)");
        MenuItem miDelete = new MenuItem("Delete Row(s)");
        miDelete.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        miInsertBefore.setOnAction(event -> {
            this.insertNewRow(ROW_CURRENT); // replace current actually
        });
        miInsertAfter.setOnAction(event -> {
            this.insertNewRow(ROW_NEXT); // insert to next
        });
        miCopy.setOnAction(event -> {
            this.copy();
        });
        miDelete.setOnAction(event -> {
            // TODO last line should be kept back
            this.deleteSelectedRows();
        });
        contextMenu.getItems().addAll(miInsertBefore, miInsertAfter, miCopy, miDelete);
        return contextMenu;
    }

    @Override
    public boolean copy() {
        log.debug("Copy selected cells");
        if (tableView.isFocused()) {
            String selectionText = getSelectionText();
            ClipBoardUtils.textToClipboard(selectionText);
        }
        return true;
    }

    @Override
    public boolean paste() {
        if (tableView.isFocused()) {
            setFirstSelectedCell(ClipBoardUtils.textFromClipboard());
            tableView.refresh();
            this.saveToCache();
        }
        return true;
    }

    @Override
    public boolean cut() {
        // TODO
        return false;
    }

    private void insertNewRow(int offset) {
        Row selectedRow = tableView.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            int newIdx = selectedRow.getIndex() + offset;
            if (newIdx >= 0 && newIdx < tableView.getItems().size()) {
                Row newRow = createStubRow(tableView.getColumns().size() - 1);
                newRow.setIndex(newIdx);
                stubRowIdx++; //must increase stub row index before insert new row, otherwise the last row's moving will trigger redundant new stub row created.
                tableView.getSelectionModel().clearSelection();
                tableView.getItems().add(newIdx, newRow);
                this.reOrder();
                tableView.refresh();
                this.saveToCache();
            }
        }
    }

    private void deleteSelectedRows() {
        Platform.runLater(() -> {
            ObservableList<Row> selectedRows = getSelectedRows();
            log.trace("stubRowIdx=%d".formatted(stubRowIdx));
            List<Row> rowsWithoutStub = selectedRows.stream().filter(r -> r.getIndex() != stubRowIdx).toList();
            log.debug("Delete %d lines".formatted(rowsWithoutStub.size()));
            tableView.getItems().removeAll(rowsWithoutStub);
            this.reOrder();
            stubRowIdx = tableView.getItems().size() - 1;
            tableView.getSelectionModel().clearSelection();
            saveToCache();
        });
    }

    private void reOrder() {
        ObservableList<Row> items = tableView.getItems();
        log.debug("%d rows left.".formatted(items.size()));
        for (int i = 0; i < items.size(); i++) {
            Row item = items.get(i);
            item.setIndex(i);
        }
    }

    private ObservableList<Row> getSelectedRows() {
        return tableView.getSelectionModel().getSelectedItems();
    }

    private ObservableList<TablePosition> getSelectedCells() {
        return tableView.getSelectionModel().getSelectedCells();
    }

    private List<String> getSelectedCellsData() {
        ObservableList<TablePosition> selectedCells = getSelectedCells();
        log.debug(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        return selectedCells.stream().map(pos -> {
            List<String> data = tableView.getItems().get(pos.getRow()).getData();
            if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                if (pos.getColumn() < 0) return null;
                return data.get(pos.getColumn());
            }
            return null;
        }).toList();
    }

    private Stream<String> stream() {
        return tableView.getItems().stream().flatMap(row -> row.getData().stream());
    }

    private CellPos getSelectedCellPosition() {
        Optional<TablePosition> first = tableView.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            return CellPos.fromTablePosition(first.get());
        }
        return null;
    }

    @Override
    public void searchNext(String keyword, TextSearchOptions options) {
        this.search(keyword, options, false);
    }

    @Override
    public void searchPrev(String keyword, TextSearchOptions options) {
        this.search(keyword, options, true);
    }

    private void search(String keyword, TextSearchOptions options, boolean reverse) {
        int rowSize = tableView.getColumns().size() - 1 - 1; // excludes index column and stub column
        if (csvNavigator == null) {
            csvNavigator = new CsvNavigator(this.stream().filter(Objects::nonNull).toList(), rowSize);
        }
        CellPos foundCellPos;
        if (selectedCellPos == null) {
            csvNavigator.moveCursor(reverse ? csvNavigator.getTotal() - 1 : 0);
        }
        else {
            if (reverse) csvNavigator.moveCursorPrev();
            else csvNavigator.moveCursorNext();
        }
        BiFunction<String, Boolean, CellPos> locate = reverse ? csvNavigator::locatePrev : csvNavigator::locateNext;
        foundCellPos = locate.apply(keyword, options.isCaseSensitive());
        log.debug("Found cell: %s".formatted(foundCellPos));
        if (foundCellPos != null) {
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().select(foundCellPos.getRowIdx(), tableView.getColumns().get(foundCellPos.getColIdx() + 1));
        }
        else {
            selectedCellPos = null;
        }
    }

    @Override
    public void replaceSelection(String keywords, TextSearchOptions searchOptions, String replacement) {
        BiFunction<String, String, Boolean> contains = searchOptions.isCaseSensitive() ? StringUtils::contains : StringUtils::containsIgnoreCase;
        String firstSelectedText = getFirstSelectedText();
        if (contains.apply(firstSelectedText, keywords)) {
            TriFunction<String, String, String, String> replace = searchOptions.isCaseSensitive() ? StringUtils::replace : StringUtils::replaceIgnoreCase;
            String applied = replace.apply(getFirstSelectedText(), keywords, replacement);
            this.setFirstSelectedCell(applied);
            this.saveToCache();
            tableView.refresh();
        }
        this.searchNext(keywords, searchOptions);
    }

    @Override
    public void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement) {
        // replace in cache
        BiFunction<String, String, Boolean> contains = searchOptions.isCaseSensitive() ? StringUtils::contains : StringUtils::containsIgnoreCase;
        if (contains.apply(this.text, keywords)) {
            TriFunction<String, String, String, String> replace = searchOptions.isCaseSensitive() ? StringUtils::replace : StringUtils::replaceIgnoreCase;
            this.text = replace.apply(this.text, keywords, replacement);
            this.emmitEventsSinceCacheChanged();
            this.reload();
        }
    }

    @Override
    public void undo() {
        this.undoService.undo();
        EventBus.getIns().notifyMenuStateChange(MenuTag.UNDO, this.undoService.isUndoAvailable())
                .notifyMenuStateChange(MenuTag.REDO, this.undoService.isRedoAvailable());
    }

    @Override
    public void redo() {
        this.undoService.redo();
        EventBus.getIns().notifyMenuStateChange(MenuTag.UNDO, this.undoService.isUndoAvailable())
                .notifyMenuStateChange(MenuTag.REDO, this.undoService.isRedoAvailable());
    }

    @Override
    public boolean isSelected() {
        return !tableView.getSelectionModel().isEmpty();
    }

    @Override
    public boolean isUndoAvailable() {
        return this.undoService.isUndoAvailable();
    }

    @Override
    public boolean isRedoAvailable() {
        return this.undoService.isRedoAvailable();
    }

    private String rowsToCsv(ObservableList<Row> rows) {
        Optional<String> reduced = rows.stream()
                .filter(row -> row.getIndex() != stubRowIdx)
                .map(row -> {
                    return row.getData().stream().map(s -> s == null ? EMPTY : s)
                            .map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(","));
                })
                .reduce((s, s2) -> "%s\n%s".formatted(s, s2));
        return reduced.orElse(EMPTY);
    }

    private boolean saveToCache() {
        String csv = rowsToCsv(tableView.getItems());
        if (!StringUtils.equals(csv, this.text)) {
            log.debug("Save to cache:");
            log.trace(csv);
            this.text = csv;
            this.emmitEventsSinceCacheChanged();
            return true;
        }
        else {
            log.debug("Nothing new to save to cache");
        }
        return false;
    }

    private void emmitEventsSinceCacheChanged() {
        super.isChanged = true;
        undoService.push(text);
        dataChangedEvent.push(null);
        EventBus.getIns().notifyMenuStateChange(MenuTag.UNDO, this.undoService.isUndoAvailable());
        fileChangedEventHandler.onFileChanged(editorContext.getFileData());
    }


    @Override
    public void save() throws IOException {
        log.info("Save cache to file: %s".formatted(editorContext.getFileData().getFile()));
        FileUtils.write(editorContext.getFileData().getFile(),
                TextUtils.convertToWindows(this.text), StandardCharsets.UTF_8);
        super.isChanged = false;
        fileSavedEventHandler.onFileSaved(this.editorContext.getFileData());
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
        log.debug(StringUtils.join(selectedCells.stream().map(tablePosition -> "[%d,%d]".formatted(tablePosition.getRow(), tablePosition.getColumn())).toList(), " "));
        LinkedHashMap<Integer, List<TablePosition>> map =
                selectedCells.stream().collect(Collectors.groupingBy(TablePositionBase::getRow, LinkedHashMap::new, Collectors.toList()));
        return map.values().stream().map(tablePositions -> {
            return tablePositions.stream().map(pos -> {
                List<String> data = tableView.getItems().get(pos.getRow()).getData();
                if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                    if (pos.getColumn() <= 0) return null;
                    String ret = data.get(pos.getColumn() - 1);
                    log.trace("[%d,%d] %s%n", pos.getRow(), pos.getColumn(), ret);
                    return ret;
                }
                return null;
            }).filter(Objects::nonNull).map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(", "));
        }).collect(Collectors.joining(LINE_SEPARATOR));
    }

    private String getFirstSelectedText() {
        Optional<TablePosition> first = tableView.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            TablePosition pos = first.get();
            Row row = tableView.getItems().get(pos.getRow());
            return row.getData().get(pos.getColumn() - 1);
        }
        return EMPTY;
    }

    private void setFirstSelectedCell(String text) {
        Optional<TablePosition> first = tableView.getSelectionModel().getSelectedCells().stream().findFirst();
        if (first.isPresent()) {
            TablePosition<Row, String> pos = first.get();
            List<String> data = tableView.getItems().get(pos.getRow()).getData();
            int dataPos = pos.getColumn() - 1;
            if (CollectionUtils.isNotEmpty(data) && dataPos < data.size()) {
                data.set(Math.max(0, dataPos), text);
            }
            // replacing text causes whole updating
            this.onCellTextChanged(pos.getRow(), pos.getTableColumn(), text);
        }
    }

}
