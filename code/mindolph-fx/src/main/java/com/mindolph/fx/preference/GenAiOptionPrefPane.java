package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.genai.ChoiceUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_OUTPUT_LANGUAGE;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;

/**
 * @since unknown
 */
public class GenAiOptionPrefPane extends BasePrefsPane implements Initializable {

    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguages;
    @FXML
    private Spinner<Integer> spTimeOut;

    public GenAiOptionPrefPane() {
        super("/preference/gen_ai_option_pref_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbLanguages.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesTo(cbLanguages);
        cbLanguages.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            fxPreferences.savePreference(GEN_AI_OUTPUT_LANGUAGE, newValue.getKey());
        });
        // time out setting for all.
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.GLOBAL);
    }
}
