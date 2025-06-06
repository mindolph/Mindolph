package com.mindolph;

import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.fx.view.FileSelectView;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FileSelectViewDemo extends Application implements Initializable {

    public static class Launcher {
        public static void main(String[] args) {
            Application.launch(FileSelectViewDemo.class, args);
        }
    }

    @FXML
    private FileSelectView fileSelectView;
//    private CheckBoxTreeItem<String> rootItem;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/file_select_view_demo.fxml"));
        Scene scene = new Scene(root, 800, 480);
        primaryStage.setTitle("Hello " + FileSelectViewDemo.class.getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("ready");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        new Thread(() -> {
//            Platform.runLater(() -> {
//                CheckBoxTreeItem<NodeData> subItem = new CheckBoxTreeItem<>(new NodeData("SUB"));
//                fileSelectView.getRootItem().getChildren().add(subItem);
//            });
//
//        }).start();
        WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(JSON);
        fileSelectView.loadWorkspace(workspaceList.getProjects().getFirst(), null, true, false, null);
    }

    String JSON = """
            {
              "projects": [
                {
                  "baseDirPath": "/Users/allen/Temp/mindolph-workspace-demo"
                },
                {
                  "baseDirPath": "/Users/allen/Workspace/github/Mindolph/DemoWorkspace"
                },
                {
                  "baseDirPath": "/Users/allen/Temp/mindolph/test-create-workspace"
                }
              ]
            }
            """;
}
