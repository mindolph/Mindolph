package com.mindolph.fx.preference;

import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.genai.ChoiceUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.*;

/**
 * @since unknown
 */
public class GenAiOptionPrefPane extends BaseModelProviderPrefPane implements Initializable {

    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguages;
    @FXML
    private Spinner<Integer> spTimeOut;

    @FXML
    protected ChoiceBox<Pair<GenAiModelProvider, String>> cbProviderGenerate;
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbModelGenerate;

    @FXML
    protected ChoiceBox<Pair<GenAiModelProvider, String>> cbProviderSummarize;
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbModelSummarize;

    public GenAiOptionPrefPane() {
        super("/preference/gen_ai_option_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbLanguages.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesTo(cbLanguages);
        cbLanguages.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            fxPreferences.savePreference(GEN_AI_OUTPUT_LANGUAGE, newValue.getKey());
        });
        // time out setting for all.
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);

        super.initProviderAndModelComponents(this.cbProviderGenerate, this.cbModelGenerate, GenAiConstants.MODEL_TYPE_CHAT);
        super.initProviderAndModelComponents(this.cbProviderSummarize, this.cbModelSummarize, GenAiConstants.MODEL_TYPE_CHAT);

        super.selectProviderAndModel(this.cbProviderGenerate, this.cbModelGenerate, GEN_AI_GENERATE_MODEL);
        super.selectProviderAndModel(this.cbProviderSummarize, this.cbModelSummarize, GEN_AI_SUMMARIZE_MODEL);

        this.cbProviderGenerate.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        this.cbModelGenerate.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        this.cbProviderSummarize.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        this.cbModelSummarize.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
    }

    @Override
    protected void onSave(boolean notify) {
        super.saveProviderAndModelSelection(GEN_AI_GENERATE_MODEL, cbProviderGenerate, cbModelGenerate);
        super.saveProviderAndModelSelection(GEN_AI_SUMMARIZE_MODEL, cbProviderSummarize, cbModelSummarize);
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.GLOBAL);
    }
}
