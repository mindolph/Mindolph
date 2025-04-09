package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

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

    public BaseAiPane(String res, Object editorId, String fileType) {
        this.editorId = editorId;
        this.fileType = fileType;
        FxmlUtils.loadUri(res, this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));

        String activeProvider = LlmConfig.getIns().getActiveProviderMeta();
        Map<String, ProviderMeta> map = LlmConfig.getIns().loadAllProviderMetas();
        ProviderMeta providerMeta = map.get(activeProvider);
        Pair<String, ModelMeta> targetItem = null;
        List<Pair<String, ModelMeta>> allModels = new ArrayList<>();
        List<Pair<String, ModelMeta>> preModels = PROVIDER_MODELS.get(activeProvider)
                .stream().map(m -> new Pair<>(m.name(), m)).sorted(MODEL_COMPARATOR).toList();
        allModels.addAll(preModels);
        if (providerMeta.customModels() != null) {
            List<Pair<String, ModelMeta>> customModels = providerMeta.customModels().stream().map(modelMeta -> new Pair<>(modelMeta.name(), modelMeta)).toList();
            allModels.addAll(customModels);
        }
        if ("Custom".equals(providerMeta.aiModel())) {
            ModelMeta activeModel = null;
            if (providerMeta.customModels() != null) {
                activeModel = providerMeta.customModels().stream().filter(ModelMeta::active).findFirst().orElse(null);
            }
            if (activeModel != null) {
                targetItem = new Pair<>(activeModel.name(), activeModel);
            }
        }
        else {
            targetItem = new Pair<>(providerMeta.aiModel(), GenAiConstants.lookupModelMeta(activeProvider, providerMeta.aiModel()));
        }

        cbModel.getItems().clear();
        cbModel.getItems().addAll(allModels);
        if (targetItem != null && !allModels.contains(targetItem)) {
            cbModel.getItems().add(targetItem); // exclude same model
        }
        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            lbMsg.setText("Max output tokens: %s".formatted(displayGenAiTokens(newValue.getValue().maxTokens())));
        });
        if (cbModel.getItems().contains(targetItem)) {
            cbModel.getSelectionModel().select(targetItem);
        }
        cbModel.setConverter(modelMetaConverter);
        cbLanguage.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesTo(cbLanguage);
    }

    protected void toggleComponents(boolean isGenerating) {
        cbModel.setDisable(isGenerating);
        cbLanguage.setDisable(isGenerating);
    }
}
