package com.mindolph.mindmap.legacy;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.FontUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This dialog is only for supporting traditional AWT font.
 *
 * @author mindolph.com@gmail.com
 */
public class AwtFontSelectDialog extends BaseDialogController<Font> {

    @FXML
    ChoiceBox<Pair<String, String>> cbFontFamily;

    @FXML
    ChoiceBox<Pair<Integer, String>> cbFontStyle;

    @FXML
    ChoiceBox<Pair<Integer, Integer>> cbFontSize;

    @FXML
    TextArea textArea;

    private final StringConverter<Pair<String, String>> fontFamilyConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<String, String> object) {
            return object.getValue();
        }

        @Override
        public Pair<String, String> fromString(String string) {
            return null;
        }
    };

    private final StringConverter<Pair<Integer, String>> fontStyleConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<Integer, String> object) {
            return object.getValue();
        }

        @Override
        public Pair<Integer, String> fromString(String string) {
            return null;
        }
    };

    private final StringConverter<Pair<Integer, Integer>> fontSizeConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<Integer, Integer> object) {
            return String.valueOf(object.getValue());
        }

        @Override
        public Pair<Integer, Integer> fromString(String string) {
            return null;
        }
    };


    public AwtFontSelectDialog(Font font) {
        super.result = font;
        dialog = new CustomDialogBuilder<Font>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Select Font")
                .fxmlUri("dialog/font_select_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .defaultValue(font)
                .controller(this)
                .build();


        String[] availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        cbFontFamily.setConverter(fontFamilyConverter);
        cbFontFamily.getItems().addAll(Arrays.stream(availableFontFamilyNames).map(s -> new Pair<>(s, s)).toList());
        cbFontFamily.setValue(new Pair<>(font.getFamily(), font.getFamily()));

        List<Pair<Integer, String>> styles = Arrays.asList(
                new Pair<>(Font.PLAIN, "Plain"),
                new Pair<>(Font.BOLD, "Bold"),
                new Pair<>(Font.ITALIC, "Italic"),
                new Pair<>(Font.BOLD | Font.ITALIC, "Bold+Italic")
        );
        cbFontStyle.setConverter(fontStyleConverter);
        cbFontStyle.getItems().addAll(styles);
        cbFontStyle.setValue(styles.stream().filter(pair -> pair.getKey().equals(font.getStyle())).findFirst().orElse(new Pair<>(Font.PLAIN, "Plain")));

        List<Pair<Integer, Integer>> size = new ArrayList<>();
        for (int i = 8; i < 32; i++) {
            size.add(new Pair<>(i, i));
        }
        size.addAll(Arrays.asList(
                new Pair<>(36, 36),
                new Pair<>(40, 40),
                new Pair<>(44, 44),
                new Pair<>(48, 48),
                new Pair<>(56, 56),
                new Pair<>(64, 64),
                new Pair<>(80, 80),
                new Pair<>(96, 96)
        ));
        cbFontSize.setConverter(fontSizeConverter);
        cbFontSize.getItems().addAll(size);
        cbFontSize.setValue(size.stream().filter(pair -> pair.getKey().equals(font.getSize())).findFirst().orElse(new Pair<>(16, 16)));

        // Add listeners after data inited.
        cbFontFamily.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        cbFontStyle.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        cbFontSize.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        updateTextArea();
    }

    private void updateTextArea() {
        Pair<String, String> fontFamily = cbFontFamily.getSelectionModel().getSelectedItem();
        Pair<Integer, String> fontStyle = cbFontStyle.getSelectionModel().getSelectedItem();
        Pair<Integer, Integer> fontSize = cbFontSize.getSelectionModel().getSelectedItem();
        if (fontSize == null) {
            DialogFactory.errDialog("Font size not selected");
        }
        Font awtFont = new Font(fontFamily.getKey(), fontStyle.getKey(), fontSize.getKey());
        javafx.scene.text.Font fxFont = FontUtils.awtFontToFxFont(awtFont);
        textArea.setFont(fxFont);
        super.result = awtFont;
    }

}
