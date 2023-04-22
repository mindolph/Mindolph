package com.mindolph.mindmap.preference;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mindmap.MindMapConfig;
import javafx.beans.property.ReadOnlyProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Composite control for Mind Map's preferences.
 *
 * @author mindolph.com@gmail.com
 */
public class MmdPreferences extends BasePrefsPane {

    private final Logger log = LoggerFactory.getLogger(MmdPreferences.class);

    private MindMapConfig mindMapConfig;

    @FXML
    private Spinner<Integer> spnGridStep;
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

    public MmdPreferences() {
        super("/preference/mmd_preferences.fxml");
        mindMapConfig = new MindMapConfig();
        mindMapConfig.loadFromPreferences();
        log.info(String.valueOf(mindMapConfig));
        this.bindAll();
    }

    private void init() {
    }

    private void bindAll() {
        // bind with the old style
        this.bindPreference(spnGridStep.valueProperty(), mindMapConfig::setGridStep);
        this.bindPreference(ckbShowGrid.selectedProperty(), mindMapConfig::setShowGrid);
        this.bindPreference(cpGridColor.valueProperty(), mindMapConfig::setGridColor);
        this.bindPreference(cpBackgroundFillColor.valueProperty(), mindMapConfig::setPaperColor);
        this.bindPreference(spnConnectorWidth.valueProperty(), aDouble -> mindMapConfig.setConnectorWidth(aDouble.floatValue()));
        this.bindPreference(spnJumpLinkWidth.valueProperty(), aDouble -> mindMapConfig.setJumpLinkWidth(aDouble.floatValue()));
        this.bindPreference(ckbShowCollapsatorOnMouseHover.selectedProperty(), mindMapConfig::setShowCollapsatorOnMouseHover);
        this.bindPreference(spnCollapsatorWidth.valueProperty(), aDouble -> mindMapConfig.setCollapsatorBorderWidth(aDouble.floatValue()));
        this.bindPreference(spnCollapsatorSize.valueProperty(), aDouble -> mindMapConfig.setCollapsatorSize(aDouble.floatValue()));
        this.bindPreference(cpCollapsatorFillColor.valueProperty(), mindMapConfig::setCollapsatorBackgroundColor);
        this.bindPreference(cpCollapsatorBorderColor.valueProperty(), mindMapConfig::setCollapsatorBorderColor);
        this.bindPreference(cpConnectorColor.valueProperty(), mindMapConfig::setConnectorColor);
        this.bindPreference(cpJumpLinkColor.valueProperty(), mindMapConfig::setJumpLinkColor);
        this.bindPreference(ckbDropShadow.selectedProperty(), mindMapConfig::setDropShadow);
        this.bindPreference(spnBorderWidth.valueProperty(), number -> mindMapConfig.setElementBorderWidth(number.floatValue()));
        this.bindPreference(spnRoundRadius.valueProperty(), number -> mindMapConfig.setRoundRadius(number.floatValue()));
        this.bindPreference(cpRootFillColor.valueProperty(), mindMapConfig::setRootBackgroundColor);
        this.bindPreference(cpRootTextColor.valueProperty(), mindMapConfig::setRootTextColor);
        this.bindPreference(cp1stLevelColor.valueProperty(), mindMapConfig::setFirstLevelBackgroundColor);
        this.bindPreference(cp1stLevelTextColor.valueProperty(), mindMapConfig::setFirstLevelTextColor);
        this.bindPreference(cp2ndLevelColor.valueProperty(), mindMapConfig::setOtherLevelBackgroundColor);
        this.bindPreference(cp2ndLevelTextColor.valueProperty(), mindMapConfig::setOtherLevelTextColor);
        this.bindPreference(sld1stLevelHGap.valueProperty(), number -> mindMapConfig.setFirstLevelHorizontalInset(number.intValue()));
        this.bindPreference(sld1stLevelVGap.valueProperty(), number -> mindMapConfig.setFirstLevelVerticalInset(number.intValue()));
        this.bindPreference(sld2ndLevelHGap.valueProperty(), number -> mindMapConfig.setOtherLevelHorizontalInset(number.intValue()));
        this.bindPreference(sld2ndLevelVGap.valueProperty(), number -> mindMapConfig.setOtherLevelVerticalInset(number.intValue()));
        this.bindPreference(cpSelectionColor.valueProperty(), mindMapConfig::setSelectLineColor);
        this.bindPreference(spnSelectionWidth.valueProperty(), aDouble -> mindMapConfig.setSelectLineWidth(aDouble.floatValue()));
        this.bindPreference(spnSelectionGap.valueProperty(), number -> mindMapConfig.setSelectLineGap(number.intValue()));
        this.bindPreference(spnUndRedo.valueProperty(), integer -> mindMapConfig.setMaxRedoUndo(integer));

        // bind with the new style.
        super.bindPreference(ckbTrimTopicText.selectedProperty(), PrefConstants.PREF_KEY_MMD_TRIM_TOPIC_TEXT, true);
//        super.bindPreference(ckbPreferInternalBrowser.selectedProperty(), PrefConstants.PREF_KEY_MMD_USE_INSIDE_BROWSER, true);
//        super.bindPreference(ckbUseRelevantPathForProjectFiles.selectedProperty(), PrefConstants.PREF_KEY_MMD_MAKE_RELATIVE_PATH_TO_PROJECT, true);
        super.bindPreference(ckbUnfoldCollapsedDropTarget.selectedProperty(), PrefConstants.PREF_KEY_MMD_UNFOLD_COLLAPSED_TARGET, false);
        super.bindPreference(ckbCopyParentColorToNewChild.selectedProperty(), PrefConstants.PREF_KEY_MMD_COPY_COLOR_INFO_TO_NEW_CHILD, false);
        this.bindPreference(ckbSmartTextPaste.selectedProperty(), mindMapConfig::setSmartTextPaste);
    }

