package com.mindolph.mindmap.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

/**
 * @author mindolph.com@gmail.com
 */
public class AddImageChooseDialog extends BaseDialogController<Integer> {

    @FXML
    private RadioButton rbClipBoard;
    @FXML
    private RadioButton rbFile;

    public AddImageChooseDialog(String title, Integer defaultSelection) {
        result = 0; // select clipboard by default
        dialog = new CustomDialogBuilder<Integer>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title)
                .fxmlUri("dialog/add_image_choose_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .controller(AddImageChooseDialog.this)
                .build();
        ToggleGroup group = new ToggleGroup();
        rbClipBoard.setToggleGroup(group);
        rbFile.setToggleGroup(group);
        rbClipBoard.setSelected(defaultSelection == 0);
        rbFile.setSelected(defaultSelection == 1);
        rbClipBoard.selectedProperty().addListener((observableValue, b1, b2) -> result = b2 ? 0 : 1);
        rbFile.selectedProperty().addListener((observableValue, b1, b2) -> result = b2 ? 1 : 0);
    }
}
