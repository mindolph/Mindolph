package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
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
public class AiPreferencePane extends BasePrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(AiPreferencePane.class);

    @FXML
    private TabPane tpGenAi;

    @FXML
    private Tab tabAgents;
    @FXML
    private Tab tabProviders;
    @FXML
    private Tab tabDataset;
    @FXML
    private Tab tabOptions;

    private static int latestActivePaneIndex = 0;

    public AiPreferencePane() {
        super("/preference/gen_ai_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tpGenAi.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
            switch (newIdx.intValue()) {
                case 0:
                    if (tabAgents.getContent() == null) tabAgents.setContent(new AiAgentPrefPane());
                    break;
                case 1:
                    if (tabProviders.getContent() == null) tabProviders.setContent(new AiProviderPrefPane());
                    break;
                case 2:
                    if (tabDataset.getContent() == null) tabDataset.setContent(new AiDatasetPrefPane());
                    break;
                case 3:
                    if (tabOptions.getContent() == null) tabOptions.setContent(new AiOptionPrefPane());
                    break;
            }
            latestActivePaneIndex = newIdx.intValue();
        });
        // load first tab
        tabAgents.setContent(new AiAgentPrefPane());
    }

    @Override
    protected void onSave(boolean notify) {
        // DO NOTHING
    }
}
