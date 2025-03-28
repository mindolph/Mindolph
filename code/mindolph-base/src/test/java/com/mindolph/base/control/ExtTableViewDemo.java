package com.mindolph.base.control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.apache.commons.lang3.RandomStringUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class ExtTableViewDemo implements Initializable {

    @FXML
    HBox content;

    DemoTableView demoTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        demoTableView = new DemoTableView();
        demoTableView.setEditable(true);// seems doesn't work

        demoTableView.appendColumn("col 1");
        demoTableView.appendColumn("col 2");
        demoTableView.appendColumn("col 3");
        demoTableView.appendColumn("");// stub

        for (int i = 0; i < 10; i++) {
            Row row = demoTableView.appendStubRow();
            row.setIndex(i);
            for (int j = 0; j < 3; j++) {
                row.updateValue(j, RandomStringUtils.randomAlphabetic(8));
            }
        }
        demoTableView.appendStubRow();// stub row
        content.getChildren().add(demoTableView);
    }

    @FXML
    private void onInsertStubRow() {
        Row selectedRow = demoTableView.getSelectionModel().getSelectedItem();
        int newIdx = selectedRow.getIndex() + 1;
        demoTableView.insertNewRow(newIdx);
    }

    @FXML
    private void onAppendStubColumn() {
        demoTableView.appendColumn("New stub col");
    }

    @FXML
    private void onDeleteSelectedRows() {
        demoTableView.deleteSelectedRows();
        demoTableView.reOrder();
    }

    public static class DemoTableView extends ExtTableView {
        @Override
        protected Callback<TableColumn<Row, String>, TableCell<Row, String>> createIndexCellFactory() {
            return rowStringTableColumn -> {
                TableCell<Row, String> indexCell = new SimpleTextCell();
                indexCell.setTextAlignment(TextAlignment.CENTER);
                indexCell.setAlignment(Pos.BASELINE_CENTER);
                return indexCell;
            };
        }
    }

}
