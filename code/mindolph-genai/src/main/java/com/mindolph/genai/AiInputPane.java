package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.InputBuilder;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.PaneUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.core.constant.GenAiConstants.ActionType;

/**
 * An input panel for gen-ai.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiInputPane extends BaseAiPane {

    private static final Logger log = LoggerFactory.getLogger(AiInputPane.class);

    @FXML
    private HBox hbReady;
    @FXML
    private HBox hbGenerating;
    @FXML
    private Button btnStop;
    @FXML
    private Label lbModel;
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
    private ProgressBar pbWaiting;

    public AiInputPane(Object editorId, String fileType, String defaultInput) {
        super("/genai/ai_input_pane.fxml", editorId, fileType);

        taInput.setText(defaultInput);
        if (defaultInput != null && !defaultInput.isEmpty()) {
            taInput.positionCaret(defaultInput.length());
        }
        String activeProvider = LlmConfig.getIns().getActiveAiProvider();
        log.debug("Load models for gen-ai provider: %s".formatted(activeProvider));

        ModelMeta modelMeta = LlmConfig.getIns().preferredModelForActiveLlmProvider();
        taInput.setPromptText("The prompt to generate content by %s".formatted(activeProvider));
        if (modelMeta != null && StringUtils.isNotBlank(modelMeta.name())) {
            log.info("with default model: '%s'".formatted(modelMeta.name()));
        }

        lbTitle.setText("Generate content with %s".formatted(activeProvider));

//        lbModel.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
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
                Pair<String, ModelMeta> selectedModel = cbModel.getSelectionModel().getSelectedItem();
                if (selectedModel == null) {
                    DialogFactory.warnDialog("Please select a model to generate content.");
                    return;
                }
                lbMsg.setText(null);
                this.toggleComponents(true);
                String modelName = selectedModel.getValue().name();
                boolean isStreaming = !SupportFileTypes.TYPE_MIND_MAP.equals(fileType);// && !SupportFileTypes.TYPE_PLANTUML.equals(fileType);
                String prompt = taInput.getText().trim();
                log.debug(prompt);
                GenAiEvents.getIns().emitGenerateEvent(editorId,
                        new InputBuilder().model(modelName).text(prompt).temperature(cbTemperature.getValue().getKey())
                                .outputLanguage(cbLanguage.getValue().getKey())
                                .maxTokens(selectedModel.getValue().maxTokens()).outputAdjust(null)
                                .isRetry(false).isStreaming(isStreaming)
                                .createInput());
            }
            else {
                taInput.requestFocus();
            }
        });
        btnStop.setOnAction(event -> {
            this.toggleComponents(false);
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

        // for escaping
        PaneUtils.escapablePanes(() -> GenAiEvents.getIns().emitActionEvent(editorId, ActionType.CANCEL),
                this, hbReady, hbGenerating);
    }

    @Override
    protected void toggleComponents(boolean isGenerating) {
        super.toggleComponents(isGenerating);
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
        lbMsg.setText(reason);
        this.toggleComponents(false);
    }

    @Override
    public void requestFocus() {
        taInput.requestFocus();
    }

    public record Temperature(float value, String display) {
        // set 0.01 instead of 0.0 just because of hugging-face api require positive float value.
        public static final Temperature SAFE = new Temperature(0.01f, "Safe");
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
