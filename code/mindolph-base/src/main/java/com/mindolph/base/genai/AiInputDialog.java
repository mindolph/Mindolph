package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.tools.Borders;

import static com.mindolph.base.genai.GenAiEvents.ActionType;
import static com.mindolph.base.genai.GenAiEvents.Input;

/**
 * @author mindolph.com@gmail.com
 */
public class AiInputDialog extends StackPane {

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


    private Object editorId;

    public AiInputDialog(Object editorId) {
        FxmlUtils.loadUri("/genai/ai_input_dialog.fxml", this);
//        Borders.wrap(this).lineBorder().color(Color.BLACK).build().build();

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
            pbWaiting.setVisible(true);
            GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(taInput.getText(), cbTemperature.getValue().getKey(), null));
        });

        cbTemperature.getItems().addAll(
                new Pair<>(Temperature.MIN.value, Temperature.MIN),
                new Pair<>(Temperature.DEFAULT.value, Temperature.DEFAULT),
                new Pair<>(Temperature.MAX.value, Temperature.MAX)
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
        cbTemperature.setValue(new Pair<>(Temperature.DEFAULT.value, Temperature.DEFAULT));
    }

    @Override
    public void requestFocus() {
        taInput.requestFocus();
    }

    public record Temperature(float value, String display) {
        public static final Temperature DEFAULT = new Temperature(0.5f, "DEFAULT");
        public static final Temperature MIN = new Temperature(0.0f, "MIN");
        public static final Temperature MAX = new Temperature(1.0f, "MAX");

        @Override
        public String toString() {
            return display;
        }
    }
}
