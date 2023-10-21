package com.mindolph.base.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.FontUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author mindolph.com@gmail.com
 */
public class FontSelectDialog extends BaseDialogController<Font> {

    private static final Logger log = LoggerFactory.getLogger(FontSelectDialog.class);

    @FXML
    ChoiceBox<Pair<String, String>> cbFontFamily;

    @FXML
    ChoiceBox<Pair<FontWeight, String>> cbFontStyle;

    @FXML
    CheckBox cbItalic;

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

    private final StringConverter<Pair<FontWeight, String>> fontStyleConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<FontWeight, String> object) {
            return object.getValue();
        }

        @Override
        public Pair<FontWeight, String> fromString(String string) {
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

    /**
     *
     * @param font initialize with font
     */
    public FontSelectDialog(Font font) {
        log.debug("Load dialog with font: " + font);
        dialog = new CustomDialogBuilder<Font>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Select Font")
                .fxmlUri("dialog/font_select_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .defaultValue(font)
                .controller(this)
                .build();

        List<String> families = Font.getFamilies();

//        String[] availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        cbFontFamily.setConverter(fontFamilyConverter);
        cbFontFamily.getItems().addAll(families.stream().map(family -> new Pair<>(family, family)).collect(Collectors.toList()));
        cbFontFamily.setValue(new Pair<>(font.getFamily(), font.getFamily()));

        List<Pair<FontWeight, String>> styles = Arrays.asList(
                new Pair<>(FontWeight.NORMAL, "Regular"),
                new Pair<>(FontWeight.BOLD, "Bold")
        );
        cbFontStyle.setConverter(fontStyleConverter);
        cbFontStyle.getItems().addAll(styles);
        cbFontStyle.setValue(styles.stream().filter(pair -> pair.getKey() == FontUtils.fontWeight(font.getStyle()))
                .findFirst().orElse(new Pair<>(FontWeight.NORMAL, "Regular")));

        cbItalic.setSelected(FontUtils.fontPosture(font.getStyle()) == FontPosture.ITALIC);

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
        cbFontSize.setValue(size.stream().filter(pair -> pair.getKey().equals(Double.valueOf(font.getSize()).intValue()))
                .findFirst().orElse(new Pair<>(16, 16)));

        // Add listeners after data inited.
        cbFontFamily.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        cbFontStyle.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        cbItalic.selectedProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        cbFontSize.valueProperty().addListener((observable, oldValue, newValue) -> updateTextArea());
        updateTextArea();
    }

    private void updateTextArea() {
        log.debug("Update text area");
        Pair<String, String> fontFamily = cbFontFamily.getSelectionModel().getSelectedItem();
        Pair<FontWeight, String> fontStyle = cbFontStyle.getSelectionModel().getSelectedItem();
        Pair<Integer, Integer> fontSize = cbFontSize.getSelectionModel().getSelectedItem();
        boolean italic = cbItalic.isSelected();
        if (fontSize == null) {
            DialogFactory.errDialog("Font size not selected");
            return;
        }
        Font font = Font.font(fontFamily.getKey(), fontStyle.getKey(), italic ? FontPosture.ITALIC : FontPosture.REGULAR, fontSize.getKey());
        log.debug(String.valueOf(font));
        textArea.setFont(font);
        super.result = font;
    }

}
