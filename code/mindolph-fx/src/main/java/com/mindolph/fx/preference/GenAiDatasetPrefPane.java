package com.mindolph.fx.preference;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.EmbeddingStage;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.BaseEmbeddingService.EmbeddingProgress;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.control.WorkspaceSelector;
import com.mindolph.fx.view.FileSelectView;
import com.mindolph.genai.ChoiceUtils;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;
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
    private Label lblSelectedFiles;

    private DatasetMeta currentDatasetMeta;


    private final StateMachine<EmbeddingState, EmbeddingProgress> embeddingStateMachine;

    public GenAiDatasetPrefPane() {
        super("/preference/gen_ai_dataset_pref_pane.fxml");

        StateBuilder<EmbeddingState, EmbeddingProgress> builder = new StateBuilder<>();
        builder.state(EmbeddingState.READY)
                .in(p -> {
                    Platform.runLater(() -> {
                        btnEmbedding.setText("Start embedding");
                        lblEmbeddingStatus.setText("Ready to do embedding");
                        pbProgress.setVisible(true);
                        pbProgress.setProgress(0);
                    });
                })
                .state(EmbeddingState.EMBEDDING)
                .in(progress -> {
                    Platform.runLater(() -> {
                        if (progress.stage() == null) {
                            // start embedding
                            btnEmbedding.setText("Stop embedding");
                            btnEmbedding.setDisable(false);
                            lblEmbeddingStatus.setText(progress.msg());
                            pbProgress.setVisible(true);
                            pbProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                            fileSelectView.clearEmbeddingStatusLabels();
                        }
                        else {
                            // embedding in progress
                            lblEmbeddingStatus.setText(progress.msg());
                            if (EmbeddingStage.EMBEDDING.equals(progress.stage())) {
                                log.debug("progress: %.1f at file %s".formatted(progress.ratio(), progress.file()));
                                pbProgress.setProgress(progress.ratio());
                                fileSelectView.findAndUpdateName(progress.file(), progress.success() ? "embedded" : "fail");
                                fileSelectView.refresh();
                            }
                        }
                    });
                })
                .state(EmbeddingState.DONE)
                .in(progress -> {
                    Platform.runLater(() -> {
                        btnEmbedding.setText("Start embedding");
                        btnEmbedding.setDisable(false);
                        lblEmbeddingStatus.setText(progress.msg());
                        pbProgress.setProgress(0);
                    });
                })
                .initialize(EmbeddingState.READY)
                .action("Ready", EmbeddingState.READY, EmbeddingState.READY)
                .action("Start to embed", EmbeddingState.READY, EmbeddingState.EMBEDDING)
                .action("Embedding", EmbeddingState.EMBEDDING, EmbeddingState.EMBEDDING)
                .action("Embedding is done", EmbeddingState.EMBEDDING, EmbeddingState.DONE)
                .action("Reset", EmbeddingState.DONE, EmbeddingState.READY)
                .action("Restart embedding", EmbeddingState.DONE, EmbeddingState.EMBEDDING);

        embeddingStateMachine = new StateMachine<>(builder);
        embeddingStateMachine.start();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbDataset.setConverter(datasetConverter);
        cbDataset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            beforeLoading();
            // init all components from dataset choosing.
            DatasetMeta datasetMeta = newValue.getValue();
            if (datasetMeta != null) {
                currentDatasetMeta = datasetMeta;
                // save current active dataset ID.
                super.fxPreferences.savePreference(PrefConstants.GEN_AI_DATASET_LATEST, datasetMeta.getId());
                // init language selection
                ChoiceUtils.selectOrUnselectLanguage(cbLanguage, currentDatasetMeta.getLanguageCode());
                // init model provider and model.
                super.selectEmbeddingProviderAndModel(currentDatasetMeta.getProvider(), currentDatasetMeta.getEmbeddingModel());
                lblSelectedFiles.setText("Selected %d files".formatted(datasetMeta.getFiles() == null ? 0 : datasetMeta.getFiles().size()));
            }
            else {
                log.warn("unknow dataset");
            }
            // init workspace selector and load selected files through workspace selection change event.
            String jsonWorkspaces = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_PROJECTS, "{}");
            WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(jsonWorkspaces);
            workspaceSelector.loadWorkspaces(workspaceList, workspaceList.getProjects().getFirst());
            afterLoading();
        });
        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
        if (datasetMap != null && !datasetMap.isEmpty()) {
            cbDataset.getItems().addAll(datasetMap.values().stream().map(ds -> new Pair<>(ds.getId(), ds)).toList());
        }
        btnAddDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddDataset.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("My Dataset");
            dialog.setTitle("Create new dataset");
            dialog.setContentText("Enter dataset name:");
            dialog.showAndWait().ifPresent(datasetName -> {
                String dsId = IdUtils.makeUUID();
                currentDatasetMeta = new DatasetMeta();
                currentDatasetMeta.setId(dsId);
                currentDatasetMeta.setName(datasetName);
                Pair<String, DatasetMeta> newAgentPair = new Pair<>(dsId, currentDatasetMeta);
                cbDataset.getItems().add(newAgentPair);
                cbDataset.getSelectionModel().select(newAgentPair);
                super.saveChanges();
            });
        });
        btnRemoveDataset.setOnAction(event -> {
            if (currentDatasetMeta == null) {
                return;
            }
            if (DialogFactory.yesNoConfirmDialog("Removing Dataset", "Are you sure you want to remove the dataset '%s'?".formatted(currentDatasetMeta.getName()))) {
                beforeLoading();
                LlmConfig.getIns().removeDataset(currentDatasetMeta.getId());
                if (cbDataset.getItems().remove(new Pair<>(currentDatasetMeta.getId(), currentDatasetMeta))) {
                    clearAll();
                }
                else {
                    log.warn("Failed to remove dataset '{}'", currentDatasetMeta.getId());
                }
                afterLoading();
            }
        });

        // only embedding models are applied
        super.initEmbeddingModelComponents(this.cbEmbeddingProvider, this.cbLanguage, this.cbEmbeddingModel);

        // workspace
        workspaceSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            log.debug("Workspace changed: {}", newValue);
            fileSelectView.loadWorkspace(newValue.getValue(), currentDatasetMeta.getFiles(), true, false, pathname -> {
                return FilenameUtils.isExtension(pathname.getName(), SUPPORTED_EMBEDDING_FILE_TYPES);
            });
        });

        EventBus.getIns().subscribeWorkspaceLoaded(1, nodeDataTreeItem -> {
            // start to listen to checked files changes only after the workspace is loaded (to avoid redundant event handling)
            fileSelectView.getCheckModel().getCheckedItems().addListener((ListChangeListener<TreeItem<NodeData>>) changed -> {
//                if (isLoading.get()) {
//                    return;
//                }
                while (changed.next()) {
                    log.debug("Selection changed: added %d, removed %d".formatted(changed.getAddedSize(), changed.getRemovedSize()));
                    List<NodeData> addedNodes = changed.getAddedSubList().stream().map(TreeItem::getValue).filter(NodeData::isFile).toList();
                    List<NodeData> removedNodes = changed.getRemoved().stream().map(TreeItem::getValue).filter(NodeData::isFile).toList();
                    if (log.isTraceEnabled()) {
                        addedNodes.forEach(nd -> log.trace("%s %s".formatted(nd.getName(), String.valueOf(nd.getFile()))));
                        removedNodes.forEach(nd -> log.trace("%s %s".formatted(nd.getName(), String.valueOf(nd.getFile()))));
                    }
                    currentDatasetMeta.getAddedFiles().addAll(addedNodes.stream().map(NodeData::getFile).toList());
                    currentDatasetMeta.getRemovedFiles().addAll(removedNodes.stream().map(NodeData::getFile).toList());
                }
                super.saveChanges();
            });
        });

        btnEmbedding.setOnAction(event -> {
            btnEmbedding.setDisable(true); // MUST disable it.
            if (embeddingStateMachine.isState(EmbeddingState.EMBEDDING)) {
                // try to stop the embedding.
                currentDatasetMeta.setStop(true);
            }
            else {
                if (currentDatasetMeta == null) {
                    DialogFactory.warnDialog("Please select a dataset first!");
                    return;
                }
                currentDatasetMeta.setStop(false);
                if (currentDatasetMeta.getFiles() == null) {
                    DialogFactory.warnDialog("Please select files to do embedding");
                    return;
                }
                log.info("Start to embed files: %s".formatted(StringUtils.join(currentDatasetMeta.getFiles(), ", ")));
                try {
                    embeddingStateMachine.postWithPayload(EmbeddingState.EMBEDDING, new EmbeddingProgress("Start to embed selected files..."));
                    EmbeddingService.getInstance().embedDataset(currentDatasetMeta, progress -> {
                        int percent = BigDecimal.valueOf(progress.successCount()).divide(BigDecimal.valueOf(currentDatasetMeta.getFiles().size()), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
                        currentDatasetMeta.setStatus(percent);
                        super.saveChanges();
                        embeddingStateMachine.postWithPayload(EmbeddingState.DONE, progress);
                    });
                    // NOTE: the progress events are listened by the EmbeddingService::listenOnProgressEvent.
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                    DialogFactory.errDialog(e.getLocalizedMessage());
                    currentDatasetMeta.setStop(true); // force to stop if any exception occurs.
                    embeddingStateMachine.postWithPayload(EmbeddingState.DONE, new EmbeddingProgress("ERROR: %s".formatted(e.getLocalizedMessage())));
                }
            }
        });

        // pre-select latest selected dataset
        String latestDatasetId = super.fxPreferences.getPreference(PrefConstants.GEN_AI_DATASET_LATEST, String.class);
        int selectIdx = cbDataset.getItems().stream().map(Pair::getKey).toList().indexOf(latestDatasetId);
        log.debug("pre-select dataset item %s at index %s".formatted(latestDatasetId, selectIdx));
        cbDataset.getSelectionModel().select(selectIdx);

        // listen to the progress of embedding
        EmbeddingService.getInstance().listenOnProgressEvent(progress -> {
            embeddingStateMachine.postWithPayload(EmbeddingState.EMBEDDING, progress);
        });

    }

    @Override
    protected void onSave(boolean notify) {
        super.onSave(notify);
        this.saveCurrentDataset();
        // update label after calculated.
        lblSelectedFiles.setText("Selected %d files".formatted(currentDatasetMeta.getFiles().size()));
        embeddingStateMachine.post(EmbeddingState.READY);
    }

    private DatasetMeta saveCurrentDataset() {
//        if (isLoading.get()) {
//            return null;
//        }
        DatasetMeta datasetMeta = currentDatasetMeta;
        if (!cbLanguage.getSelectionModel().isEmpty()) {
            datasetMeta.setLanguageCode(cbLanguage.getSelectionModel().getSelectedItem().getKey());
        }
        if (!super.cbEmbeddingProvider.getSelectionModel().isEmpty()) {
            datasetMeta.setProvider(super.cbEmbeddingProvider.getSelectionModel().getSelectedItem().getKey());
        }
        if (!super.cbEmbeddingModel.getSelectionModel().isEmpty()) {
            datasetMeta.setEmbeddingModel(super.cbEmbeddingModel.getSelectionModel().getSelectedItem().getValue());
        }
        else {
            datasetMeta.setEmbeddingModel(null);
        }
        datasetMeta.merge();
        LlmConfig.getIns().saveDataset(datasetMeta.getId(), datasetMeta);
        return datasetMeta;
    }

    private void clearAll() {
        cbDataset.getSelectionModel().clearSelection();
        cbLanguage.getSelectionModel().clearSelection();
        super.selectEmbeddingProviderAndModel(null, null);
//        workspaceSelector.getItems().clear();
        workspaceSelector.getSelectionModel().clearSelection();
        fileSelectView.getRootItem().getChildren().clear();
        lblEmbeddingStatus.setText("");
        currentDatasetMeta = null;
    }

    private enum EmbeddingState {
        READY, EMBEDDING, DONE
    }
}
