package com.mindolph.fx.preference;

import com.mindolph.base.control.BaseLoadingSavingPrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.LocalModelManager;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.genai.GenAiUtils;
import com.mindolph.mfx.control.MChoiceBox;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.SimpleProgressDialog;
import com.mindolph.mfx.util.GlobalExecutor;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import static com.mindolph.core.constant.GenAiConstants.MODEL_TYPE_CHAT;
import static com.mindolph.core.constant.GenAiConstants.MODEL_TYPE_EMBEDDING;
import static com.mindolph.genai.GenaiUiConstants.*;

/**
 * Include choice boxes for LLM providers and their models.
 *
 * @since 1.13.0
 */
public class BaseModelProviderPrefPane extends BaseLoadingSavingPrefsPane {

    private static final Logger log = LoggerFactory.getLogger(BaseModelProviderPrefPane.class);

    public BaseModelProviderPrefPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    /**
     *
     * @param cbProvider
     * @param cbLanguage
     * @param cbModel
     */
    protected void initEmbeddingModelRelatedComponents(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider,
                                                       ChoiceBox<Pair<String, String>> cbLanguage,
                                                       ChoiceBox<Pair<String, ModelMeta>> cbModel) {
        this.initModelRelatedComponents(cbProvider, cbLanguage, cbModel, MODEL_TYPE_EMBEDDING);
    }

    /**
     *
     * @param cbProvider
     * @param cbModel
     */
    protected void initChatModelRelatedComponents(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider,
                                                  ChoiceBox<Pair<String, ModelMeta>> cbModel) {
        this.initModelRelatedComponents(cbProvider, null, cbModel, MODEL_TYPE_CHAT);
    }

