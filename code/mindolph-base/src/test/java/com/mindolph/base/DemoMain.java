package com.mindolph.base;

import com.mindolph.base.util.FxImageUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

/**
 * Main entrance of demo for mindolph-core.
 */
public class DemoMain extends Application {

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
        FxmlUtils.loadUriToStage("/dialog/dialog_demo.fxml").show();
    }


    @FXML
    public void onFixedSplitPaneTest(ActionEvent event){
        FxmlUtils.loadUriToStage("/container/fixed_splitpane_demo.fxml").show();
    }

    @FXML
    public void onSplitPaneTest(ActionEvent event) {
        FxmlUtils.loadUriToStage("/container/hidden_splitpane_demo.fxml").show();
    }

    @FXML
    public void onScrollableImageView(ActionEvent event) {
        FxmlUtils.loadUriToStage("/control/scrollable_image_view_demo.fxml").show();
    }

    @FXML
    public void onScalableView(ActionEvent event) {
        FxmlUtils.loadUriToStage("/control/scalable_view_demo.fxml").show();
    }

    @FXML
    private void onCanvasDemo() {
        FxmlUtils.loadUriToStage("/canvas/canvas_demo.fxml").show();
    }


    public static class TestLauncher {

        public static void main(String[] args) {
            launch(DemoMain.class, args);
        }
    }
}