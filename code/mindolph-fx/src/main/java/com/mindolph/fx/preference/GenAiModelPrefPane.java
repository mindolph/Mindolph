package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.BaseOrganizedPrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.fx.dialog.CustomModelDialog;
import com.mindolph.genai.GenaiUiConstants;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.lang3.EnumUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.SceneStatePrefs.GEN_AI_PROVIDER_ACTIVE;
import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;
import static com.mindolph.genai.GenaiUiConstants.*;

/**
 * @since unknown
 */
public class GenAiModelPrefPane extends BaseOrganizedPrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiModelPrefPane.class);

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbModelProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfBaseUrl;
    @FXML
    private ChoiceBox<Pair<String, ModelMeta>> cbModel;
    @FXML
    private ChoiceBox<Pair<String, ModelMeta>> cbCustomModels;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnRemove;
    @FXML
    private Label lbMaxOutputTokens;
    @FXML
    private CheckBox cbUseProxy;

    public GenAiModelPrefPane() {
        super("/preference/gen_ai_model_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.beforeLoading();
        // model providers
        cbModelProvider.setConverter(modelProviderConverter);
        List<Pair<GenAiModelProvider, String>> providerPairs = EnumUtils.getEnumList(GenAiModelProvider.class).stream().map(p -> new Pair<>(p, p.getDisplayName())).toList();
        cbModelProvider.getItems().addAll(providerPairs);
        super.bindPreference(cbModelProvider.valueProperty(), GEN_AI_PROVIDER_ACTIVE, OPEN_AI.name(),
                pair -> pair.getKey().name(),
                providerName -> new Pair<>(valueOf(providerName), providerName),
                selected -> {
                    super.beforeLoading();
                    Map<String, ProviderMeta> map = LlmConfig.getIns().loadAllProviderMetas();
                    GenAiModelProvider provider = selected.getKey();
                    if (provider != null) {
                        log.debug("Load models for gen-ai provider: %s".formatted(provider.name()));
                        if (provider.getType() == ProviderType.PUBLIC) {
                            tfApiKey.setDisable(false);
                            tfBaseUrl.setDisable(true);
                        }
                        else if (provider.getType() == ProviderType.PRIVATE) {
                            tfApiKey.setDisable(true);
                            tfBaseUrl.setDisable(false);
                        }
                        ProviderMeta providerMeta = map.get(provider.name());
                        if (providerMeta == null) {
                            // init for a vendor who had never been set up.
                            providerMeta = new ProviderMeta("", "", MODEL_CUSTOM_ITEM.getValue().getName(), false);
                        }
                        tfApiKey.setText(providerMeta.apiKey());
                        tfBaseUrl.setText(providerMeta.baseUrl());
                        cbUseProxy.setSelected(providerMeta.useProxy());

                        // Specific to disable the proxy support for OLLAMA since the LangChain4j is not supported it yet.
                        cbUseProxy.setDisable(provider == OLLAMA || provider == ALI_Q_WEN);

                        cbModel.getItems().clear();
                        // init all pre-defined models first
                        PROVIDER_MODELS.get(provider.name()).stream().map("  %s"::formatted).forEach(log::debug);
                        List<Pair<String, ModelMeta>> models = PROVIDER_MODELS.get(provider.name())
                                .stream().map(m -> new Pair<>(m.getName(), m)).sorted(GenaiUiConstants.MODEL_COMPARATOR).toList();
                        Pair<String, ModelMeta> targetItem = null;
                        if (!models.isEmpty()) {
                            cbModel.getItems().addAll(models);
                        }
                        cbModel.getItems().add(MODEL_CUSTOM_ITEM);
                        // select the preferred model
                        if ("Custom".equals(providerMeta.aiModel())) {
                            targetItem = MODEL_CUSTOM_ITEM;
                        }
                        else {
                            ModelMeta defaultModel = GenAiConstants.lookupModelMeta(provider.name(), providerMeta.aiModel());
                            if (defaultModel != null) {
                                targetItem = new Pair<>(providerMeta.aiModel(), defaultModel);
                            }
                        }
                        cbModel.getSelectionModel().select(targetItem);
                        fxPreferences.savePreference(SceneStatePrefs.GEN_AI_PROVIDER_LATEST, provider.name());
                    }
                    super.afterLoading();
                });

        // Dynamic preference can't use bindPreference.
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isLoading()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderMeta providerMeta = new ProviderMeta(newValue, null,
                    cbModel.getSelectionModel().getSelectedItem().getValue().getName(), cbUseProxy.isSelected(), customs);
            LlmConfig.getIns().saveProviderMeta(cbModelProvider.getValue().getKey(), providerMeta);
            this.onSave(true);
        });
        // Dynamic preference can't use bindPreference.
        tfBaseUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isLoading()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderMeta providerMeta = new ProviderMeta(null, newValue,
                    cbModel.getSelectionModel().getSelectedItem().getValue().getName(), cbUseProxy.isSelected(), customs);
            LlmConfig.getIns().saveProviderMeta(cbModelProvider.getValue().getKey(), providerMeta);
            this.onSave(true);
        });

        cbModel.setConverter(modelMetaConverter);
        cbModel.valueProperty().addListener((observable, oldValue, selectedModel) -> {
            if (selectedModel == null || selectedModel.getValue() == null) {
                log.info("No model selected");
                cbCustomModels.getItems().clear();
                return;
            }
            String providerName = cbModelProvider.getValue().getKey().name();
            ProviderMeta providerMeta = LlmConfig.getIns().loadAllProviderMetas().get(providerName);

            List<ModelMeta> customModels = List.of();
            if (providerMeta != null) {
                if (MODEL_CUSTOM_ITEM == selectedModel) {
                    // when 'Custom' model selected.
                    cbCustomModels.setDisable(false);
                    btnAdd.setDisable(false);
                    btnRemove.setDisable(false);
                    customModels = this.showCustomModels(providerName);
                }
                else {
                    // with pre-defined model selected.
                    cbCustomModels.setDisable(true);
                    btnAdd.setDisable(true);
                    btnRemove.setDisable(true);
                    customModels = providerMeta.customModels();
                    this.updateModelDescription(selectedModel.getValue());
                }
            }
            providerMeta = new ProviderMeta(tfApiKey.getText(), tfBaseUrl.getText(),
                    selectedModel.getValue().getName(), cbUseProxy.isSelected(), customModels);
            LlmConfig.getIns().saveProviderMeta(cbModelProvider.getValue().getKey(), providerMeta);
            this.onSave(true);
        });
        cbCustomModels.setConverter(modelMetaConverter);
        cbCustomModels.valueProperty().addListener((observable, oldValue, selectedModel) -> {
            if (selectedModel == null || selectedModel.getValue() == null) {
                btnRemove.setDisable(true);
                return;
            }
            btnRemove.setDisable(false);
            log.debug("on custom model selected: %s".formatted(selectedModel.getValue()));
//            String activeProviderName = cbModelProvider.getValue().getKey().name();
//            LlmConfig.getIns().activateCustomModel(GenAiModelProvider.valueOf(activeProviderName), selectedModel.getValue());
            this.updateModelDescription(selectedModel.getValue());
        });
        btnAdd.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemove.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAdd.setOnAction(event -> {
            CustomModelDialog dialog = new CustomModelDialog();
            ModelMeta newCustomModel = dialog.showAndWait();
            if (newCustomModel == null) return;
            String activeProviderName = cbModelProvider.getValue().getKey().name();
            // check existence before saving.
            ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(activeProviderName);
            log.debug("new custom model: %s".formatted(newCustomModel));
            if (providerMeta.customModels() == null) {
                providerMeta.setCustomModels(new ArrayList<>());
            }
            else {
                if (providerMeta.customModels().stream().anyMatch(mm -> mm.getName().equals(newCustomModel.getName()))) {
                    Platform.runLater(() -> {
                        Notifications.create().title("Notice").text("Model %s already exists".formatted(newCustomModel.getName())).showWarning();
                    });
                    return; // already exists
                }
            }

            providerMeta.customModels().add(newCustomModel);
            LlmConfig.getIns().saveProviderMeta(valueOf(activeProviderName), providerMeta);
//            LlmConfig.getIns().activateCustomModel(valueOf(activeProviderName), newCustomModel);
            this.showCustomModels(activeProviderName);
        });
        btnRemove.setOnAction(event -> {
            String name = cbCustomModels.getSelectionModel().getSelectedItem().getValue().getName();
            boolean sure = DialogFactory.okCancelConfirmDialog("Are you to delete the custom model '%s'".formatted(name));
            if (sure) {
                String activeProviderName = cbModelProvider.getValue().getKey().name();
                ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(activeProviderName);
                providerMeta.customModels().removeIf(mm -> mm.getName().equals(name));
                providerMeta.customModels().stream().findFirst().ifPresent(mm -> {
                    mm.setActive(true);
                });
                LlmConfig.getIns().saveProviderMeta(valueOf(activeProviderName), providerMeta);
                this.showCustomModels(activeProviderName);
            }
        });
        cbUseProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (isLoading()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderMeta providerMeta = new ProviderMeta(tfApiKey.getText(), tfBaseUrl.getText(),
                    cbModel.getSelectionModel().getSelectedItem().getValue().getName(), newValue, customs);
            LlmConfig.getIns().saveProviderMeta(cbModelProvider.getValue().getKey(), providerMeta);
            this.onSave(true);
        });

        // pre-select latest selected provider
        String latestProviderKey = super.fxPreferences.getPreferenceAlias(SceneStatePrefs.GEN_AI_PROVIDER_LATEST, GEN_AI_PROVIDER_ACTIVE, String.class);
        int selectIdx = cbModelProvider.getItems().stream().map(Pair::getKey).map(GenAiModelProvider::name).toList().indexOf(latestProviderKey);
        log.debug("pre-select provider item %s at index %s".formatted(latestProviderKey, selectIdx));
        cbModelProvider.getSelectionModel().select(selectIdx);
    }


    private List<ModelMeta> showCustomModels(String providerName) {
        cbCustomModels.getItems().clear();
        ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(providerName);
        List<ModelMeta> customModels = providerMeta.customModels();
        if (customModels == null || customModels.isEmpty()) {
            log.info("no custom models found for provider: %s".formatted(providerName));
            this.updateModelDescription(null); // clear description when no custom models
            btnRemove.setDisable(true); // disable the remove button here since the choice box of a custom model will never be updated
        }
        else {
            List<Pair<String, ModelMeta>> metaPairs = customModels.stream().map(modelMeta -> new Pair<>(modelMeta.getName(), modelMeta)).toList();
            cbCustomModels.getItems().addAll(metaPairs);
            Pair<String, ModelMeta> activePair = cbCustomModels.getItems().stream()
                    .filter(pair -> pair.getValue().active()).findFirst().orElse(null);
            if (activePair != null) {
                cbCustomModels.getSelectionModel().select(cbCustomModels.getItems().indexOf(activePair));
            }
            btnRemove.setDisable(false);
        }
        return customModels;
    }

    private void updateModelDescription(ModelMeta model) {
        if (model == null || model.maxTokens() <= 0) {
            lbMaxOutputTokens.setVisible(false);
        }
        else {
            lbMaxOutputTokens.setVisible(true);
            lbMaxOutputTokens.setText("Max output tokens: %s".formatted(displayGenAiTokens(model.maxTokens())));
        }
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.MODEL_PREF_CHANGED);
    }
}
