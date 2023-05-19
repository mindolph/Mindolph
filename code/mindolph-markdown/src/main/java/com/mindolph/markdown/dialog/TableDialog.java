package com.mindolph.markdown.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;


/**
 * Select rows and columns for a table
 *
 * @author mindolph.com@gmail.com
 */
public class TableDialog extends BaseDialogController<TableOptions> {

    @FXML
    private Spinner<Integer> spRows;
    @FXML
    private Spinner<Integer> spCols;


    public TableDialog(TableOptions initOptions) {
        dialog = new CustomDialogBuilder<TableOptions>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Create Table")
                .fxmlUri("dialog/table_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(null)
                .controller(this)
                .build();

        result = new TableOptions(3, 3);

        spRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 3));
        spCols.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 25, 3));

        // listeners
        spRows.valueProperty().addListener(((observableValue, oldv, newv) -> result.setRows(newv)));
        spCols.valueProperty().addListener(((observableValue, oldv, newv) -> result.setCols(newv)));
    }
}
