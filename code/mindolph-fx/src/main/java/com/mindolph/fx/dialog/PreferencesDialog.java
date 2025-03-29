package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.fx.preference.FontPreferencesPane;
import com.mindolph.fx.preference.GenAiPreferencePane;
import com.mindolph.fx.preference.GeneralPreferencesPane;
import com.mindolph.markdown.preference.MarkdownPreferencesPane;
import com.mindolph.mfx.container.SideTabPane;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.preference.MmdPreferencesPane;

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
    private MmdPreferencesPane mmdPreferences;

    @FXML
    private MarkdownPreferencesPane mdPreferences;

//    @FXML
//    private PlantumlPreferences plantumlPreferences;

    @FXML
    private GenAiPreferencePane genAiPreferences;

    @FXML
    private Tab tabGeneral;
    @FXML
    private Tab tabFont;
    @FXML
    private Tab tabMindMap;
    @FXML
    private Tab tabMarkdown;
    @FXML
    private Tab tabGenAi;

    private int lastActivatedTabIndex = 0;

    private final ButtonType resetButtonType = new ButtonType("Reset Default", ButtonBar.ButtonData.LEFT);

    public PreferencesDialog() {
        dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Preferences")
                .fxmlUri("dialog/preferences_dialog.fxml")
                .button(ButtonType.CLOSE, dialog -> {
//                    fontPreferencesPane.save
                    mmdPreferences.onSave(true); // todo use change status to avoid unnecessary save
                    dialog.close();
                })
                .button(resetButtonType, dialog -> {
                    boolean confirmed = DialogFactory.okCancelConfirmDialog("Are you sure, this operation will reset all your preferences to default");
                    if (confirmed) {
                        generalPreferencesPane.resetToDefault();
                        fontPreferencesPane.resetToDefault();
                        mmdPreferences.resetToDefault();
                        mdPreferences.resetToDefault();
                        genAiPreferences.resetToDefault();
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
        genAiPreferences.loadPreferences();

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> lastActivatedTabIndex = newValue.intValue());

        Platform.runLater(() -> {
            tabPane.setTabIcon(tabGeneral, FontIconManager.getIns().getIcon(IconKey.GEAR));
            tabPane.setTabIcon(tabFont, FontIconManager.getIns().getIcon(IconKey.FONT));
            tabPane.setTabIcon(tabMindMap, FontIconManager.getIns().getIcon(IconKey.FILE_MMD));
            tabPane.setTabIcon(tabMarkdown, FontIconManager.getIns().getIcon(IconKey.FILE_MD));
            tabPane.setTabIcon(tabGenAi, FontIconManager.getIns().getIcon(IconKey.MAGIC));
            this.selectTab(lastActivatedTabIndex);
        });
    }

    public void selectTab(int initialTabIndex) {
        tabPane.getSelectionModel().select(initialTabIndex);
        lastActivatedTabIndex = initialTabIndex;
    }

    public void selectTab(String name) {
        for (Tab tab : tabPane.getTabs()) {
            if (name.equals(tab.getUserData())) {
                int i = tabPane.getTabs().indexOf(tab);
                tabPane.getSelectionModel().select(i);
                lastActivatedTabIndex = i;
                break;
            }
        }
    }

}
