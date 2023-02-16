package com.mindolph.fx.print;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.fx.print.PrintOptions.ScaleType;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;


/**
 * TODO move from here.
 *
 * @author mindolph.com@gmail.com
 */
public class PrintOptionsDialog extends BaseDialogController<PrintOptions> {

    @FXML
    private ChoiceBox<Pair<Integer, String>> cbZoomTo;
    @FXML
    private Spinner<Integer> spFitWidthTo;
    @FXML
    private Spinner<Integer> spFitHeightTo;

    @FXML
    private RadioButton rbZoomTo;
    @FXML
    private RadioButton rbFitToSinglePage;
    @FXML
    private RadioButton rbFitWidthTo;
    @FXML
    private RadioButton rbFitHeightTo;

    public PrintOptionsDialog(PrintOptions initOptions) {
        dialog = new CustomDialogBuilder<PrintOptions>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Print Options")
                .fxmlUri("dialog/print_options_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(initOptions)
                .controller(this)
                .build();

        result = new PrintOptions(initOptions);

        ToggleGroup group = new ToggleGroup();
        rbZoomTo.setToggleGroup(group);
        rbFitToSinglePage.setToggleGroup(group);
        rbFitWidthTo.setToggleGroup(group);
        rbFitHeightTo.setToggleGroup(group);

        rbZoomTo.setSelected(initOptions.getScaleType() == ScaleType.ZOOM);
        rbFitToSinglePage.setSelected(initOptions.getScaleType() == ScaleType.FIT_TO_SINGLE_PAGE);
        rbFitWidthTo.setSelected(initOptions.getScaleType() == ScaleType.FIT_WIDTH_TO_PAGES);
        rbFitHeightTo.setSelected(initOptions.getScaleType() == ScaleType.FIT_HEIGHT_TO_PAGES);

        cbZoomTo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<Integer, String> pair) {
                return pair.getValue();
            }

            @Override
            public Pair<Integer, String> fromString(String s) {
                return null;
            }
        });
        for (int i = 0; i < 20; i++) {
            int key = (i + 1) * 25;
            String value = key + "%";
            cbZoomTo.getItems().add(new Pair<>(key, value));
        }
        int scaleInPercent = (int) (initOptions.getScale() * 100);
        cbZoomTo.setValue(new Pair<>(scaleInPercent, scaleInPercent + "%"));
        spFitWidthTo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, initOptions.getPagesInRow()));
        spFitHeightTo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, initOptions.getPagesInColumn()));

        cbZoomTo.setDisable(initOptions.getScaleType() != ScaleType.ZOOM);
        spFitWidthTo.setDisable(initOptions.getScaleType() != ScaleType.FIT_WIDTH_TO_PAGES);
        spFitHeightTo.setDisable(initOptions.getScaleType() != ScaleType.FIT_HEIGHT_TO_PAGES);

        // listeners
        rbZoomTo.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) result.setScaleType(ScaleType.ZOOM);
            cbZoomTo.setDisable(!t1);
        });
        rbFitToSinglePage.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) result.setScaleType(ScaleType.FIT_TO_SINGLE_PAGE);
        });
        rbFitWidthTo.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) result.setScaleType(ScaleType.FIT_WIDTH_TO_PAGES);
            spFitWidthTo.setDisable(!t1);
        });
        rbFitHeightTo.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) result.setScaleType(ScaleType.FIT_HEIGHT_TO_PAGES);
            spFitHeightTo.setDisable(!t1);
        });
        cbZoomTo.valueProperty().addListener((observableValue, oldv, newv) -> result.setScale(newv.getKey() / 100f));
        spFitWidthTo.valueProperty().addListener(((observableValue, oldv, newv) -> result.setPagesInRow(newv)));
        spFitHeightTo.valueProperty().addListener(((observableValue, oldv, newv) -> result.setPagesInColumn(newv)));
    }
}
