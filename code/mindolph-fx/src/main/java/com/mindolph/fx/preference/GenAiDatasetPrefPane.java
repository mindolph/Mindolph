package com.mindolph.fx.preference;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.EmbeddingStage;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.BaseEmbeddingService.EmbeddingProgress;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.mfx.util.GlobalExecutor;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
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
import org.apache.commons.collections4.CollectionUtils;
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
 * @since 1.13.0
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

    private WorkspaceMeta latestWorkspace;

    private StateMachine<EmbeddingState, EmbeddingProgress> embeddingStateMachine;

    public GenAiDatasetPrefPane() {
        super("/preference/gen_ai_dataset_pref_pane.fxml");
    }

    private void initStateMachine() {
        StateBuilder<EmbeddingState, EmbeddingProgress> builder = new StateBuilder<>();
        builder.state(EmbeddingState.INIT)
                .in(p -> {
                    Platform.runLater(this::disableAll);
                })
                .state(EmbeddingState.READY)
                .in(p -> {
                    Platform.runLater(() -> {
                        this.enableAll();
                        cbDataset.setDisable(false);
                        btnAddDataset.setDisable(false);
                        btnEmbedding.setText("Start embedding");
                        btnEmbedding.setDisable(false);
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
                            disableAll();
                            cbDataset.setDisable(true);
                            btnAddDataset.setDisable(true);
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
                        enableAll();
                        cbDataset.setDisable(false);
                        btnAddDataset.setDisable(false);
                        btnEmbedding.setText("Start embedding");
                        btnEmbedding.setDisable(false);
                        lblEmbeddingStatus.setText(progress.msg());
                        pbProgress.setProgress(0);
                        this.displaySelectedAndEmbeddedCount(currentDatasetMeta.getId());
                    });
                })
                .initialize(EmbeddingState.INIT)
                .action("Ready", EmbeddingState.INIT, EmbeddingState.READY)
                .action("Not ready", EmbeddingState.READY, EmbeddingState.INIT)
                .action("Still ready", EmbeddingState.READY, EmbeddingState.READY)
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
        this.initStateMachine();
        cbDataset.setConverter(datasetConverter);
        cbDataset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            beforeLoading();
            // init all components from dataset choosing.
            DatasetMeta datasetMeta = newValue.getValue();
            if (datasetMeta != null) {
                currentDatasetMeta = datasetMeta;
                // save current active dataset ID.
                super.fxPreferences.savePreference(SceneStatePrefs.GEN_AI_DATASET_LATEST, datasetMeta.getId());
                // init language selection
                ChoiceUtils.selectOrUnselectLanguage(cbLanguage, currentDatasetMeta.getLanguageCode());
                // init model provider and model.
                super.selectEmbeddingProviderAndModel(currentDatasetMeta.getProvider(), currentDatasetMeta.getEmbeddingModel());
                this.displaySelectedAndEmbeddedCount(datasetMeta.getId());
                btnRemoveDataset.setDisable(false);
                Platform.runLater(() -> embeddingStateMachine.post(EmbeddingState.READY));
            }
            else {
                Platform.runLater(() -> embeddingStateMachine.post(EmbeddingState.INIT));
                btnRemoveDataset.setDisable(true);
                log.warn("unknow dataset");
            }

            // clear file select view before init workspace selector.
            fileSelectView.getRootItem().getChildren().clear();
            fileSelectView.refresh();
            // init workspace selector and load selected files through workspace selection change event.
            String jsonWorkspaces = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_PROJECTS, "{}");
            WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(jsonWorkspaces);
            workspaceSelector.loadWorkspaces(workspaceList, latestWorkspace, false);

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
                super.saveChanges(false);
            });
        });
        btnRemoveDataset.setOnAction(event -> {
            if (currentDatasetMeta == null) {
                return;
            }
            if (DialogFactory.yesNoConfirmDialog("Remove Dataset", "Are you sure to remove the dataset '%s'? All the embedded data of this dataset will be deleted as well.".formatted(currentDatasetMeta.getName()))) {
                beforeLoading();
                LlmConfig.getIns().removeDataset(currentDatasetMeta.getId());
                cbDataset.getSelectionModel().clearSelection();
                if (cbDataset.getItems().remove(new Pair<>(currentDatasetMeta.getId(), currentDatasetMeta))) {
                    this.clearAll();
                    embeddingStateMachine.post(EmbeddingState.INIT);
                }
                else {
                    log.warn("Failed to remove dataset '{}'", currentDatasetMeta.getId());
                }
                afterLoading();
            }
        });

        // only embedding models are applied
        super.initEmbeddingModelRelatedComponents(this.cbEmbeddingProvider, this.cbLanguage, this.cbEmbeddingModel);

        // workspace
        workspaceSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            log.debug("Workspace changed: {}", newValue.getKey());
            latestWorkspace = newValue.getValue();
            fileSelectView.loadWorkspace(currentDatasetMeta, latestWorkspace, true, false,
                    pathname -> FilenameUtils.isExtension(pathname.getName(), SUPPORTED_EMBEDDING_FILE_TYPES));
        });

        EventBus.getIns().subscribeWorkspaceLoaded(1, nodeDataTreeItem -> {
            // start to listen to checked files changes only after the workspace is loaded (to avoid redundant event handling)
            fileSelectView.getCheckModel().getCheckedItems().addListener((ListChangeListener<TreeItem<NodeData>>) changed -> {
                if (super.isLoading()) {
                    return;
                }
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
                super.saveChanges(false);
                this.displaySelectedAndEmbeddedCount(currentDatasetMeta.getId());
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
                if (!currentDatasetMeta.isAllSetup()) {
                    DialogFactory.warnDialog("Setup dataset first!");
                    return;
                }
                currentDatasetMeta.setStop(false);
                if (CollectionUtils.isEmpty(currentDatasetMeta.getFiles())) {
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
        String latestDatasetId = super.fxPreferences.getPreference(SceneStatePrefs.GEN_AI_DATASET_LATEST, String.class);
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
    }

    private DatasetMeta saveCurrentDataset() {
//        if (isLoading.get()) {
//            return null;
//        }
        DatasetMeta datasetMeta = currentDatasetMeta;
        datasetMeta.setLanguageCode(super.safeGetSelectedLanguageCode(cbLanguage));
        if (super.cbEmbeddingProvider.hasSelected()) {
            datasetMeta.setProvider(super.cbEmbeddingProvider.getSelectionModel().getSelectedItem().getKey());
        }
        else {
            datasetMeta.setProvider(null);
        }
        if (super.cbEmbeddingModel.hasSelected()) {
            datasetMeta.setEmbeddingModel(super.cbEmbeddingModel.getSelectionModel().getSelectedItem().getKey());
        }
        else {
            datasetMeta.setProvider(null);
            datasetMeta.setEmbeddingModel(null);
        }
        datasetMeta.merge();
        LlmConfig.getIns().saveDataset(datasetMeta.getId(), datasetMeta);
        return datasetMeta;
    }

    private void displaySelectedAndEmbeddedCount(String datasetId) {
        GlobalExecutor.submit(() -> {
            try {
                if (!EmbeddingService.getInstance().testTableExistence()) {
                    Platform.runLater(() -> {
                        lblSelectedFiles.setText("You have never done any embedding yet");
                    });
                    return;
                }
                int count = EmbeddingService.getInstance().countEmbeddedDocuments(datasetId);
                Platform.runLater(() -> {
                    lblSelectedFiles.setText("Selected %d files, %d have been embedded".formatted(currentDatasetMeta.getFiles() == null ? 0 : currentDatasetMeta.getFiles().size(), count));
                });
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                Platform.runLater(() -> {
                    lblSelectedFiles.setText(e.getLocalizedMessage());
                });
            }
        });
    }

    private void disableAll() {
        NodeUtils.disable(cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, workspaceSelector, btnRemoveDataset, btnEmbedding, fileSelectView);
        pbProgress.setVisible(false);
//        lblSelectedFiles.setText("");
        lblEmbeddingStatus.setText("");
    }

    private void enableAll() {
        NodeUtils.enable(cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, workspaceSelector, btnRemoveDataset, btnEmbedding, fileSelectView);
    }

    private void clearAll() {
        // doesn't work here, guest it can't be called after elements were changed.
//        cbDataset.getSelectionModel().clearSelection();
        cbLanguage.getSelectionModel().clearSelection();
        super.unselectEmbeddingProviderAndModel();
//        workspaceSelector.getItems().clear();
        workspaceSelector.getSelectionModel().clearSelection();
        fileSelectView.getRootItem().getChildren().clear();
        lblSelectedFiles.setText("");
        lblEmbeddingStatus.setText("");
        currentDatasetMeta = null;
    }

    private enum EmbeddingState {
        INIT, READY, EMBEDDING, DONE
    }
}
