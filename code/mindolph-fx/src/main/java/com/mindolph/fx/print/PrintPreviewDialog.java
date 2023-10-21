package com.mindolph.fx.print;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mindolph.com@gmail.com
 */
public class PrintPreviewDialog extends BaseDialogController<Void> {
    private static final Logger log = LoggerFactory.getLogger(PrintPreviewDialog.class);

    @FXML
    private ChoiceBox<Pair<Integer, String>> cbScale;

    @FXML
    private CheckBox cbDrawBorder;

    @FXML
    private ScalableScrollPane scrollPane;

    private final PrintPreviewView printPreviewView;

    private PrintOptions options = new PrintOptions();

    private final Printable printable;

    public PrintPreviewDialog(Printable printable) {
        this.printable = printable;
        super.dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Print Preview")
                .fxmlUri("print/print_preview.fxml")
                .buttons(ButtonType.CLOSE)
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .resizable(true)
                .controller(this)
                .build();

        cbScale.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<Integer, String> pair) {
                return pair.getValue();
            }

            @Override
            public Pair<Integer, String> fromString(String s) {
                return null;
            }
        });
        for (int i = 0; i < 8; i++) {
            int key = (i + 1) * 25;
            String value = key + "%";
            cbScale.getItems().add(new Pair<>(key, value));
        }
        cbScale.setValue(new Pair<>(100, 100 + "%"));

        printPreviewView = new PrintPreviewView(printable);
        scrollPane.setScalableView(printPreviewView);
        Bounds viewportBounds = scrollPane.getViewportBounds();
        printPreviewView.setViewportRectangle(new Rectangle2D(0, 0, viewportBounds.getWidth(), viewportBounds.getHeight()));
        scrollPane.calculateAndUpdateViewportRectangle();
    }

    @FXML
    public void onPrint(ActionEvent event) {
        printPreviewView.updateOptions(options);
        printPreviewView.print();
    }


    @FXML
    public void onPageSetup(ActionEvent event) {
        printPreviewView.setupPage();
    }

    @FXML
    public void onOptions(ActionEvent event) {
        PrintOptionsDialog dialog = new PrintOptionsDialog(options);
        PrintOptions useChoose = dialog.showAndWait();
        options = useChoose;
        printPreviewView.updateOptions(options);
//        SwingUtilities.invokeLater(() -> {
//            splitToPagesForCurrentFormat();
//            scrollPane.revalidate();
//        });
    }

    @FXML
    public void onDrawBorder(ActionEvent event) {
        printPreviewView.setIsDrawBorder(cbDrawBorder.isSelected());
        printPreviewView.repaint();
    }

    @FXML
    public void onScale(ActionEvent event) {
        if (printPreviewView != null) {
            double newScale = cbScale.getValue().getKey() / 100.f;
            log.debug("Scale to " + newScale);
            printPreviewView.setScale(newScale);
            printPreviewView.repaint();
        }
    }
}
