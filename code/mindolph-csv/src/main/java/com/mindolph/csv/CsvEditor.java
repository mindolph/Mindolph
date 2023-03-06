package com.mindolph.csv;

import com.mindolph.base.EditorContext;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.util.TextUtils;
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
import org.apache.commons.io.FileUtils;
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


    private final CSVFormat csvFormat;

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

    }

    @Override
    public void loadFile(Runnable afterLoading) throws IOException {
        FileReader f = new FileReader(editorContext.getFileData().getFile(), StandardCharsets.UTF_8);
        CSVParser parsed = csvFormat.parse(f);

        List<CSVRecord> records = parsed.getRecords();
        if (records.isEmpty()) {
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
        TableColumn<Row, TextField> column = new TableColumn<>(header);
        column.setMinWidth(80);
        column.setCellValueFactory(cellData -> {
            TextField textField = new TextField();
            Row row = cellData.getValue();
            int colIdx = cellData.getTableView().getColumns().indexOf(cellData.getTableColumn());
            int dataIdx = colIdx - 1;// -1 because of the index column.
            if (dataIdx >= 0 && dataIdx < row.size()) {
                textField.setText(row.getData().get(dataIdx));
            }
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!StringUtils.equals(oldValue, newValue)) {
                    row.updateValue(dataIdx, newValue);
                    if (row.getIndex() == 0) {
                        cellData.getTableColumn().setText(newValue); // for all columns
                    }
                    else if (row.getIndex() == stubRowIdx - 1) {
                        appendStubRow(); // last row editing activates new row.
                    }
                    if (colIdx == stubColIdx -1) {
                        appendColumn(""); // append a stub column
                    }
                    fileChangedEventHandler.onFileChanged(editorContext.getFileData());
                }
            });
            return new SimpleObjectProperty<>(textField);
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

    @Override
    public void save() throws IOException {
        log.info("Save file: " + editorContext.getFileData().getFile());
        Optional<String> reduced = tableView.getItems().stream().map(strings -> StringUtils.join(strings, ", "))
                .reduce((s, s2) -> "%s\n%s".formatted(s, s2));

        if (reduced.isPresent()) {
            FileUtils.write(editorContext.getFileData().getFile(),
                    TextUtils.convertToWindows(reduced.get()), StandardCharsets.UTF_8);
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
