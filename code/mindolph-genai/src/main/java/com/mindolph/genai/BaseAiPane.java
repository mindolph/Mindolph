package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.AiConstants;
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
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.I18nHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.mindolph.core.constant.AiConstants.MODEL_TYPE_CHAT;
import static com.mindolph.core.constant.AiConstants.PROVIDER_MODELS;
import static com.mindolph.genai.AiUiConstants.MODEL_COMPARATOR;
import static com.mindolph.genai.AiUiConstants.modelMetaConverter;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;

/**
 * @since 1.11.1
 */
public abstract class BaseAiPane extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(BaseAiPane.class);
    @FXML
    protected ChoiceBox<Pair<String, ProviderMeta>> cbProvider;
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

    protected I18nHelper i18n = I18nHelper.getInstance();

    public BaseAiPane(String res, Object editorId, String fileType, String modelPrefKey) {
        this.editorId = editorId;
        this.fileType = fileType;
        FxmlUtils.loadUri(res, this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));

        Tuple2<String, ModelMeta> generateModel = GenAiUtils.parseModelPreference(modelPrefKey);

        if (generateModel == null) {
            log.warn("You haven't setup the default provider and model");
            providerName = PROVIDER_MODELS.keys().stream().findFirst().orElse(null);
            ModelMeta modelMeta = PROVIDER_MODELS.get(providerName).stream().findFirst().orElse(null);
            if (modelMeta != null) {
                modelName = modelMeta.getName();
            }
            else {
                throw new RuntimeException("No models are pre-defined for provider %s".formatted(providerName));
            }
        }
        else {
            providerName = generateModel.a();
            modelName = generateModel.b().getName();
        }

        log.debug("choose model %s from gen-ai provider %s".formatted(modelName, providerName));


        // load all providers and pre-select the preferred provider from user preferences.
        Map<String, ProviderMeta> providerKeyMetaMap = LlmConfig.getIns().loadAllProviderMetas();
        ChoiceUtils.initProviders(cbProvider, providerKeyMetaMap);
        cbProvider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // change models
                this.reloadModels(newValue.getKey());
            }
        });
        ChoiceUtils.selectProvider(cbProvider, providerName);

        this.reloadModels(providerName);

        ModelMeta targetModel = AiConstants.lookupModelMeta(providerName, modelName);
        if (targetModel == null) {
            Collection<ModelMeta> filteredCustomModels = LlmConfig.getIns().getFilteredCustomModels(providerName, MODEL_TYPE_CHAT);
            targetModel = filteredCustomModels.stream().filter(modelMeta -> modelMeta.getName().equals(modelName))
                    .findFirst().orElse(null);
        }

        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lbMsg.setText(i18n.get("ai.max.output.token", displayGenAiTokens(newValue.getValue().maxTokens())));
            }
        });
        if (targetModel != null) {
            ChoiceUtils.selectModel(cbModel, targetModel.getName());
        }
        cbModel.setConverter(modelMetaConverter);
        cbLanguage.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesToAndSelectDefault(cbLanguage);
    }

    /**
     * Reload models when loading this panel or changing provider by user.
     *
     * @param providerName
     */
    protected void reloadModels(String providerName) {
        // collect pre-defined models for the provider.
        List<Pair<String, ModelMeta>> preModelPairs = AiConstants.getFilteredPreDefinedModels(providerName, MODEL_TYPE_CHAT)
                .stream().map(m -> new Pair<>(m.getName(), m)).sorted(MODEL_COMPARATOR).toList();
        List<Pair<String, ModelMeta>> allModelPairs = new ArrayList<>(preModelPairs);

        // collect custom models(if exists)
        Collection<ModelMeta> filteredCustomModels = LlmConfig.getIns().getFilteredCustomModels(providerName, MODEL_TYPE_CHAT);
        if (CollectionUtils.isNotEmpty(filteredCustomModels)) {
            allModelPairs.addAll(filteredCustomModels.stream().map(modelMeta -> new Pair<>(modelMeta.getName(), modelMeta)).toList());
        }

        cbModel.getItems().clear();
        cbModel.getItems().addAll(allModelPairs);
    }

    protected void toggleComponents(boolean isGenerating) {
        cbProvider.setDisable(isGenerating);
        cbModel.setDisable(isGenerating);
        cbLanguage.setDisable(isGenerating);
    }
}
