package com.mindolph.fx.preference;

import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.genai.ChoiceUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @since unknown
 */
public abstract class BaseGenAiPrefPane extends BaseModelProviderPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BaseGenAiPrefPane.class);

    // for Embedding model
    @FXML
    protected ChoiceBox<Pair<GenAiModelProvider, String>> cbEmbeddingProvider;
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbEmbeddingModel;

    public BaseGenAiPrefPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    protected void selectEmbeddingProviderAndModel(GenAiModelProvider providerType, ModelMeta modelMeta) {
        ChoiceUtils.selectOrUnselectProvider(this.cbEmbeddingProvider, providerType);
        ChoiceUtils.selectOrUnselectModel(this.cbEmbeddingModel, modelMeta);
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify) {
            if (this instanceof GenAiAgentPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.AGENT_PREF_CHANGED);
            }
            else if (this instanceof GenAiDatasetPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.DATASET_PREF_CHANGED);
            }
        }
    }
}
