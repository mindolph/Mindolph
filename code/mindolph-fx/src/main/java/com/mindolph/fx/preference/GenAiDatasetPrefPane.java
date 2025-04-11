package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.control.WorkspaceSelector;
import com.mindolph.fx.view.FileSelectView;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;
import static com.mindolph.genai.GenaiUiConstants.SUPPORTED_EMBEDDING_LANG;
import static com.mindolph.genai.GenaiUiConstants.datasetConverter;

/**
 * @since unknown
 */
public class GenAiDatasetPrefPane extends BaseGenAiPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiDatasetPrefPane.class);

    @FXML
    private ChoiceBox<Pair<String, DatasetMeta>> cbDataset;
    @FXML
    private Button btnAddDataset;
    @FXML
    private Button btnRemoveDataset;
    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguage;
    @FXML
    private WorkspaceSelector workspaceSelector;
    @FXML
    private FileSelectView fileSelectView;
    @FXML
    private Label lblKnowledgeBase;
    @FXML
    private Button btnEmbedding;
    @FXML
    private ProgressBar pbProgress;
    @FXML
    private Label lblEmbeddingStatus;
    @FXML
    private Label lblEmbeddingProgress;

    private DatasetMeta currentDatasetMeta;

    public GenAiDatasetPrefPane() {
        super("/preference/gen_ai_dataset_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbDataset.setConverter(datasetConverter);
        cbDataset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            isLoading.set(true);
            DatasetMeta datasetMeta = newValue.getValue();
            if (datasetMeta != null) {
                currentDatasetMeta = datasetMeta;
                super.fxPreferences.savePreference(PrefConstants.GEN_AI_DATASET_LATEST, datasetMeta.getId());
                super.selectModel(currentDatasetMeta.getProvider(), currentDatasetMeta.getEmbeddingModel());
                if (StringUtils.isNotBlank(currentDatasetMeta.getLanguageCode())) {
                    cbLanguage.getSelectionModel().select(new Pair<>(currentDatasetMeta.getLanguageCode(), SUPPORTED_EMBEDDING_LANG.get(datasetMeta.getLanguageCode())));
                }
                else {
                    cbLanguage.getSelectionModel().clearSelection();
                }
            }
            String jsonWorkspaces = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_PROJECTS, "{}");
            WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(jsonWorkspaces);
            workspaceSelector.loadWorkspaces(workspaceList, workspaceList.getProjects().getFirst());
            isLoading.set(false);
        });
        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
        if (datasetMap != null && !datasetMap.isEmpty()) {
            cbDataset.getItems().addAll(datasetMap.values().stream().map(ds -> new Pair<>(ds.getId(), ds)).toList());
        }
        btnAddDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddDataset.setOnAction(event -> {
            new TextInputDialog("My Dataset").showAndWait().ifPresent(datasetName -> {
                String dsId = IdUtils.makeUUID();
                currentDatasetMeta = new DatasetMeta();
                currentDatasetMeta.setId(dsId);
                currentDatasetMeta.setName(datasetName);
                Pair<String, DatasetMeta> newAgentPair = new Pair<>(dsId, currentDatasetMeta);
                cbDataset.getItems().add(newAgentPair);
                cbDataset.getSelectionModel().select(newAgentPair);
                this.saveCurrentDataset();
            });
        });
        btnRemoveDataset.setOnAction(event -> {
            if (currentDatasetMeta == null) {
                return;
            }
            if (DialogFactory.yesNoConfirmDialog("Removing Dataset", "Are you sure you want to remove the dataset '%s'?".formatted(currentDatasetMeta.getName()))) {
                isLoading.set(true);
                LlmConfig.getIns().removeDataset(currentDatasetMeta.getId());
                if (cbDataset.getItems().remove(new Pair<>(currentDatasetMeta.getId(), currentDatasetMeta))) {
                    clearAll();
                }
                else {
                    log.warn("Failed to remove dataset '{}'", currentDatasetMeta.getId());
                }
                isLoading.set(false);
            }
        });

        cbLanguage.setConverter(new PairStringStringConverter());
        cbLanguage.getItems().addAll(SUPPORTED_EMBEDDING_LANG.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList());
        cbLanguage.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveCurrentDataset();
        });

        // only embedding models are applied
        super.initProvidersAndModels(2);

        // workspace
        workspaceSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            fileSelectView.loadWorkspace(newValue.getValue(), currentDatasetMeta.getFiles(), true, false, pathname -> {
                return FilenameUtils.isExtension(pathname.getName(), SUPPORTED_EMBEDDING_FILE_TYPES);
            });
        });

        fileSelectView.getCheckModel().getCheckedItems().addListener((ListChangeListener<TreeItem<NodeData>>) c -> {
            while (c.next()) {
                log.debug("added %d, removed %d".formatted(c.getAddedSize(), c.getRemovedSize()));
                List<File> selectedFilesList = c.getList().stream().map(TreeItem::getValue).map(NodeData::getFile).toList();
                if (c.wasAdded()) {
                    currentDatasetMeta.getAddedFiles().addAll(selectedFilesList);
                }
                if (c.wasRemoved()) {
                    currentDatasetMeta.getRemovedFiles().addAll(selectedFilesList);
                }
                onModelChange();
            }
        });

        btnEmbedding.setOnAction(event -> {
            if (currentDatasetMeta == null) {
                DialogFactory.warnDialog("Please select a dataset first!");
                return;
            }
            if (currentDatasetMeta.getFiles() == null) {
                DialogFactory.warnDialog("Please select files to do embedding");
                return;
            }
            log.info("Start to embedding for files under: %s".formatted(StringUtils.join(currentDatasetMeta.getFiles(), ", ")));
            Platform.runLater(() -> {
                lblEmbeddingStatus.setText(StringUtils.EMPTY);
                pbProgress.setVisible(true);
            });
            try {
                EmbeddingService.getInstance().initDatabaseIfNotExist();
                EmbeddingService.getInstance().embed(currentDatasetMeta, payload -> {
                    Platform.runLater(() -> {
                        pbProgress.setVisible(false);
                        lblEmbeddingStatus.setText(payload.toString());
                        lblEmbeddingProgress.setText("100%");
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                DialogFactory.errDialog(e.getLocalizedMessage());
            } finally {
                pbProgress.setVisible(false);
            }
        });

        // pre-select latest selected dataset
        String latestDatasetId = super.fxPreferences.getPreference(PrefConstants.GEN_AI_DATASET_LATEST, String.class);
        int selectIdx = cbDataset.getItems().stream().map(Pair::getKey).toList().indexOf(latestDatasetId);
        log.debug("pre-select dataset item %s at index %s".formatted(latestDatasetId, selectIdx));
        cbDataset.getSelectionModel().select(selectIdx);

        // listen for embedding
        EmbeddingService.getInstance().listenOnProgressEvent(s -> {
            Platform.runLater(() -> lblEmbeddingStatus.setText(s));
        });
    }

    @Override
    protected void onModelChange() {
        saveCurrentDataset();
    }

    private DatasetMeta saveCurrentDataset() {
        if (isLoading.get()) {
            return null;
        }
        DatasetMeta datasetMeta = currentDatasetMeta;
        if (!cbLanguage.getSelectionModel().isEmpty()) {
            datasetMeta.setLanguageCode(cbLanguage.getSelectionModel().getSelectedItem().getKey());
        }
        if (!super.cbModelProvider.getSelectionModel().isEmpty()) {
            datasetMeta.setProvider(super.cbModelProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!super.cbModel.getSelectionModel().isEmpty()) {
            datasetMeta.setEmbeddingModel(super.cbModel.getSelectionModel().getSelectedItem().getValue());
        }
        datasetMeta.merge();
        LlmConfig.getIns().saveDataset(datasetMeta.getId(), datasetMeta);
        super.onSave(true);
        return datasetMeta;
    }

    private void clearAll() {
        cbDataset.getSelectionModel().clearSelection();
        cbLanguage.getSelectionModel().clearSelection();
        super.selectModel(null, null);
//        workspaceSelector.getItems().clear();
        workspaceSelector.getSelectionModel().clearSelection();
        fileSelectView.getRootItem().getChildren().clear();
        lblEmbeddingProgress.setText("");
        lblEmbeddingStatus.setText("");
        currentDatasetMeta = null;
    }

}
