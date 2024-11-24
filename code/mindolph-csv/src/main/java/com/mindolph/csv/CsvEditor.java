package com.mindolph.csv;

import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.ExtTableView;
import com.mindolph.base.control.Row;
import com.mindolph.base.control.SimpleTextCell;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.EventBus.MenuTag;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.base.util.RegionUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.Anchor;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.csv.undo.UndoService;
import com.mindolph.csv.undo.UndoServiceImpl;
import com.mindolph.mfx.util.ClipBoardUtils;
import com.mindolph.mfx.util.FontUtils;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.utils.MaterialDesignIconFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.text.StringEscapeUtils;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IoUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.mindolph.base.FontIconManager.DEFAULT_ICON_SIZE;
import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author mindolph.com@gmail.com
 * @since 1.2.0
 */
public class CsvEditor extends BaseEditor implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(CsvEditor.class);
    public static final int ROW_CURRENT = 0;
    public static final int ROW_NEXT = 1;

    private ExtTableView tableView;
    private ContextMenu rowContextMenu;
    private ContextMenu cellContextMenu;

    private final CSVFormat csvFormat;
    private Callback<TableColumn<Row, String>, TableCell<Row, String>> cellFactory;
    private EventHandler<TableColumn.CellEditEvent<Row, String>> commitEditCallback;

    private EditTableCell<Row, String> focusedCell;

    private final UndoService<String> undoService;
    private final EventSource<Void> prepareSearchingEvent = new EventSource<>();
    private CellPos selectedCellPos;
    private CsvNavigator csvNavigator;

    private String text; // cache
    private int clickedRowIdx = -1;

    public CsvEditor(EditorContext editorContext) {
        super("/editor/csv_editor.fxml", editorContext);
        super.fileType = SupportFileTypes.TYPE_CSV;
        this.undoService = new UndoServiceImpl<>(o -> {
            this.text = o;
            this.doReload();
            this.prepareSearchingEvent.push(null);
            fileChangedEventHandler.onFileChanged(editorContext.getFileData());
        }, s -> "%s[%d]".formatted(StringUtils.abbreviateMiddle(s, ".", 5), s.length()));
        csvFormat = CSVFormat.DEFAULT.builder().build();

        this.refresh();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Initialize");
        tableView = new ExtTableView() {
            @Override
            protected Callback<TableColumn<Row, String>, TableCell<Row, String>> createIndexCellFactory() {
                return rowStringTableColumn -> {
                    TableCell<Row, String> indexCell = new SimpleTextCell();
                    indexCell.setTextAlignment(TextAlignment.CENTER);
                    indexCell.setAlignment(Pos.BASELINE_CENTER);
                    indexCell.setOnMouseReleased(event -> {
                        log.debug("Mouse pressed");
                        Platform.runLater(() -> {
                            if (event.isShiftDown()) {
                                log.debug("Multi rows selection");
                                int start = Math.min(indexCell.getIndex(), clickedRowIdx);
                                int end = Math.max(indexCell.getIndex(), clickedRowIdx);
                                log.debug("Select rows from %d to %d".formatted(start, end));
                                tableView.selectRows(start, end);
                                EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg("Selected rows: (%d-%d)".formatted(start + 1, end + 1)));
                            }
                            else {
                                log.debug("Single row selection");
                                if (indexCell.getIndex() >= 0) { // NOTE: weired that the index occasionally be -1
                                    log.debug("Select row: %d".formatted(indexCell.getIndex()));
                                    tableView.selectRow(indexCell.getIndex());
                                    clickedRowIdx = indexCell.getIndex();
                                    EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg("Selected row: %d".formatted(clickedRowIdx + 1)));
                                }
                            }
                        });
                        event.consume();
                    });
                    indexCell.setContextMenu(createRowContextMenu());
                    return indexCell;
                };
            }
        };
        AnchorPane.setLeftAnchor(tableView, 0d);
        AnchorPane.setRightAnchor(tableView, 0d);
        AnchorPane.setTopAnchor(tableView, 0d);
        AnchorPane.setBottomAnchor(tableView, 0d);
        tableView.setPlaceholder(new Label("No content of this CSV file"));
        tableView.setOnKeyReleased(keyEvent -> {
            log.debug("Key pressed: " + keyEvent.getCode());
            if (keyEvent.getCode() == KeyCode.DELETE) {
                tableView.setAllSelectedCells(EMPTY);
                this.saveToCache();
                keyEvent.consume();
            }
        });
        super.getChildren().add(tableView);
    }


    @Override
    public void loadFile() throws IOException {
        FileReader fileReader = new FileReader(editorContext.getFileData().getFile(), StandardCharsets.UTF_8);
        this.text = IoUtils.readAllToString(fileReader);
        this.undoService.push(this.text);
        this.applyStyles();
        this.initTableView();
        prepareSearchingEvent.subscribe(unused -> {
            // todo only available for searching
            int rowSize = tableView.getColumnSize(); // excludes index column.
            if (csvNavigator == null) {
                csvNavigator = new CsvNavigator(tableView.stream().toList(), rowSize);
            }
            else {
                csvNavigator.setData(tableView.stream().toList(), rowSize);
            }
        });
        this.outline();
    }

    private void initTableView() throws IOException {
        commitEditCallback = event -> {
            if (event.getTablePosition() != null) {
                this.onCellDataChanged(event.getTablePosition(), event.getNewValue());
                Platform.runLater(this::saveToCache);
            }
        };
        StringReader stringReader = new StringReader(this.text);
        CSVParser parsed = csvFormat.parse(stringReader);
        List<CSVRecord> records = parsed.getRecords();
        Platform.runLater(() -> {
            // == init headers ==
            // init data columns
            boolean isNeedStubCol = CollectionUtils.isEmpty(records)
                    || records.stream().anyMatch(r -> StringUtils.isNotBlank(r.get(r.size() - 1)));
            if (!records.isEmpty()) {
                // how many columns depends on the max size row.
                Optional<CSVRecord> opt = records.stream().reduce((r1, r2) -> r1.size() > r2.size() ? r1 : r2);
                int columns = opt.get().size();
                CSVRecord headers = records.get(0);
                for (int i = 0; i < columns; i++) {
                    String header = EMPTY;
                    if (i < headers.size()) {
                        header = headers.get(i);
                    }
                    tableView.appendColumn(header);
                }
            }
            // init stub column if cells of last column contains non-blank content.
            if (isNeedStubCol) {
                log.debug("Create stub column");
                tableView.appendColumn(EMPTY);
            }

            this.loadData(records);

            // text content loaded from file might be not the same with generated,
            // so it's necessary to reset the cached text to avoid redundant undo history.
            this.text = rowsToCsv(tableView.getItems());
            log.trace("cache: \n%s".formatted(this.text));

            // the cell factory will be used later.
            cellFactory = param -> {
                EditTableCell<Row, String> textCell = EditTableCell.createStringEditCell();
                textCell.setMinHeight(32);
                textCell.setEditable(true);
                textCell.textProperty().addListener((observable, oldText, newText) -> {
                    // log.debug("%s - Text changed from '%s' to '%s'".formatted(textCell.getIndex(), oldText, newText));
                    if (oldText != null && !StringUtils.equals(oldText, newText) && textCell.getIndex() >= 0) {
                        log.trace("Text from '%s' to '%s' in %d, %s".formatted(oldText, newText, textCell.getIndex(), tableView.getColumns().indexOf(textCell.getTableColumn())));
                    }
                });
                textCell.selectedProperty().addListener((observable, oldValue, newSelected) -> {
                    if (newSelected) {
                        selectedCellPos = new CellPos(textCell.getIndex(), tableView.getColumns().indexOf(textCell.getTableColumn()) - 1);
                        log.trace("selectedCellPos: %s".formatted(selectedCellPos));
                        if (csvNavigator != null) {
                            csvNavigator.moveCursor(textCell.getIndex(), tableView.getColumns().indexOf(textCell.getTableColumn()) - 1);
                        }
                        ObservableList<TablePosition> selectedCells = tableView.getSelectedCells();
                        String msg = selectedCells.size() == 1
                                ? "Selected cell (%d,%d)".formatted(selectedCellPos.getRowIdx() + 1, selectedCellPos.getColIdx() + 1)
                                : "Selected %d cells".formatted(selectedCells.size());
                        EventBus.getIns()
                                .notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg(msg))
                                .notifyMenuStateChange(MenuTag.COPY, !textCell.getTableView().getSelectionModel().isEmpty())
                                .notifyMenuStateChange(MenuTag.CUT, !textCell.getTableView().getSelectionModel().isEmpty());
                    }
                });
                textCell.setContextMenu(this.createCellContextMenu());
                textCell.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                    if (t1) {
                        focusedCell = textCell;
                    }
                });
                // handler drag&drop files
                textCell.setOnDragOver(dragEvent -> {
                    if (CollectionUtils.isEmpty(dragEvent.getDragboard().getFiles())) {
                        return;
                    }
                    Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(dragEvent.getDragboard().getFiles().get(0));
                    if (optPath.isPresent()) {
                        if (textCell.getTableRow().getIndex() <= tableView.getStubRowIdx()) {
                            dragEvent.acceptTransferModes(TransferMode.LINK);
                            RegionUtils.applyDragDropBorder(textCell);
                            Platform.runLater(textCell::requestFocus);
                        }
                    }
                });
                textCell.setOnDragExited(dragEvent -> {
                    textCell.setBorder(null);
                });
                textCell.setOnDragDropped(dragEvent -> {
                    for (File file : dragEvent.getDragboard().getFiles()) {
                        Optional<String> optPath = super.getRelatedPathInCurrentWorkspace(file);
                        if (optPath.isPresent()) {
                            onCellDataChanged(textCell.getTableRow().getIndex(), textCell.getTableColumn(), optPath.get());
                            saveToCache();
                        }
                        else {
                            log.warn("Link files not in same workspace are not supported yet");
                        }
                    }
                });
                return textCell;
            };
            // NOTE: call setCellFactory here(in runLater()) otherwise the textProperty change event emitting messily.
            Platform.runLater(() -> {
                        ObservableList<TableColumn<Row, ?>> columns = tableView.getColumns();
                        for (int i = 1; i < columns.size(); i++) { // from 1 to exclude index column
                            TableColumn<Row, String> column = (TableColumn<Row, String>) columns.get(i);
                            column.setCellFactory(cellFactory);
                            column.setOnEditCommit(commitEditCallback);
                        }
                    }
            );

            log.debug("%d data columns and %d row initialized".formatted(tableView.getColumnSize(), tableView.getItems().size()));

