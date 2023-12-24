package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.AiInputDialog.Temperature;
import com.mindolph.base.genai.GenAiEvents.ActionType;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

/**
 * @author mindolph.com@gmail.com
 */
public class AiReframeDialog extends StackPane {

    @FXML
    private Button btnKeep;
    @FXML
    private Button btnRetry;
    @FXML
    private Button btnAdjust;
    @FXML
    private Button btnAbandon;
    @FXML
    private ProgressIndicator piProcessing;

    private Object editorId;

    private String inputText;

    public AiReframeDialog(Object editorId, String inputText) {
        this.editorId = editorId;
        this.inputText = inputText;
        FxmlUtils.loadUri("/genai/ai_reframe_dialog.fxml", this);

        btnKeep.setGraphic(FontIconManager.getIns().getIcon(IconKey.YES));
        btnRetry.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnAdjust.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnAbandon.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));

        btnKeep.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.KEEP);
            this.working();
        });
        btnRetry.setOnAction(event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new GenAiEvents.Input(inputText, Temperature.DEFAULT.value(), null));// todo
            this.working();
        });
        btnAdjust.setOnAction(event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new GenAiEvents.Input(inputText, Temperature.DEFAULT.value(), null));// todo
            this.working();
        });
        btnAbandon.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.ABANDON);
            this.working();
        });
    }

    private void working() {
        btnKeep.setDisable(true);
        btnRetry.setDisable(true);
        btnAdjust.setDisable(true);
        btnAbandon.setDisable(true);
        piProcessing.setDisable(false);
    }

}
