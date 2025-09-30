package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.DesktopUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

/**
 * Dialog to input URL.
 *
 * @author mindolph.com@gmail.com
 */
public class UrlDialog extends BaseDialogController<String> {

    @FXML
    private TextField textField;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnBrowse;

    public UrlDialog(String title, String url) {
        super(url);
        dialog = new CustomDialogBuilder<String>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title, 20)
                .fxmlUri("dialog/url_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(url)
                .resizable(true)
                .controller(UrlDialog.this)
                .build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> textField.requestFocus());
        });
        dialog.setOnCloseRequest(dialogEvent -> {
            if (!confirmClosing("URI has been changed, are you sure to close the dialog")) {
                dialogEvent.consume();
            }
        });
        btnClear.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLEAR));
        btnBrowse.setGraphic(FontIconManager.getIns().getIcon(IconKey.BROWSE));
        textField.setText(url);
        textField.textProperty().addListener((observable, oldValue, newValue) -> result = newValue);
    }

    @FXML
    public void onClear(ActionEvent event) {
        textField.clear();
    }

    @FXML
    public void onOpenInBrowser(ActionEvent event) {
        if (StringUtils.isNotBlank(textField.getText())) {
            DesktopUtils.openURL(textField.getText());
        }
    }
}
