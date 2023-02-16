package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.mindmap.model.TopicNode;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.swiftboot.util.ClasspathResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class ExtraMindMapViewDemo extends Application implements Initializable {

    @FXML
    private ScalableScrollPane scrollPane;

    private ExtraMindMapView extraMindMapView;
    private MindMapConfig mindMapConfig;
    private MindMap<TopicNode> mindMap;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mindMapConfig = new MindMapConfig();
        extraMindMapView = new ExtraMindMapView();
        extraMindMapView.setFocusTraversable(true);
        extraMindMapView.setConfig(mindMapConfig);
        scrollPane.setScalableView(extraMindMapView);
    }

    @FXML
    public void onBig(ActionEvent event) {
        try {
            InputStream inputStream = ClasspathResourceUtils.openResourceStream("big.mmd");
            StringReader reader = new StringReader(new String(inputStream.readAllBytes()));
            mindMap = new MindMap<>(reader, RootTopicCreator.defaultCreator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        extraMindMapView.setDiagramEventHandler(rootToCenter -> {
            scrollPane.calculateAndUpdateViewportRectangle();
            if (rootToCenter) {
                extraMindMapView.rootToCentre();
            }
        });
        extraMindMapView.loadModel(mindMap);
        scrollPane.calculateAndUpdateViewportRectangle();
    }

    @FXML
    public void onSmall(ActionEvent event) {
        try {
            InputStream inputStream = ClasspathResourceUtils.openResourceStream("small.mmd");
            StringReader reader = new StringReader(new String(inputStream.readAllBytes()));
            mindMap = new MindMap<>(reader, RootTopicCreator.defaultCreator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        extraMindMapView.loadModel(mindMap);
        scrollPane.calculateAndUpdateViewportRectangle();
    }


    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/extra_mind_map_view_demo.fxml"));
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello Extra Mind Map");
        stage.setScene(scene);
        stage.show();
    }

    public static class TestLauncher {

        public static void main(String[] args) {
            launch(ExtraMindMapViewDemo.class, args);
        }
    }
}
