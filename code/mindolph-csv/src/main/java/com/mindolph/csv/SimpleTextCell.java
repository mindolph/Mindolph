package com.mindolph.csv;

import javafx.scene.control.TableCell;

/**
 * @author allen
 */
public class SimpleTextCell extends TableCell<Row, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
    }
}