//            this.editorReadyEventHandler.onEditorReady();
            EventBus.getIns().notifyFileLoaded(editorContext.getFileData());
        });
    }

    // this is call when editing committed
    private void onCellDataChanged(TablePosition<Row, String> tablePosition, String text) {
        this.onCellDataChanged(tablePosition.getRow(), tablePosition.getTableColumn(), text);
    }

    // this is called when pasting text to cell.
    private void onCellDataChanged(int rowIdx, TableColumn<Row, String> column, String newText) {
        log.debug("onDataChanged()");
        log.debug("stubRowIdx: %d - stubColIdx: %d".formatted(tableView.getStubRowIdx(), tableView.getStubColIdx()));
        int colIdx = tableView.getColumns().indexOf(column);
        int dataIdx = colIdx - 1;// -1 because of the index column.
        if (tableView.getItems().isEmpty()) {
            return; // avoid exception when undo editing from an empty row.
        }
        Row row = tableView.getItems().get(rowIdx);
        row.updateValue(dataIdx, newText);
        if (rowIdx == 0) {
            column.setText(newText); // update text for any columns
        }
        if (StringUtils.isNotBlank(newText)) {
            if (rowIdx == tableView.getStubRowIdx()) {
                log.debug("Add new stub row since the stub row is changed");
                tableView.appendStubRowAndScrollToBottom();
            }
            if (tableView.isStubColumn(column)) {
                log.debug("Add new stub column since the stub column is changed");
                TableColumn<Row, String> stubCol = tableView.appendColumn(EMPTY);
                stubCol.setCellFactory(cellFactory);
                stubCol.setOnEditCommit(commitEditCallback);
            }
        }
        tableView.refresh();
    }

    @Override
    public void applyStyles() {
        Font defFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_CSV_EDITOR);
        Font font = fxPreferences.getPreference(FontConstants.KEY_CSV_EDITOR, Font.class, defFont);
        tableView.setStyle(FontUtils.fontToCssStyle(font));
    }

    @Override
    public void locate(Anchor anchor) {
        if (anchor instanceof CsvAnchor ca) {
            log.debug("Select %d - %d".formatted(ca.getRow(), ca.getCol()));
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().select(ca.getRow(), tableView.getColumns().get(ca.getCol()));
        }
        else {
            log.warn("No anchor to locate");
        }
    }

    private void doReload() {
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
            log.trace("* " + record.stream().map("'%s'"::formatted).collect(Collectors.joining(",")));
            Row newRow = ExtTableView.createRow(tableView.getColumnSize());
            newRow.setIndex(records.indexOf(record));
            for (int i = 0; i < record.size(); i++) {
                newRow.getData().set(i, record.get(i));
            }
            rows.add(newRow);
        }
        tableView.appendRows(rows);
        // stub row
        tableView.appendStubRow();
    }

    private ContextMenu createRowContextMenu() {
        rowContextMenu = new ContextMenu();
        MenuItem miInsertBefore = new MenuItem("Insert New Line Before");
        miInsertBefore.setGraphic(MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_ROW_PLUS_BEFORE, DEFAULT_ICON_SIZE));
        MenuItem miInsertAfter = new MenuItem("Insert New Line After");
        miInsertAfter.setGraphic(MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_ROW_PLUS_AFTER, DEFAULT_ICON_SIZE));
        MenuItem miCopy = new MenuItem("Copy", FontIconManager.getIns().getIcon(IconKey.COPY));
        MenuItem miPaste = new MenuItem("Paste", FontIconManager.getIns().getIcon(IconKey.PASTE));
        MenuItem miCut = new MenuItem("Cut", FontIconManager.getIns().getIcon(IconKey.CUT));
        MenuItem miDelete = new MenuItem("Delete Row(s)", FontIconManager.getIns().getIcon(IconKey.DELETE));
        miInsertBefore.setOnAction(event -> {
            this.insertNewRow(ROW_CURRENT); // replace current actually
        });
        miInsertAfter.setOnAction(event -> {
            this.insertNewRow(ROW_NEXT); // insert to next
        });
        miCut.setOnAction(event -> {
            this.copy();
            tableView.setAllSelectedCells(EMPTY);
            saveToCache();
        });
        miCopy.setOnAction(event -> {
            this.copy();
        });
        miPaste.setOnAction(event -> {
            this.paste();
        });
        miDelete.setOnAction(event -> {
            tableView.deleteSelectedRows();
            saveToCache();
        });
        Clipboard clipboard = Clipboard.getSystemClipboard();
        miPaste.setDisable(!clipboard.hasContent(DataFormat.PLAIN_TEXT));
        rowContextMenu.getItems().addAll(miCut, miCopy, miPaste, miInsertBefore, miInsertAfter, miDelete);
        return rowContextMenu;
    }

    private ContextMenu createCellContextMenu() {
        cellContextMenu = new ContextMenu();
        MenuItem miEdit = new MenuItem("Edit", FontIconManager.getIns().getIcon(IconKey.EDIT_TEXT));
        MenuItem miCut = new MenuItem("Cut", FontIconManager.getIns().getIcon(IconKey.CUT));
        MenuItem miCopy = new MenuItem("Copy", FontIconManager.getIns().getIcon(IconKey.COPY));
        MenuItem miPaste = new MenuItem("Paste", FontIconManager.getIns().getIcon(IconKey.PASTE));
        MenuItem miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
        miDelete.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        miEdit.setOnAction(event -> {
            if (focusedCell != null) {
                focusedCell.startEdit();
            }
        });
        miCut.setOnAction(event -> {
            this.cut();
        });
        miCopy.setOnAction(event -> {
            this.copy();
        });
        miPaste.setOnAction(event -> {
            this.paste();
        });
        miDelete.setOnAction(event -> {
            tableView.setAllSelectedCells(EMPTY);
        });
        Clipboard clipboard = Clipboard.getSystemClipboard();
        miPaste.setDisable(!clipboard.hasContent(DataFormat.PLAIN_TEXT));
        cellContextMenu.getItems().addAll(miEdit, miCut, miCopy, miPaste, new SeparatorMenuItem(), miDelete);
        return cellContextMenu;
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
            String text = ClipBoardUtils.textFromClipboard();
            if (StringUtils.isNotBlank(text) && !StringUtils.containsOnly(text, ", ")) {
                try {
                    StringReader stringReader = new StringReader(text);
                    CSVParser parsed = csvFormat.parse(stringReader);
                    List<CSVRecord> records = parsed.getRecords();
                    log.debug("Paste %d rows".formatted(records.size()));
                    TablePosition startCell = tableView.getFocusModel().getFocusedCell();
                    for (int i = 0; i < records.size(); i++) {
                        CSVRecord record = records.get(i);
                        log.debug("Paste row with %d cells".formatted(record.size()));
                        for (int j = 0; j < record.size(); j++) {
                            String newValue = record.get(j);
                            TableColumn<Row, String> column = (TableColumn<Row, String>) tableView.getColumns().get(startCell.getColumn() + j);
                            this.onCellDataChanged(startCell.getRow() + i, column, newValue);
                        }
                    }
                    this.saveToCache();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    @Override
    public boolean cut() {
        log.debug("Cut first selected cell");
        if (tableView.isFocused()) {
            String selectionText = getSelectionText();
            ClipBoardUtils.textToClipboard(selectionText);
            tableView.setAllSelectedCells(EMPTY);
            this.saveToCache();
        }
        return false;
    }

    private void insertNewRow(int offset) {
        Row selectedRow = tableView.getSelectionModel().getSelectedItem();
        if (selectedRow != null) {
            int newIdx = selectedRow.getIndex() + offset;
            if (newIdx >= 0 && newIdx < tableView.getItems().size()) {
                tableView.insertNewRow(newIdx);
                this.saveToCache();
            }
        }
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
        if (csvNavigator == null) {
            prepareSearchingEvent.push(null);
        }
        CellPos foundCellPos;
        if (selectedCellPos == null) {
            csvNavigator.moveCursor(reverse ? csvNavigator.getTotal() - 1 : 0); // reset the cursor
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
        String firstSelectedText = tableView.getFirstSelectedText();
        if (contains.apply(firstSelectedText, keywords)) {
            replacement = replacement == null ? EMPTY : replacement;
            TriFunction<String, String, String, String> replace = searchOptions.isCaseSensitive() ? StringUtils::replace : StringUtils::replaceIgnoreCase;
            String applied = replace.apply(firstSelectedText, keywords, replacement);
            TablePosition<Row, String> pos = tableView.setFirstSelectedCell(applied);
            this.onCellDataChanged(pos.getRow(), pos.getTableColumn(), applied);
            this.saveToCache();
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
            this.doReload();
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
        log.debug("convert rows to csv without stub row %d and col %d".formatted(tableView.getStubRowIdx(), tableView.getStubColIdx()));
        Optional<String> reduced = rows.stream()
                .filter(row -> row.getIndex() != tableView.getStubRowIdx()) // exclude stub row
                .map(row -> {
                    return row.getData().stream().filter(e -> row.getData().indexOf(e) < tableView.getStubColIdx()) // exclude stub column
                            .map(s -> s == null ? EMPTY : s).map(StringEscapeUtils::escapeCsv)
                            .collect(Collectors.joining(","));
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
        prepareSearchingEvent.push(null);
        EventBus.getIns().notifyMenuStateChange(MenuTag.UNDO, this.undoService.isUndoAvailable());
        fileChangedEventHandler.onFileChanged(editorContext.getFileData());
    }


    @Override
    public void save() throws IOException {
        log.info("Save cache to file: %s".formatted(editorContext.getFileData().getFile()));
        FileUtils.write(editorContext.getFileData().getFile(),
                super.convertByOs(this.text), StandardCharsets.UTF_8);
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
        Map<Integer, List<TablePosition>> map = tableView.getSelectedCellsByRow();
        return map.values().stream().map(tablePositions ->
                        tablePositions.stream().map(pos -> {
                                    List<String> data = tableView.getItems().get(pos.getRow()).getData();
                                    if (CollectionUtils.isNotEmpty(data) && pos.getColumn() < data.size()) {
                                        if (pos.getColumn() <= 0) return null;
                                        String ret = data.get(pos.getColumn() - 1);
                                        log.trace("[%d,%d] %s", pos.getRow(), pos.getColumn(), ret);
                                        return ret;
                                    }
                                    return null;
                                }).filter(Objects::nonNull).map(StringEscapeUtils::escapeCsv)
                                .collect(Collectors.joining(", ")))
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    private CellPos getFocusedCellPosition() {
        return CellPos.fromTablePosition(tableView.getFocusModel().getFocusedCell());
    }

    private CellPos getSelectedCellPosition() {
        Optional<TablePosition> first = tableView.getSelectionModel().getSelectedCells().stream().findFirst();
        return first.map(CellPos::fromTablePosition).orElse(null);
    }

}
