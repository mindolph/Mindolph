package com.mindolph.mindmap.preference;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.theme.*;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * Composite control for Mind Map's preferences.
 *
 * @author mindolph.com@gmail.com
 */
public class MmdPreferencesPane extends BasePrefsPane implements Initializable {

    private final Logger log = LoggerFactory.getLogger(MmdPreferencesPane.class);

    private MindMapConfig mindMapConfig;
//    private MindMapTheme theme;

    @FXML
    private ChoiceBox<Pair<ThemeKey, String>> cbTheme;
    @FXML
    private Button btnDuplicate;
    @FXML
    private Button btnDelete;
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


    private final Pair<ConnectorStyle, String> CS_ITEM_BEZIER  = new Pair<>(ConnectorStyle.BEZIER, ThemeUtils.connectorTypeLabel(ConnectorStyle.BEZIER.name()));
    private final Pair<ConnectorStyle, String> CS_ITEM_POLYLINE  = new Pair<>(ConnectorStyle.POLYLINE, ThemeUtils.connectorTypeLabel(ConnectorStyle.POLYLINE.name()));

    public MmdPreferencesPane() {
        super("/preference/mmd_preferences.fxml");

        THEME_ITEM_CLASSIC = new Pair<>(new ThemeKey(ThemeType.CLASSIC.name(), null), ThemeUtils.themeLabel(ThemeType.CLASSIC.name()));
        THEME_ITEM_LIGHT = new Pair<>(new ThemeKey(ThemeType.LIGHT.name(), null), ThemeUtils.themeLabel(ThemeType.LIGHT.name()));

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
                THEME_ITEM_LIGHT
        ));
        // load user's themes
        if (mindMapConfig.getUserThemes() != null) {
            cbTheme.getItems().addAll(mindMapConfig.getUserThemes().stream().map(n -> new Pair<>(new ThemeKey(n, n), ThemeUtils.themeLabel(n))).toList());
        }
        cbTheme.valueProperty().addListener((observableValue, old, newChoice) -> {
            // TO switch theme
            System.out.println("Switch to theme: " + mindMapConfig.getThemeName());
            ThemeKey selectedKey = newChoice.getKey();
            mindMapConfig.setThemeName(selectedKey.name);// set current theme
            mindMapConfig.setTheme(ThemeUtils.createTheme(selectedKey.name));
            if (this.isPredefinedTheme(selectedKey.name)) {
                btnDelete.setDisable(true);
            }
            else {
                mindMapConfig.getTheme().loadFromPreferences();
                btnDelete.setDisable(false);
            }
            this.initControlsFromPreferences();
            this.save(true);
            // bind
            this.bindTheme();
        });
        cbTheme.setValue(new Pair<>(new ThemeKey(mindMapConfig.getThemeName(), null), ThemeUtils.themeLabel(mindMapConfig.getThemeName())));
        btnDuplicate.setOnAction(event -> {
            Dialog<String> nameDialog = new TextDialogBuilder()
                    .owner(DialogFactory.DEFAULT_WINDOW)
                    .title("New Theme Name")
                    .content("Give a name for you own customized theme: ")
                    .width(480)
                    .text(mindMapConfig.getThemeName() + "_copy").build();
            Optional<String> optName = nameDialog.showAndWait();
            if (optName.isPresent()) {
                ThemeKey parentThemeKey = cbTheme.getSelectionModel().getSelectedItem().getKey();
                String newName = optName.get();
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
        btnDelete.setOnAction(event -> {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
        cbConnectorStyle.getSelectionModel().select(CS_ITEM_BEZIER.getKey() == theme.getConnectorStyle()? CS_ITEM_BEZIER: CS_ITEM_POLYLINE);
        cpGridColor.setValue(theme.getGridColor());
        cpBackgroundFillColor.setValue(theme.getPaperColor());
        spnConnectorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 16f, theme.getConnectorWidth()));
        spnJumpLinkWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 8f, theme.getJumpLinkWidth()));
        ckbShowCollapsatorOnMouseHover.setSelected(theme.isShowCollapsatorOnMouseHover());
        spnCollapsatorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, theme.getCollapsatorBorderWidth()));
        spnCollapsatorSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(16, 32f, theme.getCollapsatorSize()));
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
                save(false); // save all or single? TODO
            }
        };
        listeners.put(property, changeListener);
        property.addListener(changeListener);
        //getHandlers.add(getHandler);
    }

    private Map<ReadOnlyProperty, ChangeListener> listeners = new HashMap<>();

    @Override
    public void resetToDefault() {
        super.resetToDefault();
        mindMapConfig = new MindMapConfig();
        this.initControlsFromPreferences(); // reset all customized preferences by setting value of controls.
    }

    public void save(boolean notify) {
//        for (Consumer<?> getHandler : getHandlers) {
//            getHandler.apply();
//        }
        System.out.println("Save to Preference from cache");
        mindMapConfig.saveToPreferences();
//        preferencesManager.flush();
        // notify reload config
        if (notify && preferenceChangedEventHandler != null) {
            preferenceChangedEventHandler.onPreferenceChanged(SupportFileTypes.TYPE_MIND_MAP);
        }
    }

    private class ThemeKey {
        String name;
        String parentName;

        public ThemeKey(String name, String parentName) {
            this.name = name;
            this.parentName = parentName;
        }
    }
}
