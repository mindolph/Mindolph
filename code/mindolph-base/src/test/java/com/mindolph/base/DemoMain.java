package com.mindolph.base;

import com.mindolph.base.util.FxImageUtils;
import com.mindolph.base.util.MindolphFileUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;

import static com.mindolph.mfx.util.FxmlUtils.loadUriToStage;

/**
 * Main entrance of demo for mindolph-core.
 */
public class DemoMain extends Application {

    public static class TestLauncher {
        public static void main(String[] args) {
            launch(DemoMain.class, args);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/demo_main.fxml"));
        Scene scene = new Scene(root, 800, 480);
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();
        WritableImage img = new WritableImage(300, 300);
        WritableImage snapshot = scene.snapshot(img);
        FxImageUtils.dumpImage(snapshot);
    }

    @FXML
    public void onDialogTest(ActionEvent event) {
        loadUriToStage("/dialog/dialog_demo.fxml").show();
    }


    @FXML
    public void onFixedSplitPaneTest(ActionEvent event) {
        loadUriToStage("/container/fixed_splitpane_demo.fxml").show();
    }

    @FXML
    public void onSplitPaneTest(ActionEvent event) {
        loadUriToStage("/container/hidden_splitpane_demo.fxml").show();
    }

    @FXML
    public void onScrollableImageView(ActionEvent event) {
        loadUriToStage("/control/scrollable_image_view_demo.fxml").show();
    }

    @FXML
    public void onIconView(ActionEvent event) {
        loadUriToStage("/control/icon_view_demo.fxml").show();
    }

    @FXML
    public void onScalableView(ActionEvent event) {
        loadUriToStage("/control/scalable_view_demo.fxml").show();
    }

    @FXML
    private void onCanvasDemo() {
        loadUriToStage("/canvas/canvas_demo.fxml").show();
    }

    @FXML
    private void onExtTableView() {
        Stage stage = loadUriToStage("/control/ext_table_view_demo.fxml");
        stage.show();
    }

    @FXML
    private void onExtCodeArea() {
        loadUriToStage("/control/smart_code_area_demo.fxml").show();
    }

    @FXML
    private void onFont() {
        String path = "/System/Library/Fonts/Supplemental/AppleMyungjo.ttf";
        File f = new File(path);
        System.out.println(f.exists());
        System.out.println(f.canRead());
        try {
            Font font = Font.loadFont(new FileInputStream(f), 10.0);
            System.out.println(font);
            Text text = new Text("你好");
//            text.getStyleClass().add("glyph-icon");
//            text.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %s;", "AppleMyungjo", 15));
            WritableImage result = new WritableImage(100, 100);
            WritableImage img = text.snapshot(null, result);
            File snapshotFile = MindolphFileUtils.getTempFile("font-snapshot.png");
            System.out.println(snapshotFile);
            com.mindolph.mfx.util.FxImageUtils.writeImage(img, snapshotFile);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void onSyncScroll() {
        loadUriToStage("/sync_demo.fxml").show();
    }
}