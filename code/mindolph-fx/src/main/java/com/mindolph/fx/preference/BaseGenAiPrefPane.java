package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.apache.commons.lang3.EnumUtils;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.genai.GenaiUiConstants.*;

/**
 * @since unknown
 */
public abstract class BaseGenAiPrefPane extends BasePrefsPane implements Initializable {
    @FXML
    protected ChoiceBox<Pair<GenAiModelProvider, String>> cbModelProvider;
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbModel;

    protected FxPreferences fxPreferences;

    // pause to saving data during loading data.
    protected AtomicBoolean isLoading;

    public BaseGenAiPrefPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxPreferences = FxPreferences.getInstance();
        isLoading = new AtomicBoolean(false);
    }

    /**
     * @param type 1 is chat model, 2 is embedding model.
     */
    protected void initProvidersAndModels(int type) {
        cbModelProvider.setConverter(modelProviderConverter);
        List<Pair<GenAiModelProvider, String>> providerPairs = EnumUtils.getEnumList(GenAiModelProvider.class).stream().map(p -> new Pair<>(p, p.getName())).toList();
        cbModelProvider.getItems().addAll(providerPairs);
        cbModelProvider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            String providerName = newValue.getKey().getName();
//            LlmConfig.getIns().preferredModelForActiveLlmProvider();
            Collection<ModelMeta> preDefinedModels = PROVIDER_MODELS.get(providerName)
                    .stream().filter(mm -> mm.getType() == type).toList();
            cbModel.getItems().clear();
            cbModel.getItems().addAll(preDefinedModels.stream().map(mm -> new Pair<>(mm.name(), mm)).sorted(MODEL_COMPARATOR).toList());
            ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(providerName);
            if (providerMeta != null) {
                List<ModelMeta> customModels = providerMeta.customModels();
                if (customModels != null) {
                    customModels = customModels.stream().filter(mm -> mm.getType() == type).toList();
                    if (!customModels.isEmpty()) {
                        cbModel.getItems().addAll(customModels.stream().map(mm -> new Pair<>(mm.name(), mm)).sorted(MODEL_COMPARATOR).toList());
                    }
                }
            }
            onModelChange();
        });
        cbModel.setConverter(modelMetaConverter);
        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            onModelChange();
        });
    }

    /**
     * Invoked when model selection is changed.
     */
    protected abstract void onModelChange();

    protected void selectModel(GenAiModelProvider providerType, ModelMeta modelMeta) {
        if (providerType != null) {
            cbModelProvider.getSelectionModel().select(new Pair<>(providerType, providerType.getName()));
        }
        else {
            cbModelProvider.getSelectionModel().clearSelection();
        }
        if (modelMeta != null) {
            cbModel.getSelectionModel().select(new Pair<>(modelMeta.name(), modelMeta));
        }
        else {
            cbModel.getSelectionModel().clearSelection();
        }
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify) {
            if (this instanceof GenAiAgentPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.AGENT_PREF_CHANGED);
            }
        }
    }
}
