package com.mindolph.fx.preference;

import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEvent;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.VectorStoreMeta;
import com.mindolph.genai.ChoiceUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.*;
import static com.mindolph.core.constant.VectorStoreProvider.PG_VECTOR;
import static com.mindolph.genai.GenaiUiConstants.vectorStoreConverter;

/**
 * @since unknown
 */
public class GenAiOptionPrefPane extends BaseModelProviderPrefPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(GenAiOptionPrefPane.class);
    @FXML
    private ChoiceBox<Pair<String, String>> cbLanguages;
    @FXML
    private Spinner<Integer> spTimeOut;

    @FXML
    protected ChoiceBox<Pair<VectorStoreProvider, String>> cbVectorStoreProvider;
    @FXML
    private TextField tfHost;
    @FXML
    private Spinner<Integer> spPort;
    @FXML
    private TextField tfDatabase;
    @FXML
    private TextField tfUsername;
    @FXML
    private TextField tfPassword;

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
        ChoiceUtils.loadLanguagesToAndSelectDefault(cbLanguages);
        cbLanguages.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;
            fxPreferences.savePreference(GEN_AI_OUTPUT_LANGUAGE, newValue.getKey());
        });
        // time out setting for all.
        super.bindSpinner(spTimeOut, 1, 300, 1, GEN_AI_TIMEOUT, 60);
        // Vector store
        this.cbVectorStoreProvider.setConverter(vectorStoreConverter);
        this.cbVectorStoreProvider.getItems().clear();
        this.cbVectorStoreProvider.getItems().add(new Pair<>(PG_VECTOR, PG_VECTOR.getDisplayName()));
        super.bindPreference(cbVectorStoreProvider.valueProperty(), GEN_AI_VECTOR_STORE_PROVIDER_ACTIVE, null,
                pair -> pair.getKey().name(),
                s -> new Pair<>(VectorStoreProvider.valueOf(s), VectorStoreProvider.valueOf(s).getDisplayName()),
                pair -> {
                    VectorStoreProvider provider = pair.getKey();
                    log.debug("Load vector store provider: {}", provider.name());
                    NodeUtils.enable(tfHost, spPort, tfDatabase, tfUsername, tfPassword);
                    VectorStoreMeta vectorStoreMeta = LlmConfig.getIns().loadVectorStorePrefs(provider);
                    if (vectorStoreMeta != null) {
                        tfHost.setText(vectorStoreMeta.getHost());
                        spPort.setValueFactory(new IntegerSpinnerValueFactory(1, 65535, vectorStoreMeta.getPort() == null ? 0 : vectorStoreMeta.getPort()));
                        tfDatabase.setText(vectorStoreMeta.getDatabase());
                        tfUsername.setText(vectorStoreMeta.getUsername());
                        tfPassword.setText(vectorStoreMeta.getPassword());
                    }
                });
        tfHost.textProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        spPort.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        tfDatabase.textProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        tfUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });
        tfPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            this.saveChanges();
        });

        super.initChatModelComponents(this.cbProviderGenerate, this.cbModelGenerate);
        super.initChatModelComponents(this.cbProviderSummarize, this.cbModelSummarize);

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
        super.loadPreferences();
    }

    private void saveVectorStoragePrefs() {
        Pair<VectorStoreProvider, String> selected = cbVectorStoreProvider.getSelectionModel().getSelectedItem();
        if (selected != null) {
            VectorStoreProvider provider = cbVectorStoreProvider.getSelectionModel().getSelectedItem().getKey();
            VectorStoreMeta vectorStoreMeta = new VectorStoreMeta();
            vectorStoreMeta.setHost(tfHost.getText());
            vectorStoreMeta.setPort(spPort.getValue() == null ? null : spPort.getValue());
            vectorStoreMeta.setDatabase(tfDatabase.getText());
            vectorStoreMeta.setUsername(tfUsername.getText());
            vectorStoreMeta.setPassword(tfPassword.getText());
            LlmConfig.getIns().saveVectorStorePrefs(provider, vectorStoreMeta);
        }
    }

    @Override
    protected void onSave(boolean notify) {
        log.debug("On gen-ai options saving");
        this.saveVectorStoragePrefs();
        super.saveProviderAndModelSelection(GEN_AI_GENERATE_MODEL, cbProviderGenerate, cbModelGenerate);
        super.saveProviderAndModelSelection(GEN_AI_SUMMARIZE_MODEL, cbProviderSummarize, cbModelSummarize);
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges(PluginEvent.EventType.GLOBAL);
    }
}
