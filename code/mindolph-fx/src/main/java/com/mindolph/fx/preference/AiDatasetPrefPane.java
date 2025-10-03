package com.mindolph.fx.preference;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.Stage;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.EmbeddingContext;
import com.mindolph.base.genai.rag.EmbeddingProgress;
import com.mindolph.base.genai.rag.EmbeddingProgressBuilder;
import com.mindolph.base.genai.rag.EmbeddingService;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.control.WorkspaceSelector;
import com.mindolph.fx.view.FileSelectView;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.mfx.control.MChoiceBox;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mfx.util.GlobalExecutor;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mindolph.core.constant.GenAiConstants.SUPPORTED_EMBEDDING_FILE_TYPES;
import static com.mindolph.genai.GenaiUiConstants.datasetConverter;

/**
 * @since 1.13.0
 */
public class AiDatasetPrefPane extends BaseAiPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AiDatasetPrefPane.class);

    @FXML
    private ChoiceBox<Pair<String, DatasetMeta>> cbDataset;
    @FXML
    private Button btnAddDataset;
    @FXML
    private Button btnRemoveDataset;
    @FXML
    private TextField tfName;
    @FXML
    private MChoiceBox<Pair<String, String>> cbLanguage;
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

    private final EmbeddingContext embeddingContext = new EmbeddingContext();

    private StateMachine<EmbeddingState, EmbeddingProgress> embeddingStateMachine;

    private EventSource<Void> stateMachineReady;

    public AiDatasetPrefPane() {
        super("/preference/gen_ai_dataset_pref_pane.fxml");
    }

    private void initStateMachine() {
        StateBuilder<EmbeddingState, EmbeddingProgress> builder = new StateBuilder<>();
        builder.state(EmbeddingState.INIT)
                .in(p -> {
                    Platform.runLater(() -> {
                        disableAll();
                        NodeUtils.enable(cbDataset, btnAddDataset);
                        clearAll();
                        stateMachineReady.push(null); // notify that sm is ready.
                    });
                })
                .state(EmbeddingState.READY)
                .in(p -> {
                    Platform.runLater(() -> {
                        this.enableAll();
                        this.toggleEmbeddingModel();
                        btnEmbedding.setText("Start embedding");
                        lblEmbeddingStatus.setText("Ready to do embedding");
                        pbProgress.setVisible(false);
                        pbProgress.setProgress(0);
                    });
                })
                .state(EmbeddingState.EMBEDDING)
                .in(progress -> {
                    Platform.runLater(() -> {
                        if (progress.stage() == null) {
                            // start embedding
                            disableAll();
                            NodeUtils.enable(btnEmbedding);
                            btnEmbedding.setText("Stop embedding");
                            lblEmbeddingStatus.setText(progress.msg());
                            pbProgress.setVisible(true);
                            pbProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                            fileSelectView.clearEmbeddingStatusLabels();
                        }
                        else {
                            // embedding in progress
                            lblEmbeddingStatus.setText(progress.msg());
                            if (Stage.EMBEDDING.equals(progress.stage())) {
                                log.debug("progress: %.1f at file %s".formatted(progress.ratio(), progress.file()));
                                pbProgress.setProgress(progress.ratio());
                                fileSelectView.findAndUpdateName(progress.file(), progress.success() ? "embedded" : "fail");
                                fileSelectView.refresh();
                            }
                        }
                    });
                })
                .state(EmbeddingState.UNEMBEDDING)
                .in(progress -> {
                    Platform.runLater(() -> {
                        if (progress.stage() == null) {
                            // start embedding
                            disableAll();
                            lblEmbeddingStatus.setText(progress.msg());
                            pbProgress.setVisible(true);
                            pbProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                            fileSelectView.clearEmbeddingStatusLabels();
                        }
                        else {
                            // unembedding in progress
                            lblEmbeddingStatus.setText(progress.msg());
                            if (Stage.UNEMBEDDING.equals(progress.stage())) {
                                log.debug("progress: %.1f at file %s".formatted(progress.ratio(), progress.file()));
                                pbProgress.setProgress(progress.ratio());
                                fileSelectView.findAndUpdateName(progress.file(), progress.success() ? "never" : "fail");
                                fileSelectView.refresh();
                            }
                        }
                    });
                })
                .state(EmbeddingState.DONE)
                .in(progress -> {
                    Platform.runLater(() -> {
                        enableAll();
                        this.toggleEmbeddingModel();
                        btnEmbedding.setDisable(progress.stage() == Stage.UNEMBEDDING); // should be disabled for embedding because the dataset selection has been cleared
                        btnEmbedding.setText("Start embedding");
                        lblEmbeddingStatus.setText(progress.msg());
                        pbProgress.setDisable(true);
                        pbProgress.setProgress(0);
                        this.displaySelectedAndEmbeddedCountAsync(currentDatasetMeta.getId(), aBoolean -> {
                            if (progress.stage() == Stage.UNEMBEDDING) {
                                Platform.runLater(this::clearAll);
                            }
                        });
                    });
                })
                .initialize(EmbeddingState.INIT)
                .action("Ready", EmbeddingState.INIT, EmbeddingState.READY)
                .action("Not ready", EmbeddingState.READY, EmbeddingState.INIT)
                .action("Still ready", EmbeddingState.READY, EmbeddingState.READY)
//                .action("Start to embed", EmbeddingState.READY, EmbeddingState.EMBEDDING)
                .action("Embedding on progress", EmbeddingState.EMBEDDING, EmbeddingState.EMBEDDING)
                .action("Embedding is done", EmbeddingState.EMBEDDING, EmbeddingState.DONE)
                .action("Start to unembed (and embed)", EmbeddingState.READY, EmbeddingState.UNEMBEDDING)
                .action("Unembedding on progress", EmbeddingState.UNEMBEDDING, EmbeddingState.UNEMBEDDING)
                .action("Unembedding is done", EmbeddingState.UNEMBEDDING, EmbeddingState.DONE)
                .action("Unembedding is done and dataset is removed", EmbeddingState.UNEMBEDDING, EmbeddingState.INIT)
                .action("Start to embed after unembedding", EmbeddingState.UNEMBEDDING, EmbeddingState.EMBEDDING)
                .action("Reset", EmbeddingState.DONE, EmbeddingState.READY)
                .action("Restart embedding", EmbeddingState.DONE, EmbeddingState.EMBEDDING)
                .action("Restart unembedding", EmbeddingState.DONE, EmbeddingState.UNEMBEDDING);

        embeddingStateMachine = new StateMachine<>(builder);
        embeddingStateMachine.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        stateMachineReady = new EventSource<>();
        stateMachineReady.subscribe(v -> {
            // pre-select latest selected dataset after state machine is ready
            String latestDatasetId = super.fxPreferences.getPreference(SceneStatePrefs.GEN_AI_DATASET_LATEST, String.class);
            int selectIdx = cbDataset.getItems().stream().map(Pair::getKey).toList().indexOf(latestDatasetId);
            log.debug("pre-select dataset item %s at index %s".formatted(latestDatasetId, selectIdx));
            cbDataset.getSelectionModel().select(selectIdx);
        });

        cbDataset.setConverter(datasetConverter);
        cbDataset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            DatasetMeta datasetMeta = newValue.getValue();
            this.switchDataset(datasetMeta);
        });
        this.reloadDatasets();

        btnAddDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveDataset.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddDataset.setOnAction(event -> {
            this.createNewDataset("My Dataset");
        });
        btnRemoveDataset.setOnAction(event -> {
            this.removeSelectedDataset();
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
                currentDatasetMeta.merge();
                this.displaySelectedAndEmbeddedCountAsync(currentDatasetMeta.getId(), success -> {
                });
            });
        });

        tfName.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges();
        });

        btnEmbedding.setOnAction(event -> {
            this.embedCurrentDataset();
        });
        this.initStateMachine();
    }

    private void reloadDatasets() {
        log.debug("reload datasets for selection");
        int selectedIndex = cbDataset.getSelectionModel().getSelectedIndex();
        cbDataset.getItems().clear();
        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
        if (datasetMap != null && !datasetMap.isEmpty()) {
            cbDataset.getItems().addAll(datasetMap.values().stream().map(ds -> new Pair<>(ds.getId(), ds)).toList());
        }
        cbDataset.getSelectionModel().select(selectedIndex);
    }

    private void switchDataset(DatasetMeta datasetMeta) {
        beforeLoading();
        // init all components from dataset choosing.

        if (datasetMeta != null) {
            currentDatasetMeta = datasetMeta;
            // save current active dataset ID.
            super.fxPreferences.savePreference(SceneStatePrefs.GEN_AI_DATASET_LATEST, datasetMeta.getId());
            tfName.setText(datasetMeta.getName());
            // init language selection
            ChoiceUtils.selectOrUnselectLanguage(cbLanguage, currentDatasetMeta.getLanguageCode());
            // init model provider and model selectors.
            super.selectEmbeddingProviderAndModel(currentDatasetMeta.getProvider(), currentDatasetMeta.getEmbeddingModel());
            lblSelectedFiles.setText("Synchronizing embedding state...");
            this.displaySelectedAndEmbeddedCountAsync(datasetMeta.getId(), isEmbedded -> {
                log.debug("Is this dataset embedded: %s".formatted(isEmbedded));
                embeddingContext.setEmbedded(isEmbedded);
                btnRemoveDataset.setDisable(false);
                embeddingStateMachine.post(EmbeddingState.READY);
            });
        }
        else {
            Platform.runLater(() -> embeddingStateMachine.post(EmbeddingState.INIT));
            log.warn("unknown dataset");
        }

        // clear file select view before init workspace selector.
        fileSelectView.getRootItem().getChildren().clear();
        fileSelectView.refresh();
        // init workspace selector and load selected files through workspace selection change event.
        String jsonWorkspaces = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_PROJECTS, "{}");
        WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(jsonWorkspaces);
        workspaceSelector.loadWorkspaces(workspaceList, latestWorkspace, false);

        afterLoading();
    }

    private void createNewDataset(String defaultNewName) {
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Create new dataset")
                .content("Input dataset name")
                .text(defaultNewName)
                .width(400)
                .build();
        dialog.showAndWait().ifPresent(datasetName -> {
            if (cbDataset.getItems().stream().anyMatch(p -> p.getValue().getName().equals(datasetName))) {
                DialogFactory.warnDialog("Dataset names %s already exists".formatted(datasetName));
                this.createNewDataset(datasetName);
                return;
            }
            String dsId = IdUtils.makeUUID();
            currentDatasetMeta = new DatasetMeta();
            currentDatasetMeta.setId(dsId);
            currentDatasetMeta.setName(datasetName);
            Pair<String, DatasetMeta> newAgentPair = new Pair<>(dsId, currentDatasetMeta);
            cbDataset.getItems().add(newAgentPair);
            cbDataset.getSelectionModel().select(newAgentPair);
            super.saveChanges(false);
        });
    }

    private void embedCurrentDataset() {
        btnEmbedding.setDisable(true); // MUST disable it.
        if (embeddingStateMachine.isState(EmbeddingState.EMBEDDING)) {
            // try to stop the embedding.
            currentDatasetMeta.setStop(true);
            return;
        }
        if (currentDatasetMeta == null) {
            DialogFactory.warnDialog("Please select a dataset first.");
            btnEmbedding.setDisable(false);
            return;
        }
        if (!currentDatasetMeta.isAllSetup()) {
            DialogFactory.warnDialog("Setup dataset first");
            btnEmbedding.setDisable(false);
            return;
        }
        currentDatasetMeta.setStop(false);
        if (CollectionUtils.isEmpty(currentDatasetMeta.getFiles())) {
            DialogFactory.warnDialog("Please select files to do embedding");
            btnEmbedding.setDisable(false);
            return;
        }
        try {
            log.info("Start to delete embedding data for unselected files...");
            embeddingStateMachine.postWithPayload(EmbeddingState.UNEMBEDDING, new EmbeddingProgressBuilder().msg("Start to remove embedded data for unselected files...").build());

            // try to remove data of unselected files first.
            List<File> selectedFiles = currentDatasetMeta.getFiles();
            List<String> selectedFilePaths = selectedFiles.stream().map(File::getPath).toList();
            CompletableFuture<?> completableFuture = EmbeddingService.getInstance().unembedDataset(currentDatasetMeta, embeddingDocEntity -> !selectedFilePaths.contains(embeddingDocEntity.file_path()), embeddingProgress -> {
                log.info("Unselected files have been deleted.");
            });

            // do embedding after the unembedding is done.
            completableFuture.thenApply((Function<Object, Object>) o -> {
                log.info("Start to embed files (%d): %s".formatted(currentDatasetMeta.getFiles().size(), StringUtils.join(currentDatasetMeta.getFiles(), ", ")));
                embeddingStateMachine.postWithPayload(EmbeddingState.EMBEDDING, new EmbeddingProgressBuilder().msg("Start to embed selected files...").build());
                EmbeddingService.getInstance().embedDataset(currentDatasetMeta, progress -> {
                    if (progress.stage() == Stage.EMBEDDING) {
                        embeddingStateMachine.postWithPayload(EmbeddingState.EMBEDDING, progress);
                    }
                    else if (progress.stage() == Stage.EMBEDDING_DONE) {
                        int percent = BigDecimal.valueOf(progress.successCount()).divide(BigDecimal.valueOf(currentDatasetMeta.getFiles().size()), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
                        currentDatasetMeta.setProgress(percent);
                        super.saveChanges();
                        embeddingContext.setEmbedded(true);
                        embeddingStateMachine.postWithPayload(EmbeddingState.DONE, progress);
                    }
                });
                // NOTE: the progress events are listened by the EmbeddingService::listenOnProgressEvent.
                return null;
            });
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            DialogFactory.errDialog(e.getLocalizedMessage());
            currentDatasetMeta.setStop(true); // force to stop if any exception occurs.
            embeddingStateMachine.postWithPayload(EmbeddingState.DONE, new EmbeddingProgressBuilder().msg("ERROR: %s".formatted(e.getLocalizedMessage())).build());
        }
    }

    private void removeSelectedDataset() {
        if (currentDatasetMeta == null) {
            return;
        }
        if (DialogFactory.yesNoConfirmDialog("Remove Dataset", "Are you sure to remove the dataset '%s'? All the embedded data of this dataset will be deleted as well.".formatted(currentDatasetMeta.getName()))) {
            beforeLoading();
            // unembed dataset
            embeddingStateMachine.postWithPayload(EmbeddingState.UNEMBEDDING, new EmbeddingProgressBuilder().msg("Start to remove embedded data...").stage(Stage.PREPARE).build());
            EmbeddingService.getInstance().unembedDataset(currentDatasetMeta, embeddingDocEntity -> true, progress -> {
                if (progress.success()) {
                    log.debug("Successfully deleted embedded data for dataset {}", currentDatasetMeta.getName());
                    LlmConfig.getIns().removeDataset(currentDatasetMeta);
                    Platform.runLater(() -> {
                        cbDataset.getSelectionModel().clearSelection();
                        if (cbDataset.getItems().remove(new Pair<>(currentDatasetMeta.getId(), currentDatasetMeta))) {
                            embeddingContext.setEmbedded(false);
                            embeddingStateMachine.postWithPayload(EmbeddingState.INIT,
                                    new EmbeddingProgressBuilder().msg("All embedded data of dataset %s has been deleted.".formatted(currentDatasetMeta.getName())).stage(Stage.UNEMBEDDING).build());
                        }
                        else {
                            log.warn("Failed to remove dataset '{}'", currentDatasetMeta.getId());
                        }
                    });
                }
                else {
                    log.debug("Removing dataset fail: {}", currentDatasetMeta.getId());
                    Notifications.create().title("Removing dataset").text("Removing dataset fail");
                    embeddingStateMachine.postWithPayload(EmbeddingState.DONE,
                            new EmbeddingProgressBuilder().msg("Removing dataset %s fail".formatted(currentDatasetMeta.getName())).stage(Stage.UNEMBEDDING).build());
                }
            });
            afterLoading();
        }
    }

    @Override
    protected void onSave(boolean notify) {
        if (currentDatasetMeta != null) {
            boolean isNameChanged = !currentDatasetMeta.getName().equals(tfName.getText());
            this.saveCurrentDataset();
            if (isNameChanged) {
                this.reloadDatasets();
            }
        }
        super.onSave(notify);
    }

    private DatasetMeta saveCurrentDataset() {
//        if (isLoading.get()) {
//            return null;
//        }
        DatasetMeta datasetMeta = currentDatasetMeta;
        datasetMeta.setName(this.tfName.getText());
        datasetMeta.setLanguageCode(super.safeGetSelectedLanguageCode(cbLanguage));
        datasetMeta.setProvider(super.cbEmbeddingProvider.hasSelected() ? super.cbEmbeddingProvider.getSelectionModel().getSelectedItem().getKey() : null);
        datasetMeta.setEmbeddingModel(super.cbEmbeddingModel.hasSelected() ? super.cbEmbeddingModel.getSelectionModel().getSelectedItem().getKey() : null);
        if (!super.cbEmbeddingModel.hasSelected()) {
            datasetMeta.setProvider(null);
        }
//        datasetMeta.merge();
        LlmConfig.getIns().saveDataset(datasetMeta.getId(), datasetMeta);
        return datasetMeta;
    }

    /**
     *
     * @param datasetId
     * @param consumer  with true if embedded for sure.
     */
    private CompletableFuture<Void> displaySelectedAndEmbeddedCountAsync(String datasetId, Consumer<Boolean> consumer) {
        return GlobalExecutor.submitCompletable(() -> {
            try {
                if (!EmbeddingService.getInstance().testTableExistence()) {
                    this.safeChangeFileSelectionLabel("You have never done any embedding yet");
                    consumer.accept(false);
                }
                int count = EmbeddingService.getInstance().countEmbeddedDocuments(datasetId);
                this.safeChangeFileSelectionLabel("Selected %d files, %d have been embedded".formatted(currentDatasetMeta.size(), count));
                consumer.accept(count > 0);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                this.safeChangeFileSelectionLabel(e.getLocalizedMessage());
                consumer.accept(false);
            }
        });
    }

    private void safeChangeFileSelectionLabel(String msg) {
        Platform.runLater(() -> {
            lblSelectedFiles.setText(msg);
        });
    }

    private void disableAll() {
        NodeUtils.disable(cbDataset, tfName, cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, workspaceSelector, btnAddDataset, btnRemoveDataset, btnEmbedding, fileSelectView);
        pbProgress.setVisible(false);
//        lblSelectedFiles.setText("");
        lblEmbeddingStatus.setText("");
    }

    private void enableAll() {
        NodeUtils.enable(cbDataset, tfName, cbLanguage, cbEmbeddingProvider, cbEmbeddingModel, workspaceSelector, btnAddDataset, btnRemoveDataset, btnEmbedding, fileSelectView);
    }

    private void toggleEmbeddingModel() {
        boolean enable = !embeddingContext.isEmbedded();
        cbLanguage.setDisable(!enable);
        cbEmbeddingProvider.setDisable(!enable);
        cbEmbeddingModel.setDisable(!enable);
    }

    private void clearAll() {
        // doesn't work here, guest it can't be called after elements were changed.
//        cbDataset.getSelectionModel().clearSelection();
        tfName.clear();
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
        INIT,  // initial state, not ready to do embedding
        READY, // ready to do embedding
        EMBEDDING, // start embedding or on progress
        UNEMBEDDING, // start unembedding or on progress
        DONE // embedding/unembedding is done
    }
}
