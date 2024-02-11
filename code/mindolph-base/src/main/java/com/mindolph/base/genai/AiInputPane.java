package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import static com.mindolph.base.genai.llm.Constants.ActionType;
import static com.mindolph.base.genai.GenAiEvents.Input;

/**
 * An input panel for gen-ai.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiInputPane extends StackPane {

    @FXML
    private HBox hbReady;
    @FXML
    private HBox hbGenerating;
    @FXML
    private Button btnStop;
    @FXML
    private Label lbTemperature;
    @FXML
    private Label lbIcon;
    @FXML
    private TextArea taInput;
    @FXML
    private ChoiceBox<Pair<Float, Temperature>> cbTemperature;
    @FXML
    private Button btnGenerate;
    @FXML
    private Button btnClose;
    @FXML
    private ProgressBar pbWaiting;
    @FXML
    private Label lbMsg;

    private Object editorId;

    public AiInputPane(Object editorId, String defaultInput) {
        FxmlUtils.loadUri("/genai/ai_input_pane.fxml", this);

        taInput.setText(defaultInput);
        taInput.positionCaret(defaultInput.length());

        lbTemperature.setGraphic(FontIconManager.getIns().getIcon(IconKey.TEMPERATURE));
        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));

        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));
        btnGenerate.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));

        btnClose.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL);
        });
        taInput.setOnKeyReleased(event -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL);
            }
        });
        btnGenerate.setOnAction(event -> {
            if (StringUtils.isNotBlank(taInput.getText())) {
                lbMsg.setText(null);
                this.toggleButtons(true);
                GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(taInput.getText().trim(), cbTemperature.getValue().getKey(), null, false));
            }
            else {
                taInput.requestFocus();
            }
        });
        btnStop.setOnAction(event -> {
            this.toggleButtons(false);
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.STOP);
        });

        cbTemperature.getItems().addAll(
                new Pair<>(Temperature.SAFE.value, Temperature.SAFE),
                new Pair<>(Temperature.CREATIVE.value, Temperature.CREATIVE),
                new Pair<>(Temperature.ADVENTUROUS.value, Temperature.ADVENTUROUS),
                new Pair<>(Temperature.UNCHARTED.value, Temperature.UNCHARTED),
                new Pair<>(Temperature.CHAOS.value, Temperature.CHAOS)
        );
        cbTemperature.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<Float, Temperature> object) {
                return object == null ? "" : object.getValue().display();
            }

            @Override
            public Pair<Float, Temperature> fromString(String string) {
                return null;
            }
        });
        cbTemperature.setValue(new Pair<>(Temperature.SAFE.value, Temperature.SAFE));
    }

    private void toggleButtons(boolean isGenerating) {
        pbWaiting.setVisible(isGenerating);
        if (isGenerating)
            NodeUtils.disable(btnClose, btnGenerate, cbTemperature, taInput);
        else
            NodeUtils.enable(btnClose, btnGenerate, cbTemperature, taInput);
        hbReady.setVisible(!isGenerating);
        hbReady.setManaged(!isGenerating);
        hbGenerating.setVisible(isGenerating);
        hbGenerating.setManaged(isGenerating);
    }

    /**
     * Be called when the generation is stopped by some reason.
     */
    public void onStop(String reason) {
        pbWaiting.setVisible(false);
        lbMsg.setText(reason);
        NodeUtils.enable(btnClose, btnGenerate, cbTemperature, taInput);
    }

    @Override
    public void requestFocus() {
        taInput.requestFocus();
    }

    public record Temperature(float value, String display) {
        public static final Temperature SAFE = new Temperature(0.0f, "Safe");
        public static final Temperature CREATIVE = new Temperature(0.25f, "Creative");
        public static final Temperature ADVENTUROUS = new Temperature(0.5f, "Adventurous");
        public static final Temperature UNCHARTED = new Temperature(0.75f, "Uncharted");
        public static final Temperature CHAOS = new Temperature(1.0f, "Chaos");

        @Override
        public String toString() {
            return display;
        }
    }
}
