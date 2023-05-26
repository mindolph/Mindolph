package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.PreferenceChangedEventHandler;
import com.mindolph.fx.preference.FontPreferencesPane;
import com.mindolph.fx.preference.GeneralPreferencesPane;
import com.mindolph.markdown.preference.MarkdownPreferencesPane;
import com.mindolph.mfx.container.SideTabPane;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.preference.MmdPreferences;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;

/**
 * All preference dialog for Mindolph.
 *
 * @author mindolph.com@gmail.com
 */
public class PreferencesDialog extends BaseDialogController<Void> {

    @FXML
    private SideTabPane tabPane;

    @FXML
    private GeneralPreferencesPane generalPreferencesPane;

    @FXML
    private FontPreferencesPane fontPreferencesPane;

    @FXML
    private MmdPreferences mmdPreferences;

    @FXML
    private MarkdownPreferencesPane mdPreferences;

//    @FXML
//    private PlantumlPreferences plantumlPreferences;

    @FXML
    private Tab tabGeneral;
    @FXML
    private Tab tabFont;
    @FXML
    private Tab tabMindMap;
    @FXML
    private Tab tabMarkdown;

    private final ButtonType resetButtonType = new ButtonType("Reset Default", ButtonBar.ButtonData.LEFT);

    public PreferencesDialog(PreferenceChangedEventHandler preferenceChangedEventHandler) {
        dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Preferences of Mindolph")
                .fxmlUri("dialog/preferences_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .button(resetButtonType, () -> {
                    boolean confirmed = DialogFactory.okCancelConfirmDialog("Are you sure, this operation will reset all your preferences to default");
                    if (confirmed) {
                        generalPreferencesPane.resetToDefault();
                        fontPreferencesPane.resetToDefault();
                        mmdPreferences.resetToDefault();
                        mdPreferences.resetToDefault();
//                        plantumlPreferences.resetToDefault();
                    }
                })
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .icon(resetButtonType, FontIconManager.getIns().getIcon(IconKey.REFRESH))
                .defaultValue(null)
                .controller(this)
                .resizable(true)
                .build();
        generalPreferencesPane.loadPreferences();
        mmdPreferences.loadPreferences();
//        plantumlPreferences.loadPreferences();
        // handle preferences changes
        generalPreferencesPane.setPreferenceChangedEventHandler(preferenceChangedEventHandler);
        mmdPreferences.setPreferenceChangedEventHandler(preferenceChangedEventHandler);
        mdPreferences.setPreferenceChangedEventHandler(preferenceChangedEventHandler);
//        plantumlPreferences.setPreferenceChangedEventHandler(preferenceChangedEventHandler);
        fontPreferencesPane.setPreferenceChangedEventHandler(preferenceChangedEventHandler);

        Platform.runLater(() -> {
            tabPane.setTabIcon(tabGeneral, FontIconManager.getIns().getIcon(IconKey.GEAR));
            tabPane.setTabIcon(tabFont, FontIconManager.getIns().getIcon(IconKey.FONT));
            tabPane.setTabIcon(tabMindMap, FontIconManager.getIns().getIcon(IconKey.FILE_MMD));
            tabPane.setTabIcon(tabMarkdown, FontIconManager.getIns().getIcon(IconKey.FILE_MD));
        });
    }

    public void selectTab(int initialTabIndex) {
        tabPane.getSelectionModel().select(initialTabIndex);
    }

    public void selectTab(String name) {
        for (Tab tab : tabPane.getTabs()) {
            if (name.equals(tab.getUserData())) {
                int i = tabPane.getTabs().indexOf(tab);
                tabPane.getSelectionModel().select(i);
                break;
            }
        }
    }

}
