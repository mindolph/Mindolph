package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
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
                super.selectModel(currentAgentMeta.getProvider(), currentAgentMeta.getChatModel());
                cbLanguage.getSelectionModel().select(new Pair<>(currentAgentMeta.getLanguageCode(), currentAgentMeta.getLanguageCode()));
                taAgentPrompt.setText(agentMeta.getPromptTemplate());
                tvDatasets.getItems().clear();
                if (currentAgentMeta.getDatasetIds() != null) {
                    List<DatasetMeta> datasetMetas = LlmConfig.getIns().getDatasetsFromIds(currentAgentMeta.getDatasetIds());
                    if (datasetMetas != null && !datasetMetas.isEmpty()) {
                        tvDatasets.getItems().addAll(datasetMetas);
                    }
                }
            }
            isLoading.set(false);
        });
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getId(), agentMeta)).toList());
        super.initProvidersAndModels(1);
        btnAddAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveAgent.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddAgent.setOnAction(event -> {
            new TextInputDialog("My Agent").showAndWait().ifPresent(agentName -> {
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
        cbLanguage.setConverter(new PairStringStringConverter());
        cbLanguage.getItems().addAll(SUPPORTED_EMBEDDING_LANG.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList());
        cbLanguage.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
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
            if (datasetMetas != null && !datasetMetas.isEmpty()) {
                log.debug("Select datasets: {}", datasetMetas);
                tvDatasets.getItems().clear();
                tvDatasets.getItems().addAll(datasetMetas);
                saveCurrentAgent();
            }
        });

        // pre-select latest selected agent
        String latestAgentId = super.fxPreferences.getPreference(PrefConstants.GEN_AI_AGENT_LATEST, String.class);
        int selectIdx = cbAgent.getItems().stream().map(Pair::getKey).toList().indexOf(latestAgentId);
        log.debug("pre-select agent item %s at index %s".formatted(latestAgentId, selectIdx));
        cbAgent.getSelectionModel().select(selectIdx);
    }

    @Override
    protected void onModelChange() {
        this.saveCurrentAgent();
    }

    private AgentMeta saveCurrentAgent() {
        if (isLoading.get()) {
            return null;
        }
        AgentMeta am = currentAgentMeta;
        log.debug("On save agent {}: {}", am.getId(), am.getName());
        am.setDescription(tfDescription.getText());
        if (!cbModelProvider.getSelectionModel().isEmpty()) {
            am.setProvider(cbModelProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!cbModel.getSelectionModel().isEmpty()) {
            am.setChatModel(cbModel.getSelectionModel().getSelectedItem().getValue());
        }
        am.setPromptTemplate(taAgentPrompt.getText());
        am.setLanguageCode(cbLanguage.getSelectionModel().getSelectedItem().getValue());
        am.setDatasetIds(tvDatasets.getItems().stream().map(DatasetMeta::getId).toList());
        LlmConfig.getIns().saveAgent(am.getId(), am);
        super.onSave(true);
        return am;
    }

    private void clearAll() {
        cbAgent.getSelectionModel().clearSelection();
        tfDescription.setText("");
        taAgentPrompt.setText("");
        cbLanguage.getSelectionModel().clearSelection();
        selectModel(null, null);
        tvDatasets.getItems().clear();
        currentAgentMeta = null;
    }

}
