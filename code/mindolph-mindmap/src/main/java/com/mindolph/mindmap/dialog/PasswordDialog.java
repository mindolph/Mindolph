package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.model.PasswordData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

/**
 * @author mindolph.com@gmail.com
 */
public class PasswordDialog extends BaseDialogController<PasswordData> {
    @FXML
    private PasswordField pfPassword;
    @FXML
    private Label lbMessage;

    public PasswordDialog(PasswordData origin) {
        dialog = new CustomDialogBuilder<PasswordData>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Set Password")
                .fxmlUri("dialog/password_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(origin)
                .controller(PasswordDialog.this)
                .build();
        lbMessage.setText(origin.getHint());
        result = origin;
        dialog.setOnShown(event -> {
            Platform.runLater(() -> pfPassword.requestFocus());
        });
        pfPassword.textProperty().addListener((observableValue, s, s2) -> {
            result.setPassword(s2);
        });
    }
}