    @Override
    protected void loadCustomizePreferences() {
        //
        spnGridStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 256, mindMapConfig.getGridStep()));
        ckbShowGrid.setSelected(mindMapConfig.isShowGrid());
        cpGridColor.setValue(mindMapConfig.getGridColor());
        cpBackgroundFillColor.setValue(mindMapConfig.getPaperColor());
        spnConnectorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 16f, mindMapConfig.getConnectorWidth()));
        spnJumpLinkWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 8f, mindMapConfig.getJumpLinkWidth()));
        ckbShowCollapsatorOnMouseHover.setSelected(mindMapConfig.isShowCollapsatorOnMouseHover());
        spnCollapsatorWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, mindMapConfig.getCollapsatorBorderWidth()));
        spnCollapsatorSize.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(16, 32f, mindMapConfig.getCollapsatorSize()));
        cpCollapsatorFillColor.setValue(mindMapConfig.getCollapsatorBackgroundColor());
        cpCollapsatorBorderColor.setValue(mindMapConfig.getCollapsatorBorderColor());
        cpConnectorColor.setValue(mindMapConfig.getConnectorColor());
        cpJumpLinkColor.setValue(mindMapConfig.getJumpLinkColor());
        ckbDropShadow.setSelected(mindMapConfig.isDropShadow());
        spnBorderWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, mindMapConfig.getElementBorderWidth()));
        spnRoundRadius.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0f, 20f, mindMapConfig.getRoundRadius()));
        cpRootFillColor.setValue(mindMapConfig.getRootBackgroundColor());
        cpRootTextColor.setValue(mindMapConfig.getRootTextColor());
        cp1stLevelColor.setValue(mindMapConfig.getFirstLevelBackgroundColor());
        cp1stLevelTextColor.setValue(mindMapConfig.getFirstLevelTextColor());
        cp2ndLevelColor.setValue(mindMapConfig.getOtherLevelBackgroundColor());
        cp2ndLevelTextColor.setValue(mindMapConfig.getOtherLevelTextColor());
        sld1stLevelHGap.setValue(mindMapConfig.getFirstLevelHorizontalInset());
        sld1stLevelVGap.setValue(mindMapConfig.getFirstLevelVerticalInset());
        sld2ndLevelHGap.setValue(mindMapConfig.getOtherLevelHorizontalInset());
        sld2ndLevelVGap.setValue(mindMapConfig.getOtherLevelVerticalInset());
        cpSelectionColor.setValue(mindMapConfig.getSelectLineColor());
        spnSelectionWidth.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 4f, mindMapConfig.getSelectLineWidth()));
        spnSelectionGap.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1f, 8f, mindMapConfig.getSelectLineGap()));
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
        property.addListener((observable, oldValue, newValue) -> {
            if (isLoaded) {
                log.debug("Save preference: %s".formatted(property.getBean()));
                saveFunction.accept(newValue);
                save(); // save all or single? TODO
            }
        });
        //getHandlers.add(getHandler);
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();
        mindMapConfig = new MindMapConfig();
        this.loadCustomizePreferences(); // reset all customized preferences by setting value of controls.
    }

    public void save() {
//        for (Consumer<?> getHandler : getHandlers) {
//            getHandler.apply();
//        }
        mindMapConfig.saveToPreferences();
//        preferencesManager.flush();
        // notify reload config
        preferenceChangedEventHandler.onPreferenceChanged(SupportFileTypes.TYPE_MIND_MAP);
    }

}
