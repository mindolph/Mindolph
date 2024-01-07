package com.mindolph.print;

import com.mindolph.fx.print.PrintOptionsDialog;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.fx.print.PrintOptions;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.base.print.PrinterManager;
import com.mindolph.fx.print.ImagePrintable;
import com.mindolph.fx.print.MindMapPrintable;
import com.mindolph.fx.print.PrintPreviewView;
import com.mindolph.fx.print.Printable;
import com.mindolph.mfx.util.FxImageUtils;
import com.mindolph.mindmap.RootTopicCreator;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class PrintDemo extends Application implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(PrintDemo.class);

    @FXML
    private ScalableScrollPane scrollPane;

    @FXML
    private CheckBox cbDrawBorder;

    private PrintPreviewView printPreviewView;
    private Printable printable;
    private PrintOptions options = new PrintOptions();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/print/print_demo.fxml"));
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Hello Print");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Printer printer = PrinterManager.getInstance().getFirstPrinter();
            PageLayout pageLayout = printer.getDefaultPageLayout();
            if (pageLayout == null){
                throw new RuntimeException("Failed get page layout from printer");
            }
            if (true) {
                // 2214x820
                //2575.0 x 852.0
                InputStream inputStream = ClasspathResourceUtils.openResourceStream("print/print_demo.mmd");
                StringReader reader = new StringReader(new String(inputStream.readAllBytes()));
                MindMap mindMap = new MindMap(reader, RootTopicCreator.defaultCreator);
                printable = new MindMapPrintable(mindMap, pageLayout);
            }
            else {
                // 1000 x 607
                Image image = FxImageUtils.readImageFromResource("/print/print_demo.jpg");
                log.debug("Image size %s x %s%n".formatted(image.getWidth(), image.getHeight()));
                printable = new ImagePrintable(image, pageLayout);
            }
            printPreviewView = new PrintPreviewView(printable);
            scrollPane.setScalableView(printPreviewView);
            Bounds viewportBounds = scrollPane.getViewportBounds();
            printPreviewView.setViewportRectangle(new Rectangle2D(0, 0, viewportBounds.getWidth(), viewportBounds.getHeight()));
            scrollPane.calculateAndUpdateViewportRectangle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onPageSetup(ActionEvent event) {
        printPreviewView.setupPage();
    }

    @FXML
    public void onPrintSetup(ActionEvent event) {
        PrintOptionsDialog dialog = new PrintOptionsDialog(options);
        PrintOptions useChoose = dialog.showAndWait();
        options = useChoose;
        printPreviewView.updateOptions(options);
    }

    @FXML
    public void onPrint(ActionEvent event) {
        printPreviewView.updateOptions(options);
        printPreviewView.print();
    }

    @FXML
    public void onDrawBorder(ActionEvent event) {
        printPreviewView.setIsDrawBorder(cbDrawBorder.isSelected());
        printPreviewView.repaint();
    }

    public static class PrintDemoLauncher {
        public static void main(String[] args) {
            Application.launch(PrintDemo.class, args);
        }
    }
}
