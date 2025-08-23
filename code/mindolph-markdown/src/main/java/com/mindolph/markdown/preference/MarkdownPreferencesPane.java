package com.mindolph.markdown.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.*;

public class MarkdownPreferencesPane extends BasePrefsPane implements Initializable {

    @FXML
    private TextField tfSansFontFile;
    @FXML
    private Button btnSelectSans;
    @FXML
    private TextField tfMonoFontFile;
    @FXML
    private Button btnSelectMono;

    public MarkdownPreferencesPane() {
        super("/preference/markdown_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        String sansFontFilePath = fxPreferences.getPreference(PREF_KEY_MD_SANS_FONT_FILE, String.class);
        String monoFontFilePath = fxPreferences.getPreference(PREF_KEY_MD_MONO_FONT_FILE, String.class);
        File sansFontFile = new File(sansFontFilePath);
        File monoFontFile = new File(monoFontFilePath);
        if (sansFontFile.exists()) {
            tfSansFontFile.setText(sansFontFilePath);
        }
        if (monoFontFile.exists()) {
            tfMonoFontFile.setText(monoFontFilePath);
        }

        tfSansFontFile.textProperty().addListener((observableValue, s, t1) -> {
            onFileChanged(t1);
        });
        tfMonoFontFile.textProperty().addListener((observable, oldValue, newValue) -> {
            onFileChanged(newValue);
        });

        btnSelectSans.setOnAction(actionEvent -> {
            File defaultDir = sansFontFile.exists() ? sansFontFile.getParentFile() : SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(getScene().getWindow(), defaultDir,
                    new FileChooser.ExtensionFilter("TrueType Font(*.ttf)", "*.ttf"));
            if (file != null && file.exists()) {
                tfSansFontFile.setText(file.getPath());
            }
        });
        btnSelectMono.setOnAction(actionEvent -> {
            File defaultDir = monoFontFile.exists() ? monoFontFile.getParentFile() : SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(getScene().getWindow(), defaultDir,
                    new FileChooser.ExtensionFilter("TrueType Font(*.ttf)", "*.ttf"));
            if (file != null && file.exists()) {
                tfMonoFontFile.setText(file.getPath());
            }
        });
    }

    private void onFileChanged(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            this.onSave(true);
        }
        else {
            File file = new File(filePath);
            if (file.exists()) {
                this.onSave(true);
            }
        }
    }

    @Override
    protected void onSave(boolean notify) {
        fxPreferences.savePreference(PREF_KEY_MD_SANS_FONT_FILE, tfSansFontFile.getText());
        fxPreferences.savePreference(PREF_KEY_MD_MONO_FONT_FILE, tfMonoFontFile.getText());
    }
}
