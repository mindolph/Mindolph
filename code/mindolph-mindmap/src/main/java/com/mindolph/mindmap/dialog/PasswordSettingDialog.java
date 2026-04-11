package com.mindolph.mindmap.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import org.swiftboot.util.I18nHelper;
import com.mindolph.mindmap.model.PasswordData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * @author mindolph.com@gmail.com
 */
public class PasswordSettingDialog extends BaseDialogController<PasswordData> {

    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfPassword2;
    @FXML
    private TextField tfHint;
    @FXML
    private CheckBox cbShowPassword;
    @FXML
    private Label lbMessage;

    public PasswordSettingDialog(PasswordData origin) {
        dialog = new CustomDialogBuilder<PasswordData>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(I18nHelper.getInstance().get("mindmap.password.set.title"))
                .fxmlUri("dialog/password_setting_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .defaultValue(origin)
                .controller(PasswordSettingDialog.this)
                .build();

        dialog.setOnShown(event -> {
            Platform.runLater(() -> pfPassword.requestFocus());
        });
        tfHint.textProperty().addListener(((observableValue, s, s2) -> result.setHint(s2)));

        pfPassword.textProperty().addListener((observableValue, s, t1) -> checkPasswords());

        pfPassword2.textProperty().addListener((observableValue, s, t1) -> checkPasswords());

    }

    private void checkPasswords() {
        if (!pfPassword.getText().equals(pfPassword2.getText())) {
            lbMessage.setText(I18nHelper.getInstance().get("mindmap.password.mismatch"));
        }
        else {
            lbMessage.setText(null);
            if (result == null) {
                result = new PasswordData();
            }
            result.setPassword(pfPassword.getText());
        }
    }

}
