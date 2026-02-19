package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.BaseLoadingSavingPrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.fx.dialog.CustomModelDialog;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.core.constant.SceneStatePrefs.GEN_AI_PROVIDER_ACTIVE;
import static com.mindolph.genai.GenAiUtils.displayGenAiTokens;
import static com.mindolph.genai.GenaiUiConstants.*;

/**
 * @since 1.13.0
 */
public class AiProviderPrefPane extends BaseLoadingSavingPrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AiProviderPrefPane.class);

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfBaseUrl;
    @FXML
    private ListView<ModelMeta> lvModels;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnRemove;
    @FXML
    private Label lbMaxOutputTokens;
    @FXML
    private CheckBox cbUseProxy;

    private String currentProviderName;
    private ProviderMeta currentProviderMeta;

    public AiProviderPrefPane() {
        super("/preference/gen_ai_model_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        super.beforeLoading();
        // model providers
        cbProvider.setConverter(providerConverter);
        List<Pair<GenAiModelProvider, String>> providerPairs = EnumUtils.getEnumList(GenAiModelProvider.class).stream().map(p -> new Pair<>(p, p.getDisplayName())).toList();
        cbProvider.getItems().addAll(providerPairs);

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
        super.bindPreference(cbProvider.valueProperty(), GEN_AI_PROVIDER_ACTIVE, OPEN_AI.name(),
                pair -> pair.getKey().name(),
                providerName -> new Pair<>(valueOf(providerName), providerName),
                selected -> {
//                    super.beforeLoading();
                    Map<String, ProviderMeta> map = LlmConfig.getIns().loadAllProviderMetas();
                    GenAiModelProvider provider = selected.getKey();
                    if (provider != null) {
                        log.debug("Load models for gen-ai provider: %s".formatted(provider.name()));
                        this.currentProviderName = provider.name();
                        if (provider.getType() == ProviderType.PUBLIC) {
                            tfApiKey.setDisable(false);
                            tfBaseUrl.setDisable(true);
                        }
                        else if (provider.getType() == ProviderType.PRIVATE) {
                            tfApiKey.setDisable(true);
                            tfBaseUrl.setDisable(false);
                        }
                        btnAdd.setDisable(provider.getType() == ProviderType.INTERNAL);
                        btnRemove.setDisable(true); // disable when provider changes.
                        ProviderMeta providerMeta = map.get(currentProviderName);
                        if (providerMeta == null) {
                            // init for a vendor who had never been set up.
                            providerMeta = new ProviderMeta("", "", MODEL_CUSTOM_ITEM.getValue().getName(), false);
                        }
                        this.currentProviderMeta = providerMeta;
                        tfApiKey.setText(providerMeta.apiKey());
                        tfBaseUrl.setText(providerMeta.baseUrl());
                        cbUseProxy.setSelected(providerMeta.useProxy());

                        // Specific to disable the proxy support for OLLAMA since the LangChain4j is not supported it yet.
                        cbUseProxy.setDisable(provider == OLLAMA || provider == ALI_Q_WEN);

                        // init all pre-set models and custom models
                        showAllModels(currentProviderName);
                        fxPreferences.savePreference(SceneStatePrefs.GEN_AI_PROVIDER_LATEST, currentProviderName);
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

        btnAdd.setGraphic(FontIconManager.getIns().getIcon(IconKey.PLUS));
        btnRemove.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));
        btnAdd.setOnAction(event -> {
            this.createNewCustomModel(new ModelMeta());
        });
        btnRemove.setOnAction(event -> {
            this.removeSelectedCustomModel();
        });


        // pre-select latest selected provider
        String latestProviderKey = super.fxPreferences.getPreferenceAlias(SceneStatePrefs.GEN_AI_PROVIDER_LATEST, GEN_AI_PROVIDER_ACTIVE, String.class);
        int selectIdx = cbProvider.getItems().stream().map(Pair::getKey).map(GenAiModelProvider::name).toList().indexOf(latestProviderKey);
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

    private void createNewCustomModel(ModelMeta modelMeta) {
        CustomModelDialog dialog = new CustomModelDialog(modelMeta);
        ModelMeta newCustomModel = dialog.showAndWait();
        if (newCustomModel == null|| newCustomModel == modelMeta) return;
        // check existence before saving.
        log.debug("new custom model %s to provider %s".formatted(newCustomModel, currentProviderName));
        if (currentProviderMeta.customModels() == null) {
            currentProviderMeta.setCustomModels(new ArrayList<>());
        }
        else {
            if (currentProviderMeta.customModels().stream().anyMatch(mm -> mm.getName().equals(newCustomModel.getName()))) {
                DialogFactory.warnDialog("Model %s already exists".formatted(newCustomModel.getName()));
                this.createNewCustomModel(newCustomModel);
                return; // already exists
            }
        }

        currentProviderMeta.customModels().add(newCustomModel);
        super.saveChanges();
        showAllModels(currentProviderName);
    }

    private void removeSelectedCustomModel() {
        ModelMeta modelMeta = lvModels.getSelectionModel().getSelectedItem();
        if (modelMeta != null && modelMeta.isCustom()) {
            boolean sure = DialogFactory.okCancelConfirmDialog("Are you sure to delete the custom model '%s'".formatted(modelMeta.getName()));
            if (sure) {
                currentProviderMeta.customModels().removeIf(mm -> mm.getName().equals(modelMeta.getName()));
                super.saveChanges();
                showAllModels(currentProviderName);
            }
        }
    }

    private void updateByModelSelection(ModelMeta model) {
        lbMaxOutputTokens.setVisible(model != null && (model.isInternal() || model.maxTokens() > 0));
        if (model != null) {
            if (model.isInternal()) {
                String template = """
                        Type: Internal embedding model
                        Language code: %s
                        Dimension: %d
                        """;
                lbMaxOutputTokens.setText(template.formatted(GenAiConstants.lookupLanguage(model.getLangCode()), model.getDimension()));
            }
            else {
                // external models
                if (model.maxTokens() > 0) {
                    String template = """
                            Type: Chat model
                            Is custom: %s
                            Max output tokens: %s
                            """;
                    lbMaxOutputTokens.setText(template.formatted(model.isCustom() ? "yes" : "no", displayGenAiTokens(model.maxTokens())));
                }
            }
            btnRemove.setDisable(!model.isCustom());
        }
    }

    @Override
    protected void onSave(boolean notify, Object payload) {
        this.saveCurrentProvider();
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.MODEL_PREF_CHANGED);
    }

    private void saveCurrentProvider() {
        log.debug("save current provider %s".formatted(currentProviderName));
        currentProviderMeta.setApiKey(tfApiKey.getText());
        currentProviderMeta.setBaseUrl(tfBaseUrl.getText());
        currentProviderMeta.setUseProxy(cbUseProxy.isSelected());
        LlmConfig.getIns().saveProviderMeta(GenAiModelProvider.valueOf(currentProviderName), currentProviderMeta);
    }
}
