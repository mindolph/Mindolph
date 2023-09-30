package com.mindolph.mindmap.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.theme.*;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static com.mindolph.base.constant.FontConstants.*;

/**
 * Composite control for Mind Map's preferences.
 *
 * @author mindolph.com@gmail.com
 */
public class MmdPreferencesPane extends BasePrefsPane implements Initializable {

    private final Logger log = LoggerFactory.getLogger(MmdPreferencesPane.class);

    private MindMapConfig mindMapConfig;

    @FXML
    private ChoiceBox<Pair<ThemeKey, String>> cbTheme;
    @FXML
    private Button btnActions;
    private ContextMenu contextMenu;
    private MenuItem miDuplicate;
    private MenuItem miDelete;

    @FXML
    private Spinner<Integer> spnGridStep;
    @FXML
    private ChoiceBox<Pair<ConnectorStyle, String>> cbConnectorStyle;
    @FXML
    private CheckBox ckbShowGrid;
    @FXML
    private ColorPicker cpGridColor;
    @FXML
    private ColorPicker cpBackgroundFillColor;
    @FXML
    private Spinner<Double> spnConnectorWidth;
    @FXML
    private CheckBox ckbShowCollapsatorOnMouseHover;
    @FXML
    private Spinner<Double> spnRoundRadius;
    @FXML
    private Spinner<Double> spnJumpLinkWidth;
    @FXML
    private Spinner<Double> spnCollapsatorWidth;
    @FXML
    private Spinner<Double> spnCollapsatorSize;
    @FXML
    private ColorPicker cpTopicBorderColor;
    @FXML
    private ColorPicker cpCollapsatorFillColor;
    @FXML
    private ColorPicker cpCollapsatorBorderColor;
    @FXML
    private ColorPicker cpConnectorColor;
    @FXML
    private ColorPicker cpJumpLinkColor;
    @FXML
    private CheckBox ckbDropShadow;
    @FXML
    private Spinner<Double> spnBorderWidth;
    @FXML
    private ColorPicker cpRootFillColor;
    @FXML
    private ColorPicker cpRootTextColor;
    @FXML
    private ColorPicker cp1stLevelColor;
    @FXML
    private ColorPicker cp1stLevelTextColor;
    @FXML
    private ColorPicker cp2ndLevelColor;
    @FXML
    private ColorPicker cp2ndLevelTextColor;
    @FXML
    private Slider sld1stLevelHGap;
    @FXML
    private Slider sld1stLevelVGap;
    @FXML
    private Slider sld2ndLevelHGap;
    @FXML
    private Slider sld2ndLevelVGap;
    @FXML
    private ColorPicker cpSelectionColor;
    @FXML
    private Spinner<Double> spnSelectionWidth;
    @FXML
    private Spinner<Double> spnSelectionGap;
    @FXML
    private CheckBox ckbAddDefaultCommentToRoot;
    @FXML
    private CheckBox ckbTrimTopicText;
    //    @FXML
//    private CheckBox ckbPreferInternalBrowser;
    @FXML
    private CheckBox ckbUnfoldCollapsedDropTarget;
    @FXML
    private CheckBox ckbCopyParentColorToNewChild;
    @FXML
    private CheckBox ckbSmartTextPaste;
    @FXML
    private Spinner<Integer> spnUndRedo;

    private final Pair<ThemeKey, String> THEME_ITEM_CLASSIC;
    private final Pair<ThemeKey, String> THEME_ITEM_LIGHT;
    private final Pair<ThemeKey, String> THEME_ITEM_DARK;

    private final Pair<ConnectorStyle, String> CS_ITEM_BEZIER = new Pair<>(ConnectorStyle.BEZIER, ThemeUtils.connectorTypeLabel(ConnectorStyle.BEZIER.name()));
    private final Pair<ConnectorStyle, String> CS_ITEM_POLYLINE = new Pair<>(ConnectorStyle.POLYLINE, ThemeUtils.connectorTypeLabel(ConnectorStyle.POLYLINE.name()));

