package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiConstants.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDER_ACTIVE;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;
import static com.mindolph.core.constant.GenAiConstants.providerModels;
import static com.mindolph.core.constant.GenAiModelProvider.*;

/**
 * @author mindolph.com@gmail.com
 * @see com.mindolph.core.constant.GenAiConstants
 * @see GenAiModelProvider
 * @since 1.7.1
 */
public class GenAiPreferencePane extends BasePrefsPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiPreferencePane.class);

    private static final Pair<String, String> MODEL_CUSTOM_ITEM = new Pair<>("Custom", "Custom");

    private static final Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbAiProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfBaseUrl;
    @FXML
    private ChoiceBox<Pair<String, String>> cbModel;
    @FXML
    private TextField tfAiModel;
    @FXML
    private Spinner<Integer> spTimeOut;
    @FXML
    private CheckBox cbUseProxy;


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
        cbAiProvider.getItems().add(new Pair<>(GEMINI, GEMINI.getName()));
        cbAiProvider.getItems().add(new Pair<>(ALI_Q_WEN, ALI_Q_WEN.getName()));
        cbAiProvider.getItems().add(new Pair<>(OLLAMA, OLLAMA.getName()));
        cbAiProvider.getItems().add(new Pair<>(HUGGING_FACE, HUGGING_FACE.getName()));
        cbAiProvider.getItems().add(new Pair<>(CHAT_GLM, CHAT_GLM.getName()));
        super.bindPreference(cbAiProvider.valueProperty(), GEN_AI_PROVIDER_ACTIVE, OPEN_AI.getName(),
                pair -> pair.getKey().getName(),
                providerName -> new Pair<>(fromName(providerName), providerName),
                selected -> {
                    Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
                    GenAiModelProvider provider = selected.getKey();
                    if (provider != null) {
                        if (provider.getType() == ProviderType.PUBLIC) {
                            tfApiKey.setDisable(false);
                            tfBaseUrl.setDisable(true);
                        }
                        else if (provider.getType() == ProviderType.PRIVATE) {
                            tfApiKey.setDisable(true);
                            tfBaseUrl.setDisable(false);
                        }
                        ProviderProps vendorProps = map.get(provider.getName());
                        if (vendorProps == null) {
                            // init for a vendor who was never been setup.
                            vendorProps = new ProviderProps("", "", "", false);
                        }
                        tfApiKey.setText(vendorProps.apiKey());
                        tfBaseUrl.setText(vendorProps.baseUrl());
                        tfAiModel.setText(vendorProps.aiModel());
                        cbUseProxy.setSelected(vendorProps.useProxy());

                        // Specific disable the proxy support for OLLAMA since the LangChain4j is not supported it yet.
                        cbUseProxy.setDisable(provider == OLLAMA || provider == ALI_Q_WEN);

                        Pair<String, String> targetItem = new Pair<>(vendorProps.aiModel(), vendorProps.aiModel());

                        log.debug("Load models for gen-ai provider: %s".formatted(provider.getName()));
                        for (String m : providerModels.get(provider.getName())) {
                            log.debug("  %s".formatted(m));
                        }

                        List<Pair<String, String>> models = providerModels.get(provider.getName())
                                .stream().map(m -> new Pair<>(m, m)).sorted(MODEL_COMPARATOR).toList();
                        cbModel.getItems().clear();
                        if (models.isEmpty()) {
                            cbModel.getItems().add(MODEL_CUSTOM_ITEM);
                        }
                        else {
                            cbModel.getItems().addAll(models);
                            cbModel.getItems().add(MODEL_CUSTOM_ITEM);
                        }

                        if (cbModel.getItems().contains(targetItem)) {
                            cbModel.getSelectionModel().select(targetItem);
                            tfAiModel.setDisable(true);
                        }
                        else {
                            cbModel.getSelectionModel().select(MODEL_CUSTOM_ITEM);
                            tfAiModel.setDisable(false);
                        }
                    }
                });

        // Dynamic preference can't use bindPreference.
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(newValue, null, tfAiModel.getText(), cbUseProxy.isSelected());
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        // Dynamic preference can't use bindPreference.
        tfBaseUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(null, newValue, tfAiModel.getText(), cbUseProxy.isSelected());
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        cbModel.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, String> object) {
                return object == null ? "" : object.getValue();
            }

            @Override
            public Pair<String, String> fromString(String string) {
                return null;
            }
        });
        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            if (MODEL_CUSTOM_ITEM == newValue) {
                tfAiModel.setDisable(false);
            }
            else {
                tfAiModel.setText(newValue.getValue());
            }
        });
        // Dynamic preference can't use bindPreference.
        tfAiModel.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(tfApiKey.getText(), tfBaseUrl.getText(), newValue, cbUseProxy.isSelected());
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);
        cbUseProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(tfApiKey.getText(), tfBaseUrl.getText(), tfAiModel.getText(), newValue);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
            this.onSave(true);
        });
    }

    @Override
    protected void onSave(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges();
    }
}
