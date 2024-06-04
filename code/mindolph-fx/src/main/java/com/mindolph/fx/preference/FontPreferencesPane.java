package com.mindolph.fx.preference;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.dialog.FontSelectDialog;
import com.mindolph.base.event.EventBus;
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
import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 */
public class FontPreferencesPane extends BasePrefsPane implements Initializable {

    public FontPreferencesPane() {
        super("/preference/font_preferences_pane.fxml");
    }

    @FXML
    private ChoiceBox<Pair<PrefKey, String>> cbText;
    @FXML
    private Label lbFont;
    @FXML
    private TextArea taPreview;

    private static int lastActivatedChoiceIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        cbText.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<PrefKey, String> p) {
                return p.getValue();
            }

            @Override
            public Pair<PrefKey, String> fromString(String s) {
                return null;
            }
        });
        cbText.valueProperty().addListener((observableValue, old, newChoice) -> {
            Font font = fxPreferences.getPreference(newChoice.getKey().prefId(), Font.class, DEFAULT_FONTS.get(newChoice.getKey().prefId()));
            lbFont.setText(FontUtils.fontToString(font));
            taPreview.setFont(font);
            lastActivatedChoiceIndex = cbText.getItems().indexOf(newChoice);
        });
        cbText.getItems().addAll(Arrays.asList(
                new Pair<>(new PrefKey(KEY_MMD_TOPIC_FONT, TYPE_MIND_MAP), "Mind Map Topic"),
                new Pair<>(new PrefKey(KEY_MMD_NOTE_FONT, TYPE_MIND_MAP), "Mind Map Note Editor"),
                new Pair<>(new PrefKey(KEY_MMD_NOTE_FONT_MONO, TYPE_MIND_MAP), "Mind Map Note Editor Code"),
                new Pair<>(new PrefKey(KEY_PUML_EDITOR_MONO, TYPE_PLANTUML), "PlantUML Editor"),
                new Pair<>(new PrefKey(KEY_PUML_EDITOR, TYPE_PLANTUML), "PlantUML Editor Descriptive&Comment"),
                new Pair<>(new PrefKey(KEY_MD_EDITOR, TYPE_MARKDOWN), "Markdown Editor"),
                new Pair<>(new PrefKey(KEY_MD_EDITOR_MONO, TYPE_MARKDOWN), "Markdown Code"),
                new Pair<>(new PrefKey(KEY_TXT_EDITOR, TYPE_PLAIN_TEXT), "Plain Text Editor"),
                new Pair<>(new PrefKey(KEY_CSV_EDITOR, TYPE_PLAIN_TEXT), "CSV Editor")
        ));
        cbText.getSelectionModel().select(lastActivatedChoiceIndex);
    }

    @FXML
    private void onChange(ActionEvent event) {
        Font changedFont = new FontSelectDialog(taPreview.getFont()).showAndWait();
        if (changedFont != null) {
            lbFont.setText(FontUtils.fontToString(changedFont));
            taPreview.setFont(changedFont);
            fxPreferences.savePreference(cbText.getSelectionModel().getSelectedItem().getKey().prefId(), changedFont);
            this.onSave(true);
        }
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();
        for (Pair<PrefKey, String> item : cbText.getItems()) {
            Font defaultFont = DEFAULT_FONTS.get(item.getKey().prefId());
            fxPreferences.savePreference(item.getKey().prefId(), defaultFont);
            // update the preview for selected.
            if (item == cbText.getSelectionModel().getSelectedItem()) {
                taPreview.setFont(defaultFont);
            }
        }
        this.onSave(true);
        fxPreferences.flush();
    }

    @Override
    protected void onSave(boolean notify) {
        String fileType = cbText.getSelectionModel().getSelectedItem().getKey().fileType();
        if (notify) {
            EventBus.getIns().notifyPreferenceChanged(fileType);
        }
    }

    /**
     * Preference key in choice box.
     */
    private record PrefKey(String prefId, String fileType) {
    }

}
