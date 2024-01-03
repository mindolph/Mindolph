package com.mindolph.fx.preference;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.mfx.preference.FxPreferences;
import com.sun.javafx.scene.control.IntegerField;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class GeneralPreferencesPane extends BasePrefsPane implements Initializable {

    private static final String PLANT_UML = "Plant UML";
    private static final String MARKDOWN = "Markdown";
    @FXML
    private CheckBox cbConfirmBeforeQuitting;
    @FXML
    private CheckBox cbOpenLastFiles;
    //    @FXML
//    private CheckBox ckbEnableAutoCreateProjectFolder;
    @FXML
    private CheckBox ckbShowHiddenFiles;
    //    @FXML
//    private CheckBox ckbAutoBackupLastEdit;
    @FXML
    private TableView<OrientationItem> tvOrientation;
    @FXML
    private CheckBox cbEnableInputHelper;

    @FXML
    private ChoiceBox<Pair<GenAiModelProvider, String>> cbAiProvider;
    @FXML
    private TextField tfApiKey;
    @FXML
    private TextField tfAiModel;

    @FXML
    private CheckBox cbEnableProxy;
    @FXML
    private RadioButton rbHttp;
    @FXML
    private RadioButton rbSocks;
    @FXML
    private TextField tfProxyHost;
    @FXML
    private IntegerField tfProxyPort;
    @FXML
    private TextField tfProxyUsername;
    @FXML
    private PasswordField pfProxyPassword;


    public GeneralPreferencesPane() {
        super("/preference/general_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.bindPreference(cbConfirmBeforeQuitting.selectedProperty(), PrefConstants.GENERAL_CONFIRM_BEFORE_QUITTING, true);
        super.bindPreference(cbOpenLastFiles.selectedProperty(), PrefConstants.GENERAL_OPEN_LAST_FILES, true);
//        super.bindPreference(ckbEnableAutoCreateProjectFolder.selectedProperty(), PrefConstants.GENERAL_KNOWLEDGE_FOLDER_GENERATION_ALLOWED, false);
        super.bindPreference(ckbShowHiddenFiles.selectedProperty(), PrefConstants.GENERAL_SHOW_HIDDEN_FILES, false);
//        super.bindPreference(ckbAutoBackupLastEdit.selectedProperty(), "general.autoBackupBeforeSaving", true);

        TableColumn<OrientationItem, Object> colEditor = new TableColumn<>("Editor");
        TableColumn<OrientationItem, Object> colOrientation = new TableColumn<>("Orientation");
        colEditor.setSortable(false);
        colEditor.setEditable(false);
        colEditor.setPrefWidth(120);
        colOrientation.setSortable(false);
        colOrientation.setEditable(true);
        colOrientation.setPrefWidth(180);

        colEditor.setCellValueFactory(param -> {
            Label label = new Label(param.getValue().editor());
            label.setPadding(new Insets(4));
            return new SimpleObjectProperty<>(label);
        });

        colOrientation.setCellValueFactory(param -> {
            ChoiceBox<Pair<Orientation, String>> choiceBoxCell = new ChoiceBox<>();
            choiceBoxCell.setMinWidth(120);
            choiceBoxCell.setPadding(new Insets(4));
            choiceBoxCell.setConverter(new StringConverter<>() {
                @Override
                public String toString(Pair<Orientation, String> object) {
                    return (object == null) ? "" : object.getValue();
                }

                @Override
                public Pair<Orientation, String> fromString(String string) {
                    return null;
                }
            });
            choiceBoxCell.getItems().addAll(Arrays.stream(Orientation.values()).map(e -> new Pair<>(e, e.toString())).toList());
            choiceBoxCell.getSelectionModel().select(new Pair<>(param.getValue().orientation, param.getValue().orientation.toString()));
            choiceBoxCell.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (PLANT_UML.equals(param.getValue().editor)) {
                    fxPreferences.savePreference(GENERAL_EDITOR_ORIENTATION_PUML, newValue.getKey());
                }
                else if (MARKDOWN.equals(param.getValue().editor)) {
                    fxPreferences.savePreference(GENERAL_EDITOR_ORIENTATION_MD, newValue.getKey());
                }
            });
            return new SimpleObjectProperty<>(choiceBoxCell);
        });
        tvOrientation.getColumns().add(colEditor);
        tvOrientation.getColumns().add(colOrientation);
        FxPreferences fxPreferences = FxPreferences.getInstance();
        tvOrientation.getItems().add(new OrientationItem(PLANT_UML, fxPreferences.getPreference(GENERAL_EDITOR_ORIENTATION_PUML, Orientation.class, Orientation.VERTICAL)));
        tvOrientation.getItems().add(new OrientationItem(MARKDOWN, fxPreferences.getPreference(GENERAL_EDITOR_ORIENTATION_MD, Orientation.class, Orientation.HORIZONTAL)));
        super.bindPreference(cbEnableInputHelper.selectedProperty(), PrefConstants.GENERAL_EDITOR_ENABLE_INPUT_HELPER, true);

        // Gen AI
        cbAiProvider.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<GenAiModelProvider, String> object) {
                return object.getValue();
            }

            @Override
            public Pair<GenAiModelProvider, String> fromString(String string) {
                return null;
            }
        });
        cbAiProvider.getItems().add(new Pair<>(GenAiModelProvider.OPEN_AI, GenAiModelProvider.OPEN_AI.getName()));
        cbAiProvider.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fxPreferences.savePreference(PrefConstants.GENERAL_AI_PROVIDER_ACTIVE, newValue.getKey().getName());
            Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
            if (map.containsKey(newValue.getKey().getName())) {
                ProviderProps vendorProps = map.get(newValue.getKey().getName());
                if (vendorProps != null) {
                    tfApiKey.setText(vendorProps.apiKey());
                    tfAiModel.setText(vendorProps.aiModel());
                }
                else {
                    tfApiKey.setText("");
                    tfAiModel.setText("");
                }
            }
        });
        cbAiProvider.setValue(new Pair<>(GenAiModelProvider.OPEN_AI, GenAiModelProvider.OPEN_AI.getName()));
        tfApiKey.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(newValue, tfAiModel.getText());
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
        });
        tfAiModel.textProperty().addListener((observable, oldValue, newValue) -> {
            ProviderProps vendorProps = new ProviderProps(tfApiKey.getText(), newValue);
            LlmConfig.getIns().saveGenAiProvider(cbAiProvider.getValue().getKey(), vendorProps);
        });

        // proxy
        cbEnableProxy.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeUtils.enable(rbHttp, rbSocks, tfProxyHost, tfProxyPort, tfProxyUsername, pfProxyPassword);
            }
            else {
                NodeUtils.disable(rbHttp, rbSocks, tfProxyHost, tfProxyPort, tfProxyUsername, pfProxyPassword);
            }
            this.save(true);
        });
        ToggleGroup group = new ToggleGroup();
        rbHttp.setToggleGroup(group);
        rbSocks.setToggleGroup(group);
        rbHttp.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fxPreferences.savePreference(GENERAL_PROXY_TYPE, "HTTP");
                this.save(true);
            }
        });
        rbSocks.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fxPreferences.savePreference(GENERAL_PROXY_TYPE, "SOCKS");
                this.save(true);
            }
        });
        super.bindPreference(cbEnableProxy.selectedProperty(), PrefConstants.GENERAL_PROXY_ENABLE, false);
//        super.bindPreference(rbHttp.selectedProperty(), PrefConstants.GENERAL_PROXY_TYPE, true);
//        super.bindPreference(rbSocks.selectedProperty(), PrefConstants.GENERAL_PROXY_TYPE, false);
        super.bindPreference(tfProxyHost.textProperty(), PrefConstants.GENERAL_PROXY_HOST, "");
        super.bindPreference(tfProxyPort.valueProperty(), PrefConstants.GENERAL_PROXY_PORT, 0);
        super.bindPreference(tfProxyUsername.textProperty(), PrefConstants.GENERAL_PROXY_USERNAME, "");
        super.bindPreference(pfProxyPassword.textProperty(), PrefConstants.GENERAL_PROXY_PASSWORD, "");
    }

    @Override
    public void loadPreferences() {
        super.loadPreferences();
    }

    @Override
    protected void save(boolean notify) {
        if (notify)
            PluginEventBus.getIns().emitPreferenceChanges();
    }

    private record OrientationItem(String editor, Orientation orientation) {
    }
}