    // listeners for binding
    private Map<ReadOnlyProperty, ChangeListener> listeners = new HashMap<>();

    // any preference has been changed.
    private boolean changed = false;

    public MmdPreferencesPane() {
        super("/preference/mmd_preferences.fxml");

        THEME_ITEM_CLASSIC = new Pair<>(new ThemeKey(ThemeType.CLASSIC.name(), null), ThemeUtils.themeLabel(ThemeType.CLASSIC.name()));
        THEME_ITEM_LIGHT = new Pair<>(new ThemeKey(ThemeType.LIGHT.name(), null), ThemeUtils.themeLabel(ThemeType.LIGHT.name()));
        THEME_ITEM_DARK = new Pair<>(new ThemeKey(ThemeType.DARK.name(), null), ThemeUtils.themeLabel(ThemeType.DARK.name()));

        mindMapConfig = new MindMapConfig();
        mindMapConfig.loadFromPreferences();
        this.bindAll();
        cbTheme.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<ThemeKey, String> p) {
                return p.getValue();
            }

            @Override
            public Pair<ThemeKey, String> fromString(String s) {
                return null;
            }
        });
        // load pre-defined themes
        cbTheme.getItems().addAll(Arrays.asList(
                THEME_ITEM_CLASSIC,
                THEME_ITEM_LIGHT,
                THEME_ITEM_DARK
        ));
        // load user's themes
        if (mindMapConfig.getUserThemes() != null) {
            cbTheme.getItems().addAll(mindMapConfig.getUserThemes().stream().map(n -> new Pair<>(new ThemeKey(n, n), ThemeUtils.themeLabel(n))).toList());
        }
        cbTheme.valueProperty().addListener((observableValue, old, newChoice) -> {
            // TO switch theme
            log.debug("Switch to theme: " + mindMapConfig.getThemeName());
            ThemeKey selectedKey = newChoice.getKey();
            mindMapConfig.setThemeName(selectedKey.name);// set current theme
            mindMapConfig.setTheme(ThemeUtils.createTheme(selectedKey.name));
            if (!this.isPredefinedTheme(selectedKey.name)) {
                mindMapConfig.getTheme().loadFromPreferences();
            }
            this.initControlsFromPreferences();
            this.save(true);
            // bind
            this.bindTheme();
            // toggle theme controls disable state byt theme type and specific items.
            this.toggleThemeSettings(isPredefinedTheme(mindMapConfig.getThemeName()));
        });
        cbTheme.setValue(new Pair<>(new ThemeKey(mindMapConfig.getThemeName(), null), ThemeUtils.themeLabel(mindMapConfig.getThemeName())));
        btnActions.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnActions.setOnMouseClicked(event -> {
            if (contextMenu == null) {
                contextMenu = new ContextMenu();
            }
            else {
                contextMenu.getItems().clear();
                contextMenu.hide();
            }
            miDuplicate = new MenuItem("Duplicate", FontIconManager.getIns().getIcon(IconKey.CLONE));
            miDelete = new MenuItem("Delete", FontIconManager.getIns().getIcon(IconKey.DELETE));
            miDelete.setDisable(isPredefinedTheme(mindMapConfig.getThemeName()));
            miDuplicate.setOnAction(event1 -> {
                Dialog<String> nameDialog = new TextDialogBuilder()
                        .owner(DialogFactory.DEFAULT_WINDOW)
                        .title("New Theme Name")
                        .content("Give a name for you own customized theme: ")
                        .width(480)
                        .text(ThemeUtils.themeLabel(mindMapConfig.getThemeName()) + "_copy").build();
                Optional<String> optName = nameDialog.showAndWait();
                if (optName.isPresent()) {
                    String newName = optName.get();
                    if (cbTheme.getItems().stream().anyMatch(themeKeyStringPair -> themeKeyStringPair.getValue().equals(newName))) {
                        DialogFactory.errDialog("Theme %s already exists".formatted(newName));
                        return;
                    }

                    ThemeKey parentThemeKey = cbTheme.getSelectionModel().getSelectedItem().getKey();
                    mindMapConfig.getUserThemes().add(newName);
                    ThemeKey newThemeKey = new ThemeKey(newName, parentThemeKey.name);

                    // create new custom theme and save to preference.
                    MindMapTheme newTheme = createThemeFromParent(newThemeKey);
                    newTheme.saveToPreferences();

                    Pair<ThemeKey, String> newItem = new Pair<>(newThemeKey, newName);
                    cbTheme.getItems().add(newItem);
                    cbTheme.getSelectionModel().select(newItem);
                }
            });
            miDelete.setOnAction(event12 -> {
                ThemeKey selectedThemeKey = cbTheme.getSelectionModel().getSelectedItem().getKey();
                String selectedThemeName = selectedThemeKey.name;
                log.debug("Delete theme: " + selectedThemeName);
                if (!isPredefinedTheme(selectedThemeName)) {
                    CustomTheme userTheme = new CustomTheme(selectedThemeName);
                    userTheme.deleteFromPreference();
                    mindMapConfig.getUserThemes().remove(selectedThemeName);
                    mindMapConfig.saveToPreferences();
                    // switch to class theme
                    Pair<ThemeKey, String> deletedItem = new Pair<>(selectedThemeKey, selectedThemeName);
                    cbTheme.getItems().remove(deletedItem);
                    cbTheme.getSelectionModel().select(THEME_ITEM_CLASSIC);
                }
            });
            contextMenu.getItems().addAll(miDuplicate, miDelete);
            contextMenu.show(btnActions, event.getScreenX(), event.getScreenY());
        });
        cbConnectorStyle.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<ConnectorStyle, String> object) {
                return object.getValue();
            }

            @Override
            public Pair<ConnectorStyle, String> fromString(String string) {
                return null;
            }
        });
        cbConnectorStyle.getItems().addAll(Arrays.asList(
                CS_ITEM_BEZIER,
                CS_ITEM_POLYLINE
        ));
        cbConnectorStyle.getSelectionModel().select(new Pair<>(mindMapConfig.getTheme().getConnectorStyle(), ThemeUtils.connectorTypeLabel(mindMapConfig.getTheme().getConnectorStyle())));
    }

    private boolean isPredefinedTheme(String name) {
        return Arrays.stream(ThemeType.values()).anyMatch(themeType -> themeType.name().equals(name));
    }

    private void toggleThemeSettings(boolean disable) {
        for (String name : themeNodes.keySet()) {
            boolean isDisable = disable || mindMapConfig.getTheme().getDisabledSettings().contains(name);
            themeNodes.get(name).setDisable(isDisable);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // dont do initialization for controls here.

        // listen font changes for mind map and reload, because the fonts are set in another panel,
        // if not, the font changes will be recovered when saving mmd preferences.
        EventBus.getIns().subscribePreferenceChanged(fileType -> {
            if (SupportFileTypes.TYPE_MIND_MAP.equals(fileType)) {
                mindMapConfig.setTopicFont(fxPreferences.getPreference(KEY_MMD_TOPIC_FONT, DEFAULT_FONTS.get(KEY_MMD_TOPIC_FONT)));
                mindMapConfig.setNoteFont(fxPreferences.getPreference(KEY_MMD_NOTE_FONT, DEFAULT_FONTS.get(KEY_MMD_NOTE_FONT)));
            }
        });
    }

    /**
     * Create custom theme from parent theme in themeKey.
     *
     * @param themeKey
     * @return
     */
    private MindMapTheme createThemeFromParent(ThemeKey themeKey) {
        MindMapTheme theme = ThemeUtils.createTheme(themeKey.name);
        if (theme instanceof CustomTheme customTheme) {
            if (StringUtils.isNotBlank(themeKey.parentName)) {
                MindMapTheme parentTheme = ThemeUtils.createTheme(themeKey.parentName);
//                log.debug("Copy theme from parent theme: " + parentTheme.getClass());
                parentTheme.loadFromPreferences();
                customTheme.copyFromTheme(parentTheme);
            }
        }
        return theme;
    }

    private void bindAll() {
        this.bindTheme();

        // bind with the new style.
        super.bindPreference(ckbAddDefaultCommentToRoot.selectedProperty(), PrefConstants.PREF_KEY_MMD_ADD_DEF_COMMENT_TO_ROOT, true);
        super.bindPreference(ckbTrimTopicText.selectedProperty(), PrefConstants.PREF_KEY_MMD_TRIM_TOPIC_TEXT, true);
//        super.bindPreference(ckbPreferInternalBrowser.selectedProperty(), PrefConstants.PREF_KEY_MMD_USE_INSIDE_BROWSER, true);
//        super.bindPreference(ckbUseRelevantPathForProjectFiles.selectedProperty(), PrefConstants.PREF_KEY_MMD_MAKE_RELATIVE_PATH_TO_PROJECT, true);
        super.bindPreference(ckbUnfoldCollapsedDropTarget.selectedProperty(), PrefConstants.PREF_KEY_MMD_UNFOLD_COLLAPSED_TARGET, false);
        super.bindPreference(ckbCopyParentColorToNewChild.selectedProperty(), PrefConstants.PREF_KEY_MMD_COPY_COLOR_INFO_TO_NEW_CHILD, false);
        this.bindPreference(ckbSmartTextPaste.selectedProperty(), mindMapConfig::setSmartTextPaste);
    }

    /**
     * Bind UI controls for theme with current theme in mind map config.
     */
    private void bindTheme() {
        // bind with the old style
        MindMapTheme theme = mindMapConfig.getTheme();
        this.bindPreference(spnGridStep.valueProperty(), theme::setGridStep);
        this.bindPreference(ckbShowGrid.selectedProperty(), theme::setShowGrid);
        this.bindPreference(cbConnectorStyle.valueProperty(), connectorStyleStringPair -> theme.setConnectorStyle(connectorStyleStringPair.getKey()));

        this.bindPreference(cpGridColor.valueProperty(), theme::setGridColor);
        this.bindPreference(cpBackgroundFillColor.valueProperty(), theme::setPaperColor);

        this.bindPreference(spnConnectorWidth.valueProperty(), aDouble -> theme.setConnectorWidth(aDouble.floatValue()));
        this.bindPreference(spnJumpLinkWidth.valueProperty(), aDouble -> theme.setJumpLinkWidth(aDouble.floatValue()));
        this.bindPreference(ckbShowCollapsatorOnMouseHover.selectedProperty(), theme::setShowCollapsatorOnMouseHover);
        this.bindPreference(spnCollapsatorWidth.valueProperty(), aDouble -> theme.setCollapsatorBorderWidth(aDouble.floatValue()));
        this.bindPreference(spnCollapsatorSize.valueProperty(), aDouble -> theme.setCollapsatorSize(aDouble.floatValue()));
        this.bindPreference(cpTopicBorderColor.valueProperty(), theme::setElementBorderColor);
        this.bindPreference(cpCollapsatorFillColor.valueProperty(), theme::setCollapsatorBackgroundColor);
        this.bindPreference(cpCollapsatorBorderColor.valueProperty(), theme::setCollapsatorBorderColor);
        this.bindPreference(cpConnectorColor.valueProperty(), theme::setConnectorColor);
        this.bindPreference(cpJumpLinkColor.valueProperty(), theme::setJumpLinkColor);
        this.bindPreference(ckbDropShadow.selectedProperty(), theme::setDropShadow);
        this.bindPreference(spnBorderWidth.valueProperty(), number -> theme.setElementBorderWidth(number.floatValue()));
        this.bindPreference(spnRoundRadius.valueProperty(), number -> theme.setRoundRadius(number.floatValue()));
        this.bindPreference(cpRootFillColor.valueProperty(), theme::setRootBackgroundColor);
        this.bindPreference(cpRootTextColor.valueProperty(), theme::setRootTextColor);
        this.bindPreference(cp1stLevelColor.valueProperty(), theme::setFirstLevelBackgroundColor);
        this.bindPreference(cp1stLevelTextColor.valueProperty(), theme::setFirstLevelTextColor);
        this.bindPreference(cp2ndLevelColor.valueProperty(), theme::setOtherLevelBackgroundColor);
        this.bindPreference(cp2ndLevelTextColor.valueProperty(), theme::setOtherLevelTextColor);
        this.bindPreference(sld1stLevelHGap.valueProperty(), number -> theme.setFirstLevelHorizontalInset(number.intValue()));
        this.bindPreference(sld1stLevelVGap.valueProperty(), number -> theme.setFirstLevelVerticalInset(number.intValue()));
        this.bindPreference(sld2ndLevelHGap.valueProperty(), number -> theme.setOtherLevelHorizontalInset(number.intValue()));
        this.bindPreference(sld2ndLevelVGap.valueProperty(), number -> theme.setOtherLevelVerticalInset(number.intValue()));
        this.bindPreference(cpSelectionColor.valueProperty(), theme::setSelectLineColor);
        this.bindPreference(spnSelectionWidth.valueProperty(), aDouble -> theme.setSelectLineWidth(aDouble.floatValue()));
        this.bindPreference(spnSelectionGap.valueProperty(), number -> theme.setSelectLineGap(number.intValue()));
        this.bindPreference(spnUndRedo.valueProperty(), integer -> mindMapConfig.setMaxRedoUndo(integer));
    }

    @Override
    protected void initControlsFromPreferences() {
        //
        MindMapTheme theme = mindMapConfig.getTheme();
        spnGridStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 256, theme.getGridStep()));
        ckbShowGrid.setSelected(theme.isShowGrid());
        cbConnectorStyle.getSelectionModel().select(CS_ITEM_BEZIER.getKey() == theme.getConnectorStyle() ? CS_ITEM_BEZIER : CS_ITEM_POLYLINE);
        cpGridColor.setValue(theme.getGridColor());
        cpBackgroundFillColor.setValue(theme.getPaperColor());
        spnConnectorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 16f, theme.getConnectorWidth()));
        spnJumpLinkWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 8f, theme.getJumpLinkWidth()));
        ckbShowCollapsatorOnMouseHover.setSelected(theme.isShowCollapsatorOnMouseHover());
        spnCollapsatorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, theme.getCollapsatorBorderWidth()));
        spnCollapsatorSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(16, 32f, theme.getCollapsatorSize()));
        cpTopicBorderColor.setValue(theme.getElementBorderColor());
        cpCollapsatorFillColor.setValue(theme.getCollapsatorBackgroundColor());
        cpCollapsatorBorderColor.setValue(theme.getCollapsatorBorderColor());
        cpConnectorColor.setValue(theme.getConnectorColor());
        cpJumpLinkColor.setValue(theme.getJumpLinkColor());
        ckbDropShadow.setSelected(theme.isDropShadow());
        spnBorderWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, theme.getElementBorderWidth()));
        spnRoundRadius.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0f, 20f, theme.getRoundRadius()));
        cpRootFillColor.setValue(theme.getRootBackgroundColor());
        cpRootTextColor.setValue(theme.getRootTextColor());
        cp1stLevelColor.setValue(theme.getFirstLevelBackgroundColor());
        cp1stLevelTextColor.setValue(theme.getFirstLevelTextColor());
        cp2ndLevelColor.setValue(theme.getOtherLevelBackgroundColor());
        cp2ndLevelTextColor.setValue(theme.getOtherLevelTextColor());
        sld1stLevelHGap.setValue(theme.getFirstLevelHorizontalInset());
        sld1stLevelVGap.setValue(theme.getFirstLevelVerticalInset());
        sld2ndLevelHGap.setValue(theme.getOtherLevelHorizontalInset());
        sld2ndLevelVGap.setValue(theme.getOtherLevelVerticalInset());
        cpSelectionColor.setValue(theme.getSelectLineColor());
        spnSelectionWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, theme.getSelectLineWidth()));
        spnSelectionGap.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 8f, theme.getSelectLineGap()));
        ckbSmartTextPaste.setSelected(mindMapConfig.isSmartTextPaste());
        spnUndRedo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 50, mindMapConfig.getMaxRedoUndo()));
    }


    /**
     * This binding method is only used for mmd config for its different from FX.
     *
     * @param property
     * @param saveFunction
     * @param <T>
     */
    protected <T> void bindPreference(ReadOnlyProperty<T> property, Consumer<T> saveFunction) {
        // clean
        ChangeListener oldListener = listeners.get(property);
        if (oldListener != null) {
            property.removeListener(oldListener);
        }
        // add new listener
        ChangeListener<T> changeListener = (observable, oldValue, newValue) -> {
            if (isLoaded) {
                log.debug("Save preference to cache from control: %s".formatted(property.getBean()));
                saveFunction.accept(newValue);
                save(false);
                changed = true;
            }
        };
        listeners.put(property, changeListener);
        property.addListener(changeListener);
    }


    @Override
    public void resetToDefault() {
        super.resetToDefault();
        mindMapConfig = new MindMapConfig();
        this.initControlsFromPreferences(); // reset all customized preferences by setting value of controls.
    }

    public void save(boolean notify) {
        if (!changed) {
            return;
        }
        mindMapConfig.saveToPreferences();
        // notify reload config
        if (notify) {
            EventBus.getIns().notifyPreferenceChanged(SupportFileTypes.TYPE_MIND_MAP);
        }
    }

    private Map<String, Node> themeNodes = new HashMap<>() {
        {
            put("spnGridStep", spnGridStep);
            put("cbConnectorStyle", cbConnectorStyle);
            put("ckbShowGrid", ckbShowGrid);
            put("cpGridColor", cpGridColor);
            put("cpBackgroundFillColor", cpBackgroundFillColor);
            put("spnConnectorWidth", spnConnectorWidth);
            put("ckbShowCollapsatorOnMouseHover", ckbShowCollapsatorOnMouseHover);
            put("spnRoundRadius", spnRoundRadius);
            put("spnJumpLinkWidth", spnJumpLinkWidth);
            put("spnCollapsatorWidth", spnCollapsatorWidth);
            put("spnCollapsatorSize", spnCollapsatorSize);
            put("cpTopicBorderColor", cpTopicBorderColor);
            put("cpCollapsatorFillColor", cpCollapsatorFillColor);
            put("cpCollapsatorBorderColor", cpCollapsatorBorderColor);
            put("cpConnectorColor", cpConnectorColor);
            put("cpJumpLinkColor", cpJumpLinkColor);
            put("ckbDropShadow", ckbDropShadow);
            put("spnBorderWidth", spnBorderWidth);
            put("cpRootFillColor", cpRootFillColor);
            put("cpRootTextColor", cpRootTextColor);
            put("cp1stLevelColor", cp1stLevelColor);
            put("cp1stLevelTextColor", cp1stLevelTextColor);
            put("cp2ndLevelColor", cp2ndLevelColor);
            put("cp2ndLevelTextColor", cp2ndLevelTextColor);
            put("sld1stLevelHGap", sld1stLevelHGap);
            put("sld1stLevelVGap", sld1stLevelVGap);
            put("sld2ndLevelHGap", sld2ndLevelHGap);
            put("sld2ndLevelVGap", sld2ndLevelVGap);
            put("cpSelectionColor", cpSelectionColor);
            put("spnSelectionWidth", spnSelectionWidth);
            put("spnSelectionGap", spnSelectionGap);
        }
    };

    private record ThemeKey(String name, String parentName) {
    }
}
