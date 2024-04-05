package com.mindolph.mindmap;

import com.mindolph.mfx.drawing.CanvasGraphicsWrapper;
import com.mindolph.mfx.drawing.Graphics;
import com.mindolph.mfx.drawing.LayerCanvas;
import com.mindolph.mindmap.drawing.*;
import com.mindolph.mindmap.theme.DarkTheme;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MindMapDrawingDemo extends Application implements Initializable {

    @FXML
    private Canvas canvas;
    private LayerCanvas layerCanvas;

    private MindMapDrawingContext context;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com.mindolph.mindmap/mind_map_drawing_demo.fxml"));
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Hello Mind Map");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MindMapConfig config = new MindMapConfig();
        config.setTheme(new DarkTheme());
        config.setThemeName("Dark");
        config.getTheme().setDropShadow(true);
        context = new MindMapDrawingContext(config, config.getTheme());
        context.setDebugMode(true);
    }

    @FXML
    private void onLoad() {
        Rectangle2D rectHot = new Rectangle2D(0, 0, 600, 600);
        Graphics g = new CanvasGraphicsWrapper(canvas, rectHot);
        if (context.getConfig().getTheme().isDrawBackground()) {
            g.drawRect(new Rectangle2D(0, 0, 900, 900), null, context.getConfig().getTheme().getPaperColor());
        }
        g.drawRect(rectHot, Color.WHITE, null);

        layerCanvas = new LayerCanvas(g, context);

        // root
        RootTopicComponent t0 = new RootTopicComponent(50, 200, 200, 100);
        TopicText tt0 = new TopicText(0, 0, 200, 100);
        tt0.setText("This is root topic");
        t0.add(tt0);
        layerCanvas.add(t0);

        // first level
        FirstLevelTopicComponent t1 = new FirstLevelTopicComponent(300, 150, 200, 20);
        TopicText tt1 = new TopicText(0, 0, 200, 100);
        tt1.setText("This is first level topic,\nand should extend the parent component");
        t1.add(tt1);
        layerCanvas.add(t1);

        // other levels
        OtherLevelTopicComponent t2 = new OtherLevelTopicComponent(600, 100, 200, 100);
        TopicText tt2 = new TopicText(0, 0, 200, 100);
        tt2.setText("This is second level topic");
        t2.add(tt2);
        layerCanvas.add(t2);

        TopicConnector connector01 = new TopicConnector(t0, t1, 200, 50, 0, 50);
        TopicConnector connector02 = new TopicConnector(t1, t2, 200, 50, 0, 50);
        layerCanvas.add(connector01);
        layerCanvas.add(connector02);

        layerCanvas.updateAllBounds();
        layerCanvas.drawLayers();
    }

    public static class MindMapDrawingDemoLauncher {
        public static void main(String[] args) {
            Application.launch(MindMapDrawingDemo.class, args);
        }
    }
}
