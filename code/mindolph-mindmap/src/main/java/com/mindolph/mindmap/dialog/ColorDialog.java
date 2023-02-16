package com.mindolph.mindmap.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public class ColorDialog extends BaseDialogController<ColorDialog.ColorSet> {

    private final Logger log = LoggerFactory.getLogger(ColorDialog.class);

    @FXML
    private Label lblBorderColor;

    @FXML
    private Label lblFillColor;

    @FXML
    private Label lblTextColor;

    @FXML
    private ColorPicker cpBorderColor;

    @FXML
    private ColorPicker cpFillColor;

    @FXML
    private ColorPicker cpTextColor;

    private final ColorSet origin;

    public ColorDialog(String title, ColorSet origin) {
        this.origin = origin;
        super.result = origin;
//        Platform.runLater(() -> {
        dialog = new CustomDialogBuilder<ColorSet>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(StringUtils.abbreviate(title, 36))
                .fxmlUri("dialog/color_setting_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .defaultValue(origin)
                .controller(ColorDialog.this)
                .build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> cpBorderColor.requestFocus());
        });
        cpBorderColor.setOnAction(event -> {
            log.debug("select " + cpBorderColor.getValue());
            result.setBorderColor(cpBorderColor.getValue());
        });
        cpFillColor.setOnAction(event -> {
            log.debug("select " + cpFillColor.getValue());
            result.setFillColor(cpFillColor.getValue());
        });
        cpTextColor.setOnAction(event -> {
            log.debug("select " + cpTextColor.getValue());
            result.setTextColor(cpTextColor.getValue());
        });
//        });
    }

    @Override
    public void show(Consumer<ColorSet> callback) {
        cpBorderColor.setValue((origin.borderColor));
        cpFillColor.setValue((origin.fillColor));
        cpTextColor.setValue((origin.textColor));
        Optional<ColorSet> optColorSet = dialog.showAndWait();
        if (callback != null && optColorSet.isPresent()) {
            callback.accept(optColorSet.get());
        }
    }

    /**
     * Set of AWT color(for compatible with MindMapPanel)
     */
    public static class ColorSet {
        Color borderColor;
        Color fillColor;
        Color textColor;

        public ColorSet(Color borderColor, Color fillColor, Color textColor) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.textColor = textColor;
        }

        public ColorSet setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public ColorSet setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public ColorSet setTextColor(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public Color getFillColor() {
            return fillColor;
        }

        public Color getTextColor() {
            return textColor;
        }
    }
}
