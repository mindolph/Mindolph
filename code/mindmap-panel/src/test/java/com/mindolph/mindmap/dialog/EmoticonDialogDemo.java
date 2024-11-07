package com.mindolph.mindmap.dialog;

import com.mindolph.mfx.preference.FxPreferences;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @since 1.10
 */
public class EmoticonDialogDemo extends  Application implements Initializable {

    @FXML
    private Button btnWithoutInitial;
    @FXML
    private Button btnWithInitial;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com.mindolph.mindmap/dialog/emoticon_dialog_demo.fxml"));
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Hello Mind Map");
        primaryStage.setScene(scene);
        primaryStage.show();
        FxPreferences.getInstance().init(this.getClass());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnWithoutInitial.setOnAction(event -> {
            EmoticonDialog dialog = new EmoticonDialog("emotion_beaten");
            String selectedName = dialog.showAndWait();
            System.out.println(selectedName);
        });
        btnWithInitial.setOnAction(event -> {
            EmoticonDialog dialog = new EmoticonDialog(null);
            String selectedName = dialog.showAndWait();
            System.out.println(selectedName);
        });
    }


    public static class EmoticonDialogDemoApp {
        public static void main(String[] args) {
            Application.launch(EmoticonDialogDemo.class, args);
        }
    }
}
