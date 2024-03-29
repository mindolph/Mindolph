package com.mindolph.csv;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://gist.github.com/james-d/be5bbd6255a4640a5357
 *
 * @author mindolph.com@gmail.com
 */
public class EditTableCell<S, T> extends TableCell<S, T> {

    private static final Logger log = LoggerFactory.getLogger(EditTableCell.class);

    // Text field for editing
    private final TextField textField = new TextField();

    // Converter for converting the text in the text field to the user type, and vice-versa:
    private final StringConverter<T> converter;

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        }
        else {
            setText(converter.toString(item));
        }
    }

    public EditTableCell(StringConverter<T> converter) {
        this.converter = converter;
        setGraphic(textField);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        this.setTextAlignment(TextAlignment.CENTER);
        this.setAlignment(Pos.BASELINE_CENTER);

        this.focusedProperty().addListener((observable, oldValue, isNowFocused) -> {
            log.trace("%s input focus changed: %s".formatted(this, isNowFocused));
        });

        textField.setOnAction(evt -> {
            this.requestFocus(); // request cell focus to emmit the text filed focus property changes to commit the edit.
        });
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            log.trace("%s input focus changed: %s".formatted(textField, isNowFocused));
            if (!isNowFocused) {
                commitEdit(this.converter.fromString(textField.getText()));
            }
        });
        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(converter.toString(getItem()));
                cancelEdit();
                event.consume();
            }
//            else if (event.getCode() == KeyCode.RIGHT) {
//                getTableView().getSelectionModel().selectRightCell();
//                event.consume();
//            }
//            else if (event.getCode() == KeyCode.LEFT) {
//                getTableView().getSelectionModel().selectLeftCell();
//                event.consume();
//            }
//            else if (event.getCode() == KeyCode.UP) {
//                getTableView().getSelectionModel().selectAboveCell();
//                event.consume();
//            }
//            else if (event.getCode() == KeyCode.DOWN) {
//                getTableView().getSelectionModel().selectBelowCell();
//                event.consume();
//            }
        });
    }

    /**
     * Convenience method for creating an EditCell for a String value.
     *
     * @return
     */
    public static <S> EditTableCell<S, String> createStringEditCell() {
        return new EditTableCell<>(new DefaultStringConverter());
    }


    // set the text of the text field and display the graphic
    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(converter.toString(getItem()));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
    }

    // revert to text display
    @Override
    public void cancelEdit() {
        log.debug("The cancelEdit() call has been altered to commitEdit() because we want to keep editing text after losing focus  . ");
        log.debug("Call cancelEditing() if really want to cancel and abandon the input.");
        super.commitEdit(converter.fromString(textField.getText()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    /**
     * Really to cancel editing.
     */
    public void cancelEditing() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    // commits the edit. Update property if possible and revert to text display
    @Override
    public void commitEdit(T item) {
        log.debug("commitEdit");
        // This block is necessary to support commit on losing focus, because the baked-in mechanism
        // sets our editing state to false before we can intercept the loss of focus.
        // The default commitEdit(...) method simply bails if we are not editing...
//        if (!isEditing() && !item.equals(getItem())) {
//            TableView<S> table = getTableView();
//            if (table != null) {
//                TableColumn<S, T> column = getTableColumn();
//                TableColumn.CellEditEvent<S, T> event = new TableColumn.CellEditEvent<>(table,
//                        new TablePosition<S, T>(table, getIndex(), column),
//                        TableColumn.editCommitEvent(), item);
//                Event.fireEvent(column, event);
//            }
//        }

        super.commitEdit(item);

        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
