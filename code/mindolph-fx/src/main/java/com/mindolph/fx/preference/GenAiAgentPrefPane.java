package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.beans.property.SimpleStringProperty;
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

import static com.mindolph.genai.GenaiUiConstants.SUPPORTED_EMBEDDING_LANG;
import static com.mindolph.genai.GenaiUiConstants.agentConverter;

/**
 * @since unknown
 */
public class GenAiAgentPrefPane extends BaseGenAiPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiAgentPrefPane.class);
    @FXML
    private ChoiceBox<Pair<String, AgentMeta>> cbAgent;
    @FXML
    private Button btnAddAgent;
    @FXML
    private Button btnRemoveAgent;
    @FXML
    private TextField tfDescription;
    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguage;
    @FXML
    private TextArea taAgentPrompt;
    @FXML
    private TableView<DatasetMeta> tvDatasets;
    @FXML
    private Button btnSetDataset;

    @FXML
    protected ChoiceBox<Pair<GenAiModelProvider, String>> cbChatProvider;
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbChatModel;

    private AgentMeta currentAgentMeta;

    public GenAiAgentPrefPane() {
        super("/preference/gen_ai_agent_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbAgent.setConverter(agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            isLoading.set(true);
            AgentMeta agentMeta = newValue.getValue();
            if (agentMeta != null) {
                currentAgentMeta = agentMeta;
                super.fxPreferences.savePreference(PrefConstants.GEN_AI_AGENT_LATEST, agentMeta.getId());
                tfDescription.setText(agentMeta.getDescription());
                ChoiceUtils.selectOrUnselectLanguage(cbLanguage, currentAgentMeta.getLanguageCode());
                taAgentPrompt.setText(agentMeta.getPromptTemplate());
                tvDatasets.getItems().clear();
                if (currentAgentMeta.getDatasetIds() != null) {
                    List<DatasetMeta> datasetMetas = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
                    this.initDatasetsTableView(datasetMetas);
                }
                // embedding provider and model
                ChoiceUtils.selectOrUnselectProvider(this.cbEmbeddingProvider, currentAgentMeta.getEmbeddingProvider());
                ChoiceUtils.selectOrUnselectModel(this.cbEmbeddingModel, currentAgentMeta.getEmbeddingModel());
                // chat provider and model
                ChoiceUtils.selectOrUnselectProvider(this.cbChatProvider, currentAgentMeta.getChatProvider());
                ChoiceUtils.selectOrUnselectModel(this.cbChatModel, currentAgentMeta.getChatModel());
            }
            isLoading.set(false);
        });
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getId(), agentMeta)).toList());
        super.initEmbeddingModelComponents(this.cbEmbeddingProvider, this.cbLanguage, this.cbEmbeddingModel);
        super.initChatModelComponents(this.cbChatProvider, this.cbChatModel);

        btnAddAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddAgent.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("My Agent");
            dialog.setTitle("Create new agent");
            dialog.setContentText("Enter agent name:");
            dialog.showAndWait().ifPresent(agentName -> {
                String agtId = IdUtils.makeUUID();
                currentAgentMeta = new AgentMeta();
                currentAgentMeta.setId(agtId);
                currentAgentMeta.setName(agentName);
                Pair<String, AgentMeta> newAgentPair = new Pair<>(agtId, currentAgentMeta);
                cbAgent.getItems().add(newAgentPair);
                cbAgent.getSelectionModel().select(newAgentPair);
                this.saveCurrentAgent();
            });
        });
        btnRemoveAgent.setOnAction(event -> {
            if (cbAgent == null) {
                return;
            }
            if (DialogFactory.yesNoConfirmDialog("Removing Agent", "Are you sure you want to remove the agent '%s'?".formatted(currentAgentMeta.getName()))) {
                isLoading.set(true);
                LlmConfig.getIns().removeAgent(currentAgentMeta.getId());
                cbAgent.getItems().remove(new Pair<>(currentAgentMeta.getId(), currentAgentMeta));
                clearAll();
                isLoading.set(false);
            }
        });
        tfDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            saveCurrentAgent();
        });
        taAgentPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
            saveCurrentAgent();
        });
        TableColumn<DatasetMeta, String> colName = new TableColumn<>("Name");
        TableColumn<DatasetMeta, String> colFiles = new TableColumn<>("Files");
        TableColumn<DatasetMeta, String> colLang = new TableColumn<>("Language");
        TableColumn<DatasetMeta, String> colStatus = new TableColumn<>("Status");
        colName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        colFiles.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getFiles() == null ? 0 : param.getValue().getFiles().size())));
        colLang.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLanguageCode()));
        colStatus.setCellValueFactory(param -> new SimpleStringProperty("%d%%".formatted(param.getValue().getStatus())));
        tvDatasets.getColumns().addAll(List.of(colName, colFiles, colLang, colStatus));
        btnSetDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnSetDataset.setOnAction(event -> {
            List<DatasetMeta> selectedDatasets = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
            DatasetSelectDialog dialog = new DatasetSelectDialog(selectedDatasets);
            List<DatasetMeta> datasetMetas = dialog.showAndWait();
            if (this.initDatasetsTableView(datasetMetas)) {
                saveCurrentAgent();
            }
        });

        // pre-select latest selected agent
        String latestAgentId = super.fxPreferences.getPreference(PrefConstants.GEN_AI_AGENT_LATEST, String.class);
        int selectIdx = cbAgent.getItems().stream().map(Pair::getKey).toList().indexOf(latestAgentId);
        log.debug("pre-select agent item %s at index %s".formatted(latestAgentId, selectIdx));
        cbAgent.getSelectionModel().select(selectIdx);
    }

    private boolean initDatasetsTableView(List<DatasetMeta> datasetMetas) {
        if (datasetMetas != null && !datasetMetas.isEmpty()) {
            log.debug("Select datasets: {}", datasetMetas);
            // force to convert lang code to language.
            datasetMetas.forEach(datasetMeta -> {
                datasetMeta.setLanguageCode(SUPPORTED_EMBEDDING_LANG.get(datasetMeta.getLanguageCode()));
            });
            tvDatasets.getItems().clear();
            tvDatasets.getItems().addAll(datasetMetas);
            return true;
        }
        return false;
    }

    @Override
    protected void onSave(boolean notify) {
        this.saveCurrentAgent();
        super.onSave(notify);
    }

    private AgentMeta saveCurrentAgent() {
        if (isLoading.get()) {
            return null;
        }
        AgentMeta am = currentAgentMeta;
        log.debug("On save agent {}: {}", am.getId(), am.getName());
        am.setDescription(tfDescription.getText());
        if (!cbEmbeddingProvider.getSelectionModel().isEmpty()) {
            am.setEmbeddingProvider(cbEmbeddingProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!cbChatProvider.getSelectionModel().isEmpty()) {
            am.setChatProvider(cbChatProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!cbEmbeddingModel.getSelectionModel().isEmpty()) {
            am.setEmbeddingModel(cbEmbeddingModel.getSelectionModel().getSelectedItem().getValue());
        }
        if (!cbChatModel.getSelectionModel().isEmpty()) {
            am.setChatModel(cbChatModel.getSelectionModel().getSelectedItem().getValue());
        }
        am.setPromptTemplate(taAgentPrompt.getText());
        am.setLanguageCode(cbLanguage.getSelectionModel().getSelectedItem().getKey());
        am.setDatasetIds(tvDatasets.getItems().stream().map(DatasetMeta::getId).toList());
        LlmConfig.getIns().saveAgent(am.getId(), am);
        return am;
    }

    private void clearAll() {
        cbAgent.getSelectionModel().clearSelection();
        tfDescription.setText("");
        taAgentPrompt.setText("");
        cbLanguage.getSelectionModel().clearSelection();
        selectEmbeddingProviderAndModel(null, null);
        tvDatasets.getItems().clear();
        currentAgentMeta = null;
    }

}
