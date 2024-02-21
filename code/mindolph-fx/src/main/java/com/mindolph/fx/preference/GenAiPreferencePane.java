package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.Constants.ProviderProps;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.GENERAL_AI_PROVIDER_ACTIVE;
import static com.mindolph.base.constant.PrefConstants.GENERAL_AI_TIMEOUT;
import static com.mindolph.core.constant.GenAiModelProvider.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.1
 */
public class GenAiPreferencePane extends BasePrefsPane implements Initializable {

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbAiProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfAiModel;
    @FXML
    private Spinner<Integer> spTimeOut;

    public GenAiPreferencePane() {
        super("/preference/gen_ai_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Gen AI
        cbAiProvider.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<GenAiModelProvider, String> object) {
                return object == null ? "" : object.getValue();
            }

            @Override
            public Pair<GenAiModelProvider, String> fromString(String string) {
                return null;
            }
        });
        cbAiProvider.getItems().add(new Pair<>(OPEN_AI, OPEN_AI.getName()));
//        cbAiProvider.getItems().add(new Pair<>(GEMINI, GEMINI.getName()));
        super.bindPreference(cbAiProvider.valueProperty(), GENERAL_AI_PROVIDER_ACTIVE, OPEN_AI.getName(),
                pair -> pair.getKey().getName(),
                providerName -> new Pair<>(fromName(providerName), providerName),
                selected -> {
                    Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
                    ProviderProps vendorProps = map.get(selected.getKey().getName());
                    if (vendorProps != null) {
                        tfApiKey.setText(vendorProps.apiKey());
                        tfAiModel.setText(vendorProps.aiModel());
                    }
                    else {
                        tfApiKey.setText("");
                        tfAiModel.setText("");
                    }
                });

        // Dynamic preference can't use bindPreference.
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(newValue, tfAiModel.getText());
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        // Dynamic preference can't use bindPreference.
        tfAiModel.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(tfApiKey.getText(), newValue);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        super.bindSpinner(spTimeOut, 1, 300, 1, GENERAL_AI_TIMEOUT, 60);

    }

    @Override
    protected void onSave(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges();
    }
}
