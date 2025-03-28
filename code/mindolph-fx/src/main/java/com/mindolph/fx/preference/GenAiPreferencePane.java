package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.fx.dialog.CustomModelDialog;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.genai.GenaiUiConstants;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mindolph.base.constant.PrefConstants.*;
import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;
import static com.mindolph.genai.GenaiUiConstants.*;

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
    private CheckBox cbUseProxy;
    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguages;
    @FXML
    private Spinner<Integer> spTimeOut;

    // For Defining Agents
    @FXML
    private ChoiceBox<Pair<String, AgentMeta>> cbAgent;
    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbModelProvider;
    @FXML
    private ChoiceBox<Pair<String, ModelMeta>> cbChatModel;
    @FXML
    private Button btnAddAgent;
    @FXML
    private Button btnRemoveAgent;
    @FXML
    private TextArea taAgentPrompt;
    @FXML
    private Button btnModifyKB;
    @FXML
    private Label lblKnowledgeBase;
    @FXML
    private Button btnEmbedding;
    @FXML
    private ProgressBar pbWaiting;
    @FXML
    private Label lblEmbedding;

    private AgentMeta currentAgentMeta;
    private List<File> knowledgeFiles;

    private final AtomicBoolean isReady = new AtomicBoolean(false);
    // pause to saving data during loading an agent.
    private final AtomicBoolean isLoadingAgent = new AtomicBoolean(false);

    public GenAiPreferencePane() {
        super("/preference/gen_ai_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Gen AI
        cbAiProvider.setConverter(modelProviderConverter);
        cbAiProvider.getItems().add(new Pair<>(OPEN_AI, OPEN_AI.getName()));
        cbAiProvider.getItems().add(new Pair<>(GEMINI, GEMINI.getName()));
        cbAiProvider.getItems().add(new Pair<>(ALI_Q_WEN, ALI_Q_WEN.getName()));
        cbAiProvider.getItems().add(new Pair<>(OLLAMA, OLLAMA.getName()));
        cbAiProvider.getItems().add(new Pair<>(HUGGING_FACE, HUGGING_FACE.getName()));
        cbAiProvider.getItems().add(new Pair<>(CHAT_GLM, CHAT_GLM.getName()));
        cbAiProvider.getItems().add(new Pair<>(DEEP_SEEK, DEEP_SEEK.getName()));
        cbAiProvider.getItems().add(new Pair<>(MOONSHOT, MOONSHOT.getName()));
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

        cbModel.setConverter(modelMetaConverter);
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
        cbCustomModels.setConverter(modelMetaConverter);
        cbCustomModels.valueProperty().addListener((observable, oldValue, selectedModel) -> {
            if (selectedModel == null || selectedModel.getValue() == null) {
                btnRemove.setDisable(true);
                return;
            }
            btnRemove.setDisable(false);
            log.debug("on custom model selected: %s".formatted(selectedModel.getValue()));
            String activeProviderName = cbAiProvider.getValue().getKey().getName();
            LlmConfig.getIns().activateCustomModel(GenAiModelProvider.fromName(activeProviderName), selectedModel.getValue());
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
            boolean sure = DialogFactory.okCancelConfirmDialog("Are you to delete the custom model '%s'".formatted(name));
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
        cbLanguages.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesTo(cbLanguages);
        cbLanguages.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            fxPreferences.savePreference(GEN_AI_OUTPUT_LANGUAGE, newValue.getKey());
        });
        // time out setting for all.
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);

        // for Agents
        cbAgent.setConverter(agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            isLoadingAgent.set(true);
            AgentMeta agentMeta = newValue.getValue();
            if (agentMeta != null) {
                currentAgentMeta = agentMeta;
                if (agentMeta.getProvider() != null) {
                    cbModelProvider.getSelectionModel().select(new Pair<>(agentMeta.getProvider(), agentMeta.getProvider().getName()));
                }
                if (agentMeta.getChatModel() != null) {
                    cbChatModel.getSelectionModel().select(new Pair<>(agentMeta.getChatModel().name(), agentMeta.getChatModel()));
                }
                taAgentPrompt.setText(agentMeta.getPromptTemplate());
                knowledgeFiles = agentMeta.getFiles();
                lblKnowledgeBase.setText("Files under: %s".formatted(knowledgeFiles));
                if (knowledgeFiles != null && !knowledgeFiles.isEmpty()) {
                    btnEmbedding.setDisable(false);
                }
            }
            isLoadingAgent.set(false);
        });
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getName(), agentMeta)).toList());
        cbModelProvider.setConverter(modelProviderConverter);
        List<Pair<GenAiModelProvider, String>> providerPairs = EnumUtils.getEnumList(GenAiModelProvider.class).stream().map(p -> new Pair<>(p, p.getName())).toList();
        cbModelProvider.getItems().addAll(providerPairs);
        cbModelProvider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String activeProviderName = newValue.getKey().getName();
            ProviderProps providerProps = LlmConfig.getIns().loadGenAiProviderProps(activeProviderName);
            Collection<ModelMeta> modelMetas = PROVIDER_MODELS.get(activeProviderName);
            List<ModelMeta> customModels = providerProps.customModels();
            LlmConfig.getIns().preferredModelForActiveLlmProvider();
            cbChatModel.getItems().clear();
            cbChatModel.getItems().addAll(modelMetas.stream().map(mm -> new Pair<>(mm.name(), mm)).sorted(GenaiUiConstants.MODEL_COMPARATOR).toList());
            if (customModels != null) {
                cbChatModel.getItems().addAll(customModels.stream().map(mm -> new Pair<>(mm.name(), mm)).sorted(GenaiUiConstants.MODEL_COMPARATOR).toList());
            }
            onSaveAgent();
        });
        cbChatModel.setConverter(modelMetaConverter);
        cbChatModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            onSaveAgent();
        });
        btnAddAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddAgent.setOnAction(event -> {
            new TextInputDialog("My Agent").showAndWait().ifPresent(agentName -> {
                String agtId = IdUtils.makeUUID();
                AgentMeta agentMeta = onSaveAgent(agtId, agentName);
                currentAgentMeta = agentMeta;
                Pair<String, AgentMeta> newAgentPair = new Pair<>(agtId, agentMeta);
                cbAgent.getItems().add(newAgentPair);
                cbAgent.getSelectionModel().select(newAgentPair);
            });
        });
        btnRemoveAgent.setOnAction(event -> {
            if (DialogFactory.yesNoConfirmDialog("Removing Agent", "Are you sure you want to remove the agent %s?".formatted(currentAgentMeta.getName()))) {
                LlmConfig.getIns().removeAgent(currentAgentMeta.getId());
                cbAgent.getItems().remove(new Pair<>(currentAgentMeta.getName(), currentAgentMeta));
            }
        });
        taAgentPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
            onSaveAgent();
        });
        btnModifyKB.setOnAction(event -> {
            File file = DialogFactory.openDirDialog(this.getScene().getWindow(), SystemUtils.getUserHome());
            if (file != null && file.exists() && file.isDirectory()) {
                knowledgeFiles = Collections.singletonList(file);
                onSaveAgent();
                lblKnowledgeBase.setText("Files under: %s".formatted(file.getPath()));
                if (!knowledgeFiles.isEmpty()) {
                    btnEmbedding.setDisable(false);
                }
            }
        });
        btnEmbedding.setOnAction(event -> {
            if (currentAgentMeta == null) {
                DialogFactory.warnDialog("Please select an agent");
                return;
            }
            if (currentAgentMeta.getFiles() == null) {
                DialogFactory.warnDialog("Please select files to do embedding");
                return;
            }
            log.info("Start to embedding for files under: " + StringUtils.join(currentAgentMeta.getFiles(), ", "));
            Platform.runLater(() -> {
                pbWaiting.setVisible(true);
            });
            try {
                EmbeddingService.getInstance().embed(currentAgentMeta.getId(), currentAgentMeta.getFiles(), payload -> {
                    if (payload instanceof Exception e) {
                        Platform.runLater(() -> {
                            pbWaiting.setVisible(false);
                            lblEmbedding.setText("Embedding Failed: %s".formatted(payload.toString()));
                        });
                    }
                    Platform.runLater(() -> {
                        pbWaiting.setVisible(false);
                        lblEmbedding.setText("Embedding done");
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                DialogFactory.errDialog(e.getLocalizedMessage());
            } finally {
                pbWaiting.setVisible(false);
            }
        });
    }

    private AgentMeta onSaveAgent() {
        return onSaveAgent(currentAgentMeta.getId(), currentAgentMeta.getName());
    }

    private AgentMeta onSaveAgent(String id, String name) {
        if (isLoadingAgent.get()) {
            return null;
        }
        log.debug("On save agent {}: {}", id, name);
        AgentMeta agentMeta = new AgentMeta();
        agentMeta.setName(name);
        if (!cbModelProvider.getSelectionModel().isEmpty()) {
            agentMeta.setProvider(cbModelProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!cbChatModel.getSelectionModel().isEmpty()) {
            agentMeta.setChatModel(cbChatModel.getSelectionModel().getSelectedItem().getValue());
        }
        agentMeta.setPromptTemplate(taAgentPrompt.getText());
        agentMeta.setFiles(knowledgeFiles);
        LlmConfig.getIns().saveAgent(id, agentMeta);
        currentAgentMeta = agentMeta;
        return agentMeta;
    }

    private List<ModelMeta> showCustomModels(String providerName) {
        cbCustomModels.getItems().clear();
        ProviderProps providerProps = LlmConfig.getIns().loadGenAiProviderProps(providerName);
        List<ModelMeta> customModels = providerProps.customModels();
        if (customModels == null || customModels.isEmpty()) {
            log.info("no custom models found for provider: %s".formatted(providerName));
            this.updateModelDescription(null); // clear description when no custom models
            btnRemove.setDisable(true); // disable remove button here since the choice box of custom model will never be updated
        }
        else {
            List<Pair<String, ModelMeta>> metaPairs = customModels.stream().map(modelMeta -> new Pair<>(modelMeta.name(), modelMeta)).toList();
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
            PluginEventBus.getIns().emitPreferenceChanges();
    }
}
