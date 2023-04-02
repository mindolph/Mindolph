package com.mindolph.base.control;

import javafx.scene.control.TableCell;

/**
 * @author mindolph.com@gmail.com
 * @see ExtTableView
 */
public class SimpleTextCell extends TableCell<Row, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
    }
}
