package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.fx.dialog.CustomModelDialog;
import com.mindolph.genai.GenaiUiConstants;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDER_ACTIVE;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;
import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.genai.GenaiUiConstants.MODEL_CUSTOM_ITEM;

/**
 * @author mindolph.com@gmail.com
 * @see GenAiConstants
 * @see GenAiModelProvider
 * @since 1.7.1
 */
public class GenAiPreferencePane extends BasePrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiPreferencePane.class);

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbAiProvider;
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
    private Spinner<Integer> spTimeOut;
    @FXML
    private CheckBox cbUseProxy;

    private final AtomicBoolean isReady = new AtomicBoolean(false);

    public GenAiPreferencePane() {
        super("/preference/gen_ai_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Gen AI
        cbAiProvider.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<GenAiModelProvider, String> object) {
                return object == null ? "" : object.getValue();
            }

            @Override
            public Pair<GenAiModelProvider, String> fromString(String string) {
                return null;
            }
        });
        cbAiProvider.getItems().add(new Pair<>(OPEN_AI, OPEN_AI.getName()));
        cbAiProvider.getItems().add(new Pair<>(GEMINI, GEMINI.getName()));
        cbAiProvider.getItems().add(new Pair<>(ALI_Q_WEN, ALI_Q_WEN.getName()));
        cbAiProvider.getItems().add(new Pair<>(OLLAMA, OLLAMA.getName()));
        cbAiProvider.getItems().add(new Pair<>(HUGGING_FACE, HUGGING_FACE.getName()));
        cbAiProvider.getItems().add(new Pair<>(CHAT_GLM, CHAT_GLM.getName()));
        cbAiProvider.getItems().add(new Pair<>(DEEP_SEEK, DEEP_SEEK.getName()));
        super.bindPreference(cbAiProvider.valueProperty(), GEN_AI_PROVIDER_ACTIVE, OPEN_AI.getName(),
                pair -> pair.getKey().getName(),
                providerName -> new Pair<>(fromName(providerName), providerName),
                selected -> {
                    isReady.set(false);
                    Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
                    GenAiModelProvider provider = selected.getKey();
                    if (provider != null) {
                        log.debug("Load models for gen-ai provider: %s".formatted(provider.getName()));
                        if (provider.getType() == ProviderType.PUBLIC) {
                            tfApiKey.setDisable(false);
                            tfBaseUrl.setDisable(true);
                        }
                        else if (provider.getType() == ProviderType.PRIVATE) {
                            tfApiKey.setDisable(true);
                            tfBaseUrl.setDisable(false);
                        }
                        ProviderProps providerProps = map.get(provider.getName());
                        if (providerProps == null) {
                            // init for a vendor who was never been setup.
                            providerProps = new ProviderProps("", "", MODEL_CUSTOM_ITEM.getValue().name(), false);
                        }
                        tfApiKey.setText(providerProps.apiKey());
                        tfBaseUrl.setText(providerProps.baseUrl());
                        cbUseProxy.setSelected(providerProps.useProxy());

                        // Specific disable the proxy support for OLLAMA since the LangChain4j is not supported it yet.
                        cbUseProxy.setDisable(provider == OLLAMA || provider == ALI_Q_WEN);

                        cbModel.getItems().clear();
                        // init all pre-defined models first
                        PROVIDER_MODELS.get(provider.getName()).stream().map("  %s"::formatted).forEach(log::debug);
                        List<Pair<String, ModelMeta>> models = PROVIDER_MODELS.get(provider.getName())
                                .stream().map(m -> new Pair<>(m.name(), m)).sorted(GenaiUiConstants.MODEL_COMPARATOR).toList();
                        Pair<String, ModelMeta> targetItem = null;
                        if (!models.isEmpty()) {
                            cbModel.getItems().addAll(models);
                        }
                        cbModel.getItems().add(MODEL_CUSTOM_ITEM);
                        // select prefer model
                        if ("Custom".equals(providerProps.aiModel())) {
                            targetItem = MODEL_CUSTOM_ITEM;
                        }
                        else {
                            ModelMeta defaultModel = GenAiConstants.lookupModelMeta(provider.getName(), providerProps.aiModel());
                            if (defaultModel != null) {
                                targetItem = new Pair<>(providerProps.aiModel(), defaultModel);
                            }
                        }
                        cbModel.getSelectionModel().select(targetItem);
                        isReady.set(true);
                    }
                });

        // Dynamic preference can't use bindPreference.
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isReady.get()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderProps vendorProps = new ProviderProps(newValue, null,
                    cbModel.getSelectionModel().getSelectedItem().getValue().name(), cbUseProxy.isSelected(), customs);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        // Dynamic preference can't use bindPreference.
        tfBaseUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isReady.get()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderProps vendorProps = new ProviderProps(null, newValue,
                    cbModel.getSelectionModel().getSelectedItem().getValue().name(), cbUseProxy.isSelected(), customs);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        StringConverter<Pair<String, ModelMeta>> modelConverter = new StringConverter<>() {
            @Override
            public String toString(Pair<String, ModelMeta> object) {
                return object == null ? "" : object.getValue().name();
            }

            @Override
            public Pair<String, ModelMeta> fromString(String string) {
                return null;
            }
        };
        cbModel.setConverter(modelConverter);
        cbModel.valueProperty().addListener((observable, oldValue, selectedModel) -> {
            if (selectedModel == null || selectedModel.getValue() == null) {
                log.info("No model selected");
                cbCustomModels.getItems().clear();
                return;
            }
            String providerName = cbAiProvider.getValue().getKey().getName();
            ProviderProps providerProps = LlmConfig.getIns().loadGenAiProviders().get(providerName);

            List<ModelMeta> customModels = List.of();
            if (providerProps != null) {
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
                    customModels = providerProps.customModels();
                    this.updateModelDescription(selectedModel.getValue());
                }
            }
            providerProps = new ProviderProps(tfApiKey.getText(), tfBaseUrl.getText(),
                    selectedModel.getValue().name(), cbUseProxy.isSelected(), customModels);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), providerProps);
            this.onSave(true);
        });
        cbCustomModels.setConverter(modelConverter);
        cbCustomModels.valueProperty().addListener((observable, oldValue, selectedModel) -> {
            if (selectedModel == null || selectedModel.getValue() == null) {
                return;
            }
            log.debug("on custom model selected: %s".formatted(selectedModel.getValue()));
            this.updateModelDescription(selectedModel.getValue());
        });
        btnAdd.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemove.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAdd.setOnAction(event -> {
            CustomModelDialog dialog = new CustomModelDialog();
            ModelMeta newCustomModel = dialog.showAndWait();
            if (newCustomModel == null) return;
            String activeProviderName = cbAiProvider.getValue().getKey().getName();
            // check existence before saving.
            ProviderProps props = LlmConfig.getIns().loadGenAiProviderProps(activeProviderName);
            log.debug("new custom model: %s".formatted(newCustomModel));
            if (props.customModels() == null) {
                props.setCustomModels(new ArrayList<>());
            }
            else {
                if (props.customModels().stream().anyMatch(mm -> mm.name().equals(newCustomModel.name()))) {
                    Platform.runLater(() -> {
                        Notifications.create().title("Notice").text("Model %s already exists".formatted(newCustomModel.name())).showWarning();
                    });
                    return; // already exists
                }
            }

            props.customModels().add(newCustomModel);
            LlmConfig.getIns().saveGenAiProvider(fromName(activeProviderName), props);
            LlmConfig.getIns().activateCustomModel(fromName(activeProviderName), newCustomModel);
            this.showCustomModels(activeProviderName);
        });
        btnRemove.setOnAction(event -> {
            String name = cbCustomModels.getSelectionModel().getSelectedItem().getValue().name();
            boolean sure = DialogFactory.okCancelConfirmDialog("Are you to delete model %s".formatted(name));
            if (sure) {
                String activeProviderName = cbAiProvider.getValue().getKey().getName();
                ProviderProps props = LlmConfig.getIns().loadGenAiProviderProps(activeProviderName);
                props.customModels().removeIf(mm -> mm.name().equals(name));
                props.customModels().stream().findFirst().ifPresent(mm -> {
                    mm.setActive(true);
                });
                LlmConfig.getIns().saveGenAiProvider(fromName(activeProviderName), props);
                this.showCustomModels(activeProviderName);
            }
        });
        cbUseProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!isReady.get()) return;
            List<ModelMeta> customs = cbCustomModels.getItems().stream().map(Pair::getValue).toList();
            ProviderProps vendorProps = new ProviderProps(tfApiKey.getText(), tfBaseUrl.getText(),
                    cbModel.getSelectionModel().getSelectedItem().getValue().name(), newValue, customs);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        // time out setting for all.
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);
    }


    private List<ModelMeta> showCustomModels(String providerName) {
        cbCustomModels.getItems().clear();
        ProviderProps providerProps = LlmConfig.getIns().loadGenAiProviderProps(providerName);
        List<ModelMeta> customModels = providerProps.customModels();
        if (customModels == null || customModels.isEmpty()) {
            log.info("no custom models found for provider: %s".formatted(providerName));
            this.updateModelDescription(null); // clear description when no custom models
        }
        else {
            List<Pair<String, ModelMeta>> metaPairs = customModels.stream().map(modelMeta -> new Pair<>(modelMeta.name(), modelMeta)).toList();
            cbCustomModels.getItems().addAll(metaPairs);
            Pair<String, ModelMeta> activePair = cbCustomModels.getItems().stream()
                    .filter(pair -> pair.getValue().active()).findFirst().orElse(null);
            if (activePair != null) {
                cbCustomModels.getSelectionModel().select(cbCustomModels.getItems().indexOf(activePair));
            }
        }
        return customModels;
    }

    private void updateModelDescription(ModelMeta model) {
        if (model == null || model.maxTokens() <= 0) {
            lbMaxOutputTokens.setVisible(false);
        }
        else {
            lbMaxOutputTokens.setVisible(true);
            lbMaxOutputTokens.setText("Max output tokens: %d".formatted(model.maxTokens()));
        }
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges();
    }
}
