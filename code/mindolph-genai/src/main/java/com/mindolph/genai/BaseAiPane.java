package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;
import static com.mindolph.genai.GenaiUiConstants.MODEL_COMPARATOR;
import static com.mindolph.genai.GenaiUiConstants.modelMetaConverter;

/**
 * @since 1.11.1
 */
public abstract class BaseAiPane extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(BaseAiPane.class);
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbModel;
    @FXML
    protected ChoiceBox<Pair<String, String>> cbLanguage;
    @FXML
    protected Button btnClose;
    @FXML
    protected Label lbIcon;
    @FXML
    protected Label lbTitle;
    @FXML
    protected Label lbMsg;

    protected final Object editorId;
    protected final String fileType;
    protected String providerName;
    protected String modelName;

    public BaseAiPane(String res, Object editorId, String fileType, String modelPrefKey) {
        this.editorId = editorId;
        this.fileType = fileType;
        FxmlUtils.loadUri(res, this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));

        Tuple2<GenAiModelProvider, ModelMeta> generateModel = GenAiUtils.parseModelPreference(modelPrefKey);

        if (generateModel == null) {
            log.warn("You haven't setup the default provider and model");
            providerName = PROVIDER_MODELS.keys().stream().findFirst().orElse(null);
            ModelMeta modelMeta = PROVIDER_MODELS.get(providerName).stream().findFirst().orElse(null);
            if (modelMeta != null) {
                modelName = modelMeta.getName();
            }
            else {
                throw new RuntimeException("No models pre-defined for provider %s".formatted(providerName));
            }
        }
        else {
            providerName = generateModel.a().getName();
            modelName = generateModel.b().getName();
        }

        log.debug("choose model %s from gen-ai provider %s".formatted(modelName, providerName));

        // collect pre-defined models for the provider.
        List<Pair<String, ModelMeta>> preModelPairs = PROVIDER_MODELS.get(providerName)
                .stream().map(m -> new Pair<>(m.getName(), m)).sorted(MODEL_COMPARATOR).toList();
        List<Pair<String, ModelMeta>> allModelPairs = new ArrayList<>(preModelPairs);

        // collect custom models(if exists)
        Map<String, ProviderMeta> map = LlmConfig.getIns().loadAllProviderMetas();
        ProviderMeta providerMeta = map.get(providerName);
        if (providerMeta.customModels() != null) {
            List<Pair<String, ModelMeta>> customModelPairs = providerMeta.customModels().stream().map(modelMeta -> new Pair<>(modelMeta.getName(), modelMeta)).toList();
            allModelPairs.addAll(customModelPairs);
        }
        cbModel.getItems().clear();
        cbModel.getItems().addAll(allModelPairs);

        ModelMeta targetModel = GenAiConstants.lookupModelMeta(providerName, modelName);
        if (targetModel == null) {
            targetModel = providerMeta.customModels().stream().filter(modelMeta -> modelMeta.getName().equals(modelName))
                    .findFirst().orElse(null);
        }

        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lbMsg.setText("Max output tokens: %s".formatted(displayGenAiTokens(newValue.getValue().maxTokens())));
            }
        });
        ChoiceUtils.selectModel(cbModel, targetModel);
        cbModel.setConverter(modelMetaConverter);
        cbLanguage.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesToAndSelectDefault(cbLanguage);
    }

    protected void toggleComponents(boolean isGenerating) {
        cbModel.setDisable(isGenerating);
        cbLanguage.setDisable(isGenerating);
    }
}
