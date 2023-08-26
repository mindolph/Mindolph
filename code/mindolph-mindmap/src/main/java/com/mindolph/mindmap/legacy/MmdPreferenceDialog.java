package com.mindolph.mindmap.legacy;

import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.preference.MmdPreferencesPane;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.NotImplementedException;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class MmdPreferenceDialog extends BaseDialogController<MindMapConfig> {

    @FXML
    private MmdPreferencesPane mmdPreferences;

    public MmdPreferenceDialog() {
            dialog = new CustomDialogBuilder<MindMapConfig>()
                    .owner(DialogFactory.DEFAULT_WINDOW)
                    .title("Preferences of Mind Map")
                    .fxmlUri("dialog/mmd_preferences_dialog.fxml")
                    .buttons(ButtonType.CLOSE)
                    .defaultValue(null)
                    .controller(MmdPreferenceDialog.this)
                    .build();
            mmdPreferences.loadPreferences();
    }

    public MindMapConfig show() {
        // disable this show()
        throw new NotImplementedException("Invalid form " + this.getClass().getName());
    }
}
