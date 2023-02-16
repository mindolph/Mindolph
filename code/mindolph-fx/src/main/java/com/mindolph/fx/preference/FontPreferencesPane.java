package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.dialog.FontSelectDialog;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.util.FontUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.FontConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class FontPreferencesPane extends BasePrefsPane implements Initializable {

    public FontPreferencesPane() {
        super("/preference/font_preferences_pane.fxml");
    }

    @FXML
    private ChoiceBox<Pair<String, String>> cbText;
    @FXML
    private Label lbFont;
    @FXML
    private TextArea taPreview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbText.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, String> p) {
                return p.getValue();
            }

            @Override
            public Pair<String, String> fromString(String s) {
                return null;
            }
        });
        cbText.valueProperty().addListener((observableValue, old, newChoice) -> {
            Font font = fxPreferences.getPreference(newChoice.getKey(), Font.class, DEFAULT_FONTS.get(newChoice.getKey()));
            lbFont.setText(FontUtils.fontToString(font));
            taPreview.setFont(font);
        });
        cbText.getItems().addAll(Arrays.asList(
                new Pair<>(KEY_MMD_TOPIC_FONT, "Mind Map Topic"),
                new Pair<>(KEY_MMD_NOTE_FONT, "Mind Map Note Editor"),
                new Pair<>(KEY_PUML_EDITOR, "PlantUML Editor"),
                new Pair<>(KEY_MD_EDITOR, "Markdown Editor"),
                new Pair<>(KEY_TXT_EDITOR, "Plain Text Editor")
        ));
        cbText.setValue(new Pair<>(KEY_MMD_TOPIC_FONT, "Mind Map Topic"));
    }

    @FXML
    private void onChange(ActionEvent event) {
        Font changedFont = new FontSelectDialog(taPreview.getFont()).showAndWait();
        if (changedFont != null) {
            lbFont.setText(FontUtils.fontToString(changedFont));
            taPreview.setFont(changedFont);
            fxPreferences.savePreference(cbText.getSelectionModel().getSelectedItem().getKey(), changedFont);
            this.save();
        }
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();
        for (Pair<String, String> item : cbText.getItems()) {
            Font defaultFont = DEFAULT_FONTS.get(item.getKey());
            fxPreferences.savePreference(item.getKey(), defaultFont);
            // update the preview for selected.
            if (item == cbText.getSelectionModel().getSelectedItem()) {
                taPreview.setFont(defaultFont);
            }
        }
        this.save();
        fxPreferences.flush();
    }

    @Override
    protected void save() {
        preferenceChangedEventHandler.onPreferenceChanged(SupportFileTypes.TYPE_PLAIN_TEXT);
    }
}
