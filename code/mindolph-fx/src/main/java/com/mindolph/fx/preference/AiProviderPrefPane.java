package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.BaseLoadingSavingPrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.AiModelProvider.ProviderType;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.fx.dialog.CustomModelDialog;
import com.mindolph.genai.AiUiConstants;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.AiConstants.PROVIDER_MODELS;
import static com.mindolph.core.constant.AiModelProvider.OPEN_AI;
import static com.mindolph.core.constant.SceneStatePrefs.GEN_AI_PROVIDER_ACTIVE;
import static com.mindolph.core.constant.SceneStatePrefs.GEN_AI_PROVIDER_LATEST;
import static com.mindolph.genai.AiUiConstants.MODEL_META_COMPARATOR;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;

/**
 * Preferences - AI - Model Provider
 *
 * @since 1.13.0
 */
public class AiProviderPrefPane extends BaseLoadingSavingPrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AiProviderPrefPane.class);

    @FXML
    private ChoiceBox<Pair<String, ProviderMeta>> cbProvider;
    @FXML
    private Button btnAddProvider;
    @FXML
    private Button btnRemoveProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfBaseUrl;
    @FXML
    private ListView<ModelMeta> lvModels;
    @FXML
    private Button btnAddModel;
    @FXML
    private Button btnRemoveModel;
    @FXML
    private Label lbMaxOutputTokens;
    @FXML
    private CheckBox cbUseProxy;

    private String currentProviderId;
    private ProviderMeta currentProviderMeta;

    private Map<String, ProviderMeta> providerKeyMetaMap;

    public AiProviderPrefPane() {
        super("/preference/gen_ai_model_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        super.beforeLoading();
        this.providerKeyMetaMap = LlmConfig.getIns().loadAllProviderMetas();

        // model providers without filtering.
        ChoiceUtils.initProviders(cbProvider, this.providerKeyMetaMap);

        btnAddProvider.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveProvider.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddProvider.setOnAction(event -> {
            this.createNewProvider();
        });
        btnRemoveProvider.setOnAction(event -> {
            this.removeSelectedProvider();
        });

        lvModels.setCellFactory(modelMetaListView -> {
            ListCell<ModelMeta> listCell = new ListCell<>() {
                @Override
                protected void updateItem(ModelMeta item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    }
                    else {
                        setText(item.getName());
                    }
                }
            };
            listCell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    ModelMeta modelMeta = listCell.getItem();
                    this.updateByModelSelection(modelMeta);
                }
            });
            return listCell;
        });
        super.bindPreference(cbProvider.valueProperty(), GEN_AI_PROVIDER_LATEST, OPEN_AI.name(),
                Pair::getKey,
                providerName -> new Pair<>(providerName, providerKeyMetaMap.get(providerName)),
                selected -> {
//                    super.beforeLoading();
                    ProviderMeta pm = selected.getValue();

                    this.currentProviderMeta = pm;
                    if (pm != null) {
                        log.debug("Selected provider: " + pm);
                        log.debug("Load models for ai provider: %s - %s".formatted(pm.getId(), pm.getName()));
                        this.currentProviderId = pm.getId();

                        btnRemoveProvider.setDisable(!pm.isCustom());
                        tfApiKey.setDisable(pm.isInternal());
                        tfBaseUrl.setDisable(pm.isInternal() || pm.isPublic());
                        btnAddModel.setDisable(pm.isInternal());
                        btnRemoveModel.setDisable(true); // disable when provider changes.
                        // Specific to disable the proxy support for OLLAMA since the LangChain4j is not supported it yet.
                        cbUseProxy.setDisable("OLLAMA".equals(pm.getId()) || "ALI_Q_WEN".equals(pm.getId()));

                        tfApiKey.setText(pm.apiKey());
                        tfBaseUrl.setText(pm.baseUrl());
                        cbUseProxy.setSelected(pm.useProxy());

                        // init all pre-set models and custom models
                        showAllModels(currentProviderId);
                    }
//                    super.afterLoading();
                });

        // Dynamic preference can't use bindPreference.
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges();
        });
        // Dynamic preference can't use bindPreference.
        tfBaseUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges();
        });
        cbUseProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
            super.saveChanges();
        });

        lvModels.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.updateByModelSelection(newValue);
        });

        btnAddModel.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemoveModel.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAddModel.setOnAction(event -> {
            this.createNewCustomModel(new ModelMeta());
        });
        btnRemoveModel.setOnAction(event -> {
            this.removeSelectedCustomModel();
        });


        // pre-select latest selected provider
        String latestProviderKey = super.fxPreferences.getPreferenceAlias(GEN_AI_PROVIDER_LATEST, GEN_AI_PROVIDER_ACTIVE, String.class);
        int selectIdx = cbProvider.getItems().stream().map(Pair::getKey).toList().indexOf(latestProviderKey);
        log.debug("pre-select provider item %s at index %s".formatted(latestProviderKey, selectIdx));
        cbProvider.getSelectionModel().select(selectIdx);

        super.afterLoading();
    }

    private void showAllModels(String providerName) {
        if (StringUtils.isNotBlank(providerName)) {
            lvModels.getItems().clear();
            // pre-set models
            PROVIDER_MODELS.get(providerName).stream().map("  %s"::formatted).forEach(log::trace);
            lvModels.getItems().addAll(PROVIDER_MODELS.get(providerName).stream().sorted(MODEL_META_COMPARATOR).toList());
            // custom models
            if (currentProviderMeta != null && currentProviderMeta.customModels() != null) {
                currentProviderMeta.customModels().forEach(m -> m.setCustom(true)); // load to be custom model
                lvModels.getItems().addAll(currentProviderMeta.customModels());
                lvModels.refresh();
            }
        }
    }

    private void createNewProvider() {
        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(i18n.get("prefs.ai.provider.create"))
                .content(i18n.get("dialog.input.provider.name"))
                .text("my provider")
                .width(400)
                .build();
        dialog.showAndWait().ifPresent(providerName -> {
            if (cbProvider.getItems().stream().anyMatch(p -> p.getValue().getName().equals(providerName))) {
                DialogFactory.warnDialog(i18n.get("msg.provider.exists", providerName));
                this.createNewProvider();
                return;
            }
            log.debug("new custom provider '%s'".formatted(providerName));

            currentProviderMeta = new ProviderMeta(IdUtils.makeUUID(), providerName, ProviderType.CUSTOM.name());
            Pair<String, ProviderMeta> newProviderPair = new Pair<>(currentProviderMeta.getId(), currentProviderMeta);
            cbProvider.getItems().add(newProviderPair);
            cbProvider.getSelectionModel().select(newProviderPair);
            super.saveChanges(currentProviderMeta);
        });
    }

    private void removeSelectedProvider() {
        Pair<String, ProviderMeta> p = cbProvider.getSelectionModel().getSelectedItem();
        ProviderMeta providerMeta = p.getValue();
        if (providerMeta != null && providerMeta.isCustom()) {
            boolean sure = DialogFactory.okCancelConfirmDialog(i18n.get("msg.model.delete.confirm", providerMeta.getName()));
            if (sure) {
                LlmConfig.getIns().removeCustomProvider(currentProviderId);
                providerKeyMetaMap.remove(currentProviderId);
                cbProvider.getItems().remove(p);
                cbProvider.getSelectionModel().selectNext(); // randomly
            }
        }
    }

    private void createNewCustomModel(ModelMeta modelMeta) {
        CustomModelDialog dialog = new CustomModelDialog(modelMeta);
        ModelMeta newCustomModel = dialog.showAndWait();
        if (newCustomModel == null || newCustomModel == modelMeta) return;
        // check existence before saving.
        log.debug("new custom model '%s' to provider '%s'".formatted(newCustomModel, currentProviderId));
        if (currentProviderMeta.customModels() == null) {
            currentProviderMeta.setCustomModels(new ArrayList<>());
        }
        else {
            if (currentProviderMeta.customModels().stream().anyMatch(mm -> mm.getName().equals(newCustomModel.getName()))) {
                DialogFactory.warnDialog(i18n.get("msg.model.exists", newCustomModel.getName()));
                this.createNewCustomModel(newCustomModel);
                return; // already exists
            }
        }

        currentProviderMeta.customModels().add(newCustomModel);
        super.saveChanges();
        showAllModels(currentProviderId);
    }

    private void removeSelectedCustomModel() {
        ModelMeta modelMeta = lvModels.getSelectionModel().getSelectedItem();
        if (modelMeta != null && modelMeta.isCustom()) {
            boolean sure = DialogFactory.okCancelConfirmDialog(i18n.get("msg.model.delete.confirm", modelMeta.getName()));
            if (sure) {
                currentProviderMeta.customModels().removeIf(mm -> mm.getName().equals(modelMeta.getName()));
                super.saveChanges();
                showAllModels(currentProviderId);
            }
        }
    }

    private void updateByModelSelection(ModelMeta model) {
        lbMaxOutputTokens.setVisible(model != null && (model.isInternal() || model.maxTokens() > 0));
        if (model != null) {
            if (model.isInternal()) {
//                String template = """
//                        Type: Internal embedding model
//                        Language code: %s
//                        Dimension: %d
//                        """;
                String template = i18n.get("prefs.ai.provider.model.embedding.internal.details");
                lbMaxOutputTokens.setText(template.formatted(AiUiConstants.lookupLanguage(model.getLangCode()), model.getDimension()));
            }
            else {
                // external models
                if (model.maxTokens() > 0) {
//                    String template = """
//                            Type: Chat model
//                            Is custom: %s
//                            Max output tokens: %s
//                            """;
                    String template = i18n.get("prefs.ai.provider.model.chat.external.details");
                    lbMaxOutputTokens.setText(template.formatted(model.isCustom() ? "yes" : "no", displayGenAiTokens(model.maxTokens())));
                }
            }
            btnRemoveModel.setDisable(!model.isCustom());
        }
    }

    @Override
    protected void onSave(boolean notify, Object payload) {
        this.saveCurrentProvider();
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.MODEL_PREF_CHANGED);
    }

    private void saveCurrentProvider() {
        log.debug("save current provider %s".formatted(currentProviderId));
        currentProviderMeta.setApiKey(tfApiKey.getText());
        currentProviderMeta.setBaseUrl(tfBaseUrl.getText());
        currentProviderMeta.setUseProxy(cbUseProxy.isSelected());
        LlmConfig.getIns().saveProviderMeta(currentProviderId, currentProviderMeta);
    }
}
