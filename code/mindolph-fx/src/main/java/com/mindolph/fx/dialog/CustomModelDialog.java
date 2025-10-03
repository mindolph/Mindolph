package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ModelMetaBuilder;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * @author mindolph.com@gmail.com
 * @since 1.11
 */
public class CustomModelDialog extends BaseDialogController<ModelMeta> {
    @FXML
    private TextField tfModel;
    @FXML
    private Spinner<Integer> spMaxOutputTokens;

    public CustomModelDialog(ModelMeta defaultCustomModel) {
        dialog = new CustomDialogBuilder<ModelMeta>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Custom Model")
                .fxmlUri("dialog/custom_model_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(defaultCustomModel)
                .controller(CustomModelDialog.this)
                .build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> tfModel.requestFocus());
        });
        tfModel.setText(defaultCustomModel.getName());
        spMaxOutputTokens.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, Integer.MAX_VALUE, defaultCustomModel.getMaxTokens(), 1024));
        tfModel.textProperty().addListener((observable, oldValue, newValue) -> {
            result = new ModelMetaBuilder().name(tfModel.getText()).maxTokens(spMaxOutputTokens.getValue()).build();
        });
        spMaxOutputTokens.valueProperty().addListener((observable, oldValue, newValue) -> {
            result = new ModelMetaBuilder().name(tfModel.getText()).maxTokens(spMaxOutputTokens.getValue()).build();
        });
    }
}
