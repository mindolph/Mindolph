package com.mindolph.fx.preference;

import com.mindolph.base.plugin.PluginEvent.EventType;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.mfx.control.MChoiceBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @since 1.13.0
 */
public abstract class BaseAiPrefPane extends BaseModelProviderPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BaseAiPrefPane.class);

    // for Embedding model
    @FXML
    protected MChoiceBox<Pair<GenAiModelProvider, String>> cbEmbeddingProvider;
    @FXML
    protected MChoiceBox<Pair<String, ModelMeta>> cbEmbeddingModel;

    public BaseAiPrefPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }

    /**
     *
     * @param providerType
     * @param modelMeta
     */
    protected void selectEmbeddingProviderAndModel(GenAiModelProvider providerType, String modelMeta) {
        ChoiceUtils.selectOrUnselectProvider(this.cbEmbeddingProvider, providerType);
        ChoiceUtils.selectOrUnselectModel(this.cbEmbeddingModel, modelMeta);
    }

    protected void unselectEmbeddingProviderAndModel() {
        ChoiceUtils.selectOrUnselectProvider(this.cbEmbeddingProvider, null);
        ChoiceUtils.selectOrUnselectModel(this.cbEmbeddingModel, null);
    }

    @Override
    protected void onSave(boolean notify) {
        super.onSave(notify);
        if (notify) {
            if (this instanceof AiAgentPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(EventType.AGENT_PREF_CHANGED);
            }
            else if (this instanceof AiDatasetPrefPane) {
                PluginEventBus.getIns().emitPreferenceChanges(EventType.DATASET_PREF_CHANGED);
            }
        }
    }
}
