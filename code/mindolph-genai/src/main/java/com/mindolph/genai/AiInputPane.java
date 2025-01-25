package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.mindolph.base.genai.GenAiEvents.Input;
import static com.mindolph.core.constant.GenAiConstants.*;
import static com.mindolph.genai.GenaiUiConstants.MODEL_COMPARATOR;

/**
 * An input panel for gen-ai.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiInputPane extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(AiInputPane.class);

    @FXML
    private HBox hbReady;
    @FXML
    private HBox hbGenerating;
    @FXML
    private Button btnStop;
    @FXML
    private Label lbTitle;
    @FXML
    private Label lbModel;
    @FXML
    private Label lbTemperature;
    @FXML
    private Label lbIcon;
    @FXML
    private TextArea taInput;
    @FXML
    private ChoiceBox<Pair<String, ModelMeta>> cbModel;
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

    private final Object editorId;
    private final String fileType;

    public AiInputPane(Object editorId, String fileType, String defaultInput) {
        this.editorId = editorId;
        this.fileType = fileType;

        FxmlUtils.loadUri("/genai/ai_input_pane.fxml", this);

        taInput.setText(defaultInput);
        taInput.positionCaret(defaultInput.length());
        String activeProvider = LlmConfig.getIns().getActiveAiProvider();
        log.debug("Load models for gen-ai provider: %s".formatted(activeProvider));

        ModelMeta modelMeta = LlmConfig.getIns().preferredModelForActiveLlmProvider();
        taInput.setPromptText("The prompt to generate content by %s".formatted(activeProvider));
        if (modelMeta != null && StringUtils.isNotBlank(modelMeta.name())) {
            log.info("with default model: '%s'".formatted(modelMeta.name()));
        }

        lbTitle.setText("Generate content with %s".formatted(activeProvider));

        Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
        ProviderProps providerProps = map.get(activeProvider);
        Pair<String, ModelMeta> targetItem;
        if ("Custom".equals(providerProps.aiModel())) {
            ModelMeta customModelMeta = providerProps.customModels().stream().filter(ModelMeta::active).findFirst().orElse(null);
            targetItem = new Pair<>(customModelMeta.name(), customModelMeta);
        }
        else {
            targetItem = new Pair<>(providerProps.aiModel(), GenAiConstants.lookupModelMeta(activeProvider, providerProps.aiModel()));
        }
        List<Pair<String, ModelMeta>> models = PROVIDER_MODELS.get(activeProvider)
                .stream().map(m -> new Pair<>(m.name(), m)).sorted(MODEL_COMPARATOR).toList();
        cbModel.getItems().clear();
        cbModel.getItems().addAll(models);
        if (!models.contains(targetItem)) {
            cbModel.getItems().add(targetItem); // exclude same model
        }

        if (cbModel.getItems().contains(targetItem)) {
            cbModel.getSelectionModel().select(targetItem);
        }
        cbModel.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, ModelMeta> object) {
                return object == null ? "" : object.getValue().name();
            }

            @Override
            public Pair<String, ModelMeta> fromString(String string) {
                return null;
            }
        });

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
                lbMsg.setText(null);
                this.toggleButtons(true);
                Pair<String, ModelMeta> selectedModel = cbModel.getSelectionModel().getSelectedItem();
                String model = null;
                if (selectedModel != null) {
                    model = selectedModel.getValue().name();
                }
                boolean isStreaming = !SupportFileTypes.TYPE_MIND_MAP.equals(fileType);// && !SupportFileTypes.TYPE_PLANTUML.equals(fileType);
                String prompt = taInput.getText().trim();
                log.debug(prompt);
                GenAiEvents.getIns().emitGenerateEvent(editorId,
                        new Input(model, prompt, cbTemperature.getValue().getKey(),
                                selectedModel.getValue().maxTokens(), null, false, isStreaming));
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
        lbMsg.setText(reason);
        toggleButtons(false);
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
