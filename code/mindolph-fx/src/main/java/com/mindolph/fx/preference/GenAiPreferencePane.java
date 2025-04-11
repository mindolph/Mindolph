package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 * @see GenAiConstants
 * @see GenAiModelProvider
 * @since 1.7.1
 */
public class GenAiPreferencePane extends BasePrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiPreferencePane.class);

    @FXML
    private TabPane tpGenAi;

    @FXML
    private GenAiModelPrefPane genAiModelPrefPane;

    @FXML
    private GenAiAgentPrefPane genAiAgentPrefPane;

    @FXML
    private GenAiDatasetPrefPane genAiDatasetPrefPane;

    @FXML
    private GenAiOptionPrefPane genAiOptionPrefPane;

    private static int latestActivePaneIndex = 0;

    public GenAiPreferencePane() {
        super("/preference/gen_ai_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tpGenAi.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            latestActivePaneIndex = newValue.intValue();
        });
        tpGenAi.getSelectionModel().select(latestActivePaneIndex);

    }

    @Override
    protected void onSave(boolean notify) {
        // DO NOTHING
    }
}
