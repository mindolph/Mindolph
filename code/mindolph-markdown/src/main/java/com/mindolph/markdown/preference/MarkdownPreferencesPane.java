package com.mindolph.markdown.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.PrefConstants.PREF_KEY_MD_FONT_FILE_PDF;

public class MarkdownPreferencesPane extends BasePrefsPane implements Initializable {

    @FXML
    private TextField tfFontFile;
    @FXML
    private Button btnSelect;

    public MarkdownPreferencesPane() {
        super("/preference/markdown_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        String fontFilePath = fxPreferences.getPreference(PREF_KEY_MD_FONT_FILE_PDF, String.class);
        File fontFile = new File(fontFilePath);
        if (fontFile.exists()) {
            tfFontFile.setText(fontFilePath);
        }

        tfFontFile.textProperty().addListener((observableValue, s, t1) -> {
            File file = new File(t1);
            if (file.exists()) {
                this.save(true);
            }
        });

        btnSelect.setOnAction(actionEvent -> {
            File defaultDir = fontFile.exists() ? fontFile.getParentFile() : SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(getScene().getWindow(), defaultDir,
                    new FileChooser.ExtensionFilter("TrueType Font(*.ttf)", "*.ttf"));
            if (file != null && file.exists()) {
                tfFontFile.setText(file.getPath());
            }
        });
    }

    @Override
    protected void save(boolean notify) {
        fxPreferences.savePreference(PREF_KEY_MD_FONT_FILE_PDF, tfFontFile.getText());
    }
}