    /**
     * @param cbProvider
     * @param cbLanguage
     * @param cbModel
     * @param type       1 is chat model, 2 is embedding model, see {@link GenAiConstants}
     */
    private void initModelRelatedComponents(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider,
                                            ChoiceBox<Pair<String, String>> cbLanguage,
                                            ChoiceBox<Pair<String, ModelMeta>> cbModel,
                                            int type) {
        if (cbLanguage != null) {
            cbLanguage.setConverter(new PairStringStringConverter());
            cbLanguage.getItems().addAll(SUPPORTED_EMBEDDING_LANG.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList());
            cbLanguage.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) return;
                log.debug("Language changed: %s".formatted(newValue));
                if (cbProvider.getSelectionModel().getSelectedItem() != null) {
                    this.updateModelComponent(cbModel, cbProvider.getSelectionModel().getSelectedItem().getKey().name(), MODEL_TYPE_EMBEDDING, newValue.getKey());
                }
                // language changes does not trigger saving since it's used for filtering the models.
                // super.saveChanges();
            });
        }
        cbProvider.setConverter(new ProviderConverter());
        List<GenAiModelProvider> filteredProviders = EnumUtils.getEnumList(GenAiModelProvider.class).stream().filter(provider -> hasModelsForType(provider.name(), type)).toList();
        List<Pair<GenAiModelProvider, String>> providerPairs = filteredProviders.stream().map(p -> new Pair<>(p, p.getDisplayName())).toList();
        cbProvider.getItems().add(null); // none select
        cbProvider.getItems().addAll(providerPairs);
        cbProvider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                log.debug("Unselect provider");
                cbModel.getItems().clear();
                super.saveChanges();
                return;
            }
            String providerName = newValue.getKey().name();
            log.debug("Provider changed: %s".formatted(providerName));
            Pair<String, String> lang = cbLanguage == null ? null : cbLanguage.getSelectionModel().getSelectedItem();
            this.updateModelComponent(cbModel, providerName, type, lang == null ? null : lang.getKey());
            super.saveChanges();
        });
        cbModel.setConverter(new ModelMetaConverter());
        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (super.isLoading()) return;
            if (newValue == null) {
                log.debug("Unselect model");
                super.saveChanges();
                return;
            }
            ModelMeta selectedModel = newValue.getValue();
            if (selectedModel.isInternal() && selectedModel.getType() == MODEL_TYPE_EMBEDDING && StringUtils.isNotBlank(selectedModel.getDownloadUrl())) {
                log.debug("selected model is local embedding model: %s".formatted(selectedModel.getName()));
                String langCode = cbLanguage.getSelectionModel().getSelectedItem().getKey();
                // require download
                if (!LocalModelManager.getIns().doesModelExists(langCode, selectedModel.getName())) {
                    if (DialogFactory.yesNoConfirmDialog("Download model",
                            "Model files are required for selected embedding model %s, do you want to download those files?".formatted(selectedModel.getName()))) {
                        SimpleProgressDialog progressDialog = new SimpleProgressDialog(this.getScene().getWindow(), "Downloading", "Downloading model %s".formatted(selectedModel.getName()));
                        Future<?> future = GlobalExecutor.submit(() -> {
                            try {
                                boolean success = LocalModelManager.getIns().downloadModel(langCode, selectedModel);
                                Platform.runLater(() -> {
                                    if (success) {
                                        DialogFactory.infoDialog("Download success");
                                    }
                                    else {
                                        // deleted incomplete downloads
                                        if (LocalModelManager.getIns().clearModel(langCode, selectedModel.getName())) {
                                            log.info("Model files are deleted.");
                                        }
                                        DialogFactory.errDialog("Download fail");
                                    }
                                    progressDialog.close();
                                });
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        });

                        progressDialog.show(result -> {
                            log.debug(result);
                            if (result == null) {
                                future.cancel(true);
                            }
                        });
                    }
                }
            }
            super.saveChanges();
        });
    }

    // update the model component by provider and language choices.
    protected void updateModelComponent(ChoiceBox<Pair<String, ModelMeta>> cbModel, String providerName, int modelType, String langCode) {
        Collection<ModelMeta> preDefinedModels = GenAiConstants.getFilteredPreDefinedModels(providerName, modelType);

        if (StringUtils.isNotBlank(langCode)) {
            preDefinedModels = preDefinedModels.stream().filter(mm -> mm.getLangCode().equals(langCode)).toList();
        }

        cbModel.getItems().clear();
        cbModel.getItems().add(null); // none select
        if (CollectionUtils.isNotEmpty(preDefinedModels)) {
            log.debug("Found %d predefined models for provider %s and type %s".formatted(preDefinedModels.size(), providerName, modelType));
            cbModel.getItems().addAll(preDefinedModels.stream().map(mm -> new Pair<>(mm.getName(), mm)).sorted(MODEL_COMPARATOR).toList());
        }

        Collection<ModelMeta> customModels = LlmConfig.getIns().getFilteredCustomModels(providerName, modelType);
        if (CollectionUtils.isNotEmpty(customModels)) {
            if (StringUtils.isNotBlank(langCode)) {
                customModels = customModels.stream().filter(mm -> mm.getLangCode().equals(langCode)).toList();
            }
            if (CollectionUtils.isNotEmpty(customModels)) {
                log.debug("Found %d custom models for provider %s and type %s".formatted(customModels.size(), providerName, modelType));
                cbModel.getItems().addAll(customModels.stream().map(mm -> new Pair<>(mm.getName(), mm)).sorted(MODEL_COMPARATOR).toList());
            }
        }
    }

    // Whether there are any specific type of models for the provider
    private boolean hasModelsForType(String providerName, int modelType) {
        return CollectionUtils.isNotEmpty(GenAiConstants.getFilteredPreDefinedModels(providerName, modelType))
                || CollectionUtils.isNotEmpty(LlmConfig.getIns().getFilteredCustomModels(providerName, modelType));
    }

    protected void selectProviderAndModel(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider, ChoiceBox<Pair<String, ModelMeta>> cbModel, String prefKey) {
        if (StringUtils.isNotBlank(prefKey)) {
            Tuple2<GenAiModelProvider, ModelMeta> providerModel = GenAiUtils.parseModelPreference(prefKey);
            if (providerModel != null) {
                ChoiceUtils.selectOrUnselectProvider(cbProvider, providerModel.a());
                ChoiceUtils.selectOrUnselectModel(cbModel, providerModel.b().getName());
            }
        }
    }

    protected void saveProviderAndModelSelection(String prefKey,
                                                 MChoiceBox<Pair<GenAiModelProvider, String>> cbProvider,
                                                 MChoiceBox<Pair<String, ModelMeta>> cbModel) {
        if (!cbProvider.hasSelected() || !cbModel.hasSelected()) {
            super.fxPreferences.removePreference(prefKey);
            return;
        }
        super.fxPreferences.savePreference(prefKey,
                "%s:%s".formatted(cbProvider.getSelectionModel().getSelectedItem().getKey().name(), cbModel.getSelectionModel().getSelectedItem().getValue().getName()));
    }


    @Override
    protected void onSave(boolean notify) {
        if (notify) {
            if (this instanceof GenAiOptionPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.OPTIONS_PREF_CHANGED);
            }
        }
    }
}
