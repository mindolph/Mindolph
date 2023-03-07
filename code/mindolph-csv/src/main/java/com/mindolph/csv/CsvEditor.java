package com.mindolph.csv;

import com.mindolph.base.EditorContext;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.TextSearchOptions;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

/**
 * @author mindolph.com@gmail.com
 */
public class CsvEditor extends BaseEditor implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CsvEditor.class);
    @FXML
    private TableView<Row> tableView;

    private TextField activeInput;
    private int activeRow;
    private TableColumn<Row, TextField> activeColumn;

    private final CSVFormat csvFormat;

    private String text; // cache
    private int stubColIdx;
    private int stubRowIdx;

    public CsvEditor(EditorContext editorContext) {
        super("/editor/csv_editor.fxml", editorContext);
        super.fileType = SupportFileTypes.TYPE_CSV;

        csvFormat = CSVFormat.DEFAULT.builder().build();

        this.reload();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Initialize");
        tableView.setEditable(true);
        tableView.setFocusTraversable(true);
        tableView.focusedProperty().addListener((observable, oldValue, newValue) -> log.debug("Focused?" + newValue));
        tableView.focusModelProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("Focus is changed to: %s".formatted(newValue.getFocusedCell()));
        });
    }

    @Override
    public void loadFile(Runnable afterLoading) throws IOException {
        FileReader fileReader = new FileReader(editorContext.getFileData().getFile(), StandardCharsets.UTF_8);
        CSVParser parsed = csvFormat.parse(fileReader);
        List<CSVRecord> records = parsed.getRecords();
        if (records.isEmpty()) {
            log.warn("No data in this csv file");
            return;
        }
        Platform.runLater(() -> {
            // == init headers ==
            // init index column
            TableColumn<Row, String> indexCol = new TableColumn<>("");
            indexCol.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().getIndex() + 1)));
            tableView.getColumns().add(indexCol);
            // init data column
            CSVRecord headers = records.get(0);
            for (String header : headers) {
                appendColumn(header);
            }
            // init stub column
            appendColumn("");

            // init data content
            for (CSVRecord record : records) {
                log.debug(StringUtils.join(record, ", "));
                Row row = new Row();
                row.setIndex(records.indexOf(record));
                CollectionUtils.addAll(row.getData(), record);
                tableView.getItems().add(row);
            }
            // stub row
            stubRowIdx = records.size(); // init the index of stub row
            appendStubRow();

            this.saveToCache();

            Platform.runLater(() -> tableView.edit(activeRow, activeColumn));

            afterLoading.run();
            this.editorReadyEventHandler.onEditorReady();
        });
    }

//    private <T> List<T> toMutableList(Iterable<T> list) {
//        List<T> l = new ArrayList<>();
//        CollectionUtils.addAll(l, list);
//        return l;
//    }

    private void appendColumn(String header) {
        log.debug("Append new column '%s'".formatted(header));
        TableColumn<Row, TextField> column = new TableColumn<>(header);
        column.setMinWidth(80);
        column.setEditable(true);
//        column.setCellFactory(new Callback<TableColumn<Row, TextField>, TableCell<Row, TextField>>() {
//            @Override
//            public TableCell<Row, TextField> call(TableColumn<Row, TextField> param) {
//                return null;
//            }
//        });
        column.setCellValueFactory(cellData -> {
            TextField textField = new TextField();
            Row row = cellData.getValue();
            int colIdx = cellData.getTableView().getColumns().indexOf(cellData.getTableColumn());
            int dataIdx = colIdx - 1;// -1 because of the index column.
            if (dataIdx >= 0 && dataIdx < row.size()) {
                textField.setText(row.getData().get(dataIdx));
            }
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!StringUtils.equals(oldValue, newValue) && StringUtils.isNotBlank(newValue)) {
                    row.updateValue(dataIdx, newValue);
                    if (row.getIndex() == 0) {
                        cellData.getTableColumn().setText(newValue); // for all columns
                    }
                    else if (row.getIndex() == stubRowIdx - 1) {
                        appendStubRow(); // last row editing activates new row.
                    }
                    if (colIdx == stubColIdx - 1) {
                        appendColumn(""); // append a stub column
                        Platform.runLater(() -> {
                            if (activeInput == null) {
                                log.debug("No active input to focus");
                            }
                            else {
                                log.debug("Focus the active input at: %d %s".formatted(activeRow, activeColumn));
//                                activeInput.requestFocus();

                                tableView.requestFocus();
                                tableView.getSelectionModel().select(row);
                                tableView.getFocusModel().focus(activeRow, activeColumn);
                                activeInput.requestFocus();
                                tableView.edit(activeRow, activeColumn);
//                                tableView.getFocusModel().focus(1);
                            }
                        });
                    }
                    fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                }
            });
            textField.focusedProperty().addListener((observable, oldValue, focused) -> {
                if (focused) {
                    activeInput = textField;
                    activeRow = row.getIndex();
                    activeColumn = cellData.getTableColumn();
                }
            });
            return new SimpleObjectProperty<>(textField);
//            return new TextFieldTableCell<>();
        });
        tableView.getColumns().add(column);
        stubColIdx = tableView.getColumns().size();
    }


    private void appendStubRow() {
        String[] stubStrs = new String[stubColIdx]; // the stub cols index equals columns size.
        Arrays.fill(stubStrs, "");
        Row stubRow = new Row();
        stubRow.setIndex(stubRowIdx);
        Collections.addAll(stubRow.getData(), stubStrs);
        tableView.getItems().add(stubRow);
        stubRowIdx = tableView.getItems().size();
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

    private boolean saveToCache() {
        Optional<String> reduced = tableView.getItems().stream().map(row -> StringUtils.join(row.getData(), ", "))
                .reduce((s, s2) -> "%s\n%s".formatted(s, s2));
        if (reduced.isPresent()) {
            log.debug("Save to cache:");
            log.debug(reduced.get());
            this.text = reduced.get();
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
        return null;
    }

}
