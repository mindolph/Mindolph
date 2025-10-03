package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.fx.control.DatasetTableView;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.mfx.control.MChoiceBox;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.genai.GenaiUiConstants.agentConverter;

/**
 * @since 1.13.0
 */
public class AiAgentPrefPane extends BaseAiPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AiAgentPrefPane.class);
    @FXML
    private ChoiceBox<Pair<String, AgentMeta>> cbAgent;
    @FXML
    private Button btnAddAgent;
    @FXML
    private Button btnRemoveAgent;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfDescription;
    @FXML
    private TextArea taAgentPrompt;
    @FXML
    private DatasetTableView tvDatasets;
    @FXML
    private Button btnSetDataset;
    @FXML
    private MChoiceBox<Pair<String, String>> cbLanguage;
    @FXML
    private MChoiceBox<Pair<GenAiModelProvider, String>> cbChatProvider;
    @FXML
    private MChoiceBox<Pair<String, ModelMeta>> cbChatModel;

    private AgentMeta currentAgentMeta;

    public AiAgentPrefPane() {
        super("/preference/gen_ai_agent_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbAgent.setConverter(agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            super.beforeLoading();
            AgentMeta agentMeta = newValue.getValue();
            log.debug("Agent changed: %s".formatted(agentMeta));
            if (agentMeta != null) {
                currentAgentMeta = agentMeta;
                super.fxPreferences.savePreference(SceneStatePrefs.GEN_AI_AGENT_LATEST, agentMeta.getId());
                tfName.setText(agentMeta.getName());
                tfDescription.setText(agentMeta.getDescription());
                ChoiceUtils.selectOrUnselectLanguage(cbLanguage, currentAgentMeta.getLanguageCode());
                taAgentPrompt.setText(agentMeta.getPromptTemplate());
                tvDatasets.getItems().clear();
                if (currentAgentMeta.getDatasetIds() != null) {
                    List<DatasetMeta> datasetMetas = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
                    tvDatasets.replaceAll(datasetMetas);
                }
                // embedding provider and model
                ChoiceUtils.selectOrUnselectProvider(this.cbEmbeddingProvider, currentAgentMeta.getEmbeddingProvider());
                ChoiceUtils.selectOrUnselectModel(this.cbEmbeddingModel, currentAgentMeta.getEmbeddingModel());
                // chat provider and model
                ChoiceUtils.selectOrUnselectProvider(this.cbChatProvider, currentAgentMeta.getChatProvider());
                ChoiceUtils.selectOrUnselectModel(this.cbChatModel, currentAgentMeta.getChatModel());
                this.enableAll();
            }
            else {
                this.disableAll();
            }
            log.debug("Loading completed.");
            super.afterLoading();
        });

        this.reloadAgents();

        super.initEmbeddingModelRelatedComponents(this.cbEmbeddingProvider, this.cbLanguage, this.cbEmbeddingModel);
        super.initChatModelRelatedComponents(this.cbChatProvider, this.cbChatModel);

        btnAddAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddAgent.setOnAction(event -> {
            this.createNewAgent("My Agent");
        });
        btnRemoveAgent.setOnAction(event -> {
            if (cbAgent == null) {
                return;
            }
            if (DialogFactory.yesNoConfirmDialog("Removing Agent", "Are you sure to remove the agent '%s'?".formatted(currentAgentMeta.getName()))) {
                beforeLoading();
                LlmConfig.getIns().removeAgent(currentAgentMeta.getId());
                cbAgent.getSelectionModel().clearSelection();
                if (cbAgent.getItems().remove(new Pair<>(currentAgentMeta.getId(), currentAgentMeta))) {
                    this.clearAll();
                    this.disableAll();
                }
                else {
                    log.warn("Failed to remove agent %s".formatted(currentAgentMeta.getName()));
                }
                afterLoading();
            }
        });
        tfName.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges();
        });
        tfDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges(false);
        });
        taAgentPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges(false);
        });
        tvDatasets.init();
        btnSetDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnSetDataset.setOnAction(event -> {
            List<DatasetMeta> selectedDatasets = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
            DatasetSelectDialog dialog = new DatasetSelectDialog(selectedDatasets, super.safeGetSelectedLanguageCode(cbLanguage));
            List<DatasetMeta> datasetMetas = dialog.showAndWait();
            if (tvDatasets.replaceAll(datasetMetas)) {
                super.saveChanges();
            }
        });

        // pre-select latest selected agent
        String latestAgentId = super.fxPreferences.getPreference(SceneStatePrefs.GEN_AI_AGENT_LATEST, String.class);
        int selectIdx = cbAgent.getItems().stream().map(Pair::getKey).toList().indexOf(latestAgentId);
        if (selectIdx != -1) {
            log.debug("pre-select agent item %s at index %s".formatted(latestAgentId, selectIdx));
            cbAgent.getSelectionModel().select(selectIdx);
        }
        else {
            this.disableAll();
        }

        // listen to the changes from dataset management
        PluginEventBus.getIns().subscribePreferenceChanges(pluginEvent -> {
            if (pluginEvent.getEventType() == PluginEvent.EventType.DATASET_PREF_CHANGED) {
                if (currentAgentMeta != null) {
                    List<DatasetMeta> datasetMetas = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
                    tvDatasets.replaceAll(datasetMetas);
                }
            }
        });
    }

    private void reloadAgents() {
        log.debug("reload agents for selection");
        int selectedIndex = cbAgent.getSelectionModel().getSelectedIndex();
        cbAgent.getItems().clear();
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getId(), agentMeta)).toList());
        cbAgent.getSelectionModel().select(selectedIndex);
    }

    private void createNewAgent(String defaultNewName) {
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Create new agent")
                .content("Input agent name")
                .text(defaultNewName)
                .width(400)
                .build();
        dialog.showAndWait().ifPresent(agentName -> {
            if (cbAgent.getItems().stream().anyMatch(p -> p.getValue().getName().equals(agentName))) {
                DialogFactory.warnDialog("Dataset names %s already exists".formatted(agentName));
                this.createNewAgent(agentName);
                return;
            }
            String agtId = IdUtils.makeUUID();
            currentAgentMeta = new AgentMeta();
            currentAgentMeta.setId(agtId);
            currentAgentMeta.setName(agentName);
            Pair<String, AgentMeta> newAgentPair = new Pair<>(agtId, currentAgentMeta);
            cbAgent.getItems().add(newAgentPair);
            cbAgent.getSelectionModel().select(newAgentPair);
            super.saveChanges(false);
        });
    }

    @Override
    protected void onSave(boolean notify) {
        log.debug("onSave");
        boolean isNameChanged = !currentAgentMeta.getName().equals(tfName.getText());
        this.saveCurrentAgent();
        if (isNameChanged) {
            this.reloadAgents();
        }
        super.onSave(notify);
    }

    private AgentMeta saveCurrentAgent() {
//        if (isLoading()) {
//            log.debug("Loading... ignore saving");
//            return null;
//        }
        AgentMeta am = currentAgentMeta;
        log.debug("On save agent {}: {}", am.getId(), am.getName());
        am.setName(tfName.getText());
        am.setDescription(tfDescription.getText());

        // chat model
        am.setChatProvider(cbChatProvider.hasSelected() ? cbChatProvider.getSelectionModel().getSelectedItem().getKey() : null);
        am.setChatModel(cbChatModel.hasSelected() ? cbChatModel.getSelectionModel().getSelectedItem().getKey() : null);
        if (!cbChatModel.hasSelected()) {
            // deselect provider if not select model
            am.setChatProvider(null);
        }
        am.setPromptTemplate(taAgentPrompt.getText());
        // embedding model
        am.setEmbeddingProvider(cbEmbeddingProvider.hasSelected() ? cbEmbeddingProvider.getSelectionModel().getSelectedItem().getKey() : null);
        am.setEmbeddingModel(cbEmbeddingModel.hasSelected() ? cbEmbeddingModel.getSelectionModel().getSelectedItem().getKey() : null);
        if (!cbEmbeddingModel.hasSelected()) {
            // deselect provider if not select model
            am.setEmbeddingProvider(null);
        }
        am.setLanguageCode(super.safeGetSelectedLanguageCode(cbLanguage));
        am.setDatasetIds(tvDatasets.getItems().stream().map(DatasetMeta::getId).toList());
        LlmConfig.getIns().saveAgent(am.getId(), am);
        return am;
    }

    private void disableAll() {
        NodeUtils.disable(tfName, tfDescription, cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, cbChatProvider, cbChatModel, btnSetDataset, tvDatasets, btnRemoveAgent, taAgentPrompt);
    }

    private void enableAll() {
        NodeUtils.enable(tfName, tfDescription, cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, cbChatProvider, cbChatModel, btnSetDataset, tvDatasets, btnRemoveAgent, taAgentPrompt);
    }

    private void clearAll() {
        tfName.clear();
        tfDescription.clear();
        taAgentPrompt.clear();
        cbLanguage.getSelectionModel().clearSelection();
        super.unselectEmbeddingProviderAndModel();
        tvDatasets.getItems().clear();
        currentAgentMeta = null;
    }

}
