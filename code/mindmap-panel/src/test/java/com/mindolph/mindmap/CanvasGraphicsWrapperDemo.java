package com.mindolph.mindmap;

import com.mindolph.base.graphic.CanvasGraphicsWrapper;
import com.mindolph.base.constant.StrokeType;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class CanvasGraphicsWrapperDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    Canvas canvas;
    Pane pane;
    ScrollPane scrollPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        canvas = new Canvas();
        canvas.setHeight(512);
        canvas.setWidth(512);

        CanvasGraphicsWrapper wrapper = new CanvasGraphicsWrapper(canvas);
        wrapper.setStroke(2, StrokeType.SOLID);
        wrapper.drawLine(0, 0, 512, 30, Color.BLACK);
        wrapper.setStroke(2, StrokeType.DASHES);
        wrapper.drawLine(0, 0, 512, 60, Color.BLACK);
        wrapper.setStroke(2, StrokeType.DOTS);
        wrapper.drawLine(0, 0, 512, 90, Color.BLACK);

        wrapper.setStroke(2, StrokeType.SOLID);
        wrapper.drawRect(10, 10, 200, 100, Color.BLUE, Color.GREEN);

        wrapper.setStroke(2, StrokeType.SOLID);
        wrapper.drawRect(10, 10, 200, 100, Color.BLUE, null);

        wrapper.drawString("Hello Canvas", 30, 30, Color.RED);

        wrapper.drawCurve(220, 120, 400, 200, Color.RED);

//        InputStream resourceAsStream = getClass().getResourceAsStream("/icon/emotion/3d_glasses.png");
//        BufferedImage image = ImageIO.read(resourceAsStream);
        Image image = new Image(getClass().getResourceAsStream("/icon/emotion/3d_glasses.png"));
        wrapper.drawImage(image, 300, 300);

        // shape
        Rectangle shape = new Rectangle(220, 120, 80, 50);
        shape.setArcWidth(10.0f);
        shape.setArcHeight(10.0f);
        wrapper.draw(shape, Color.RED, null);

        // translate
        wrapper.translate(400, 400);
        wrapper.drawString("Translated Text", 30, 30, Color.RED);

        wrapper.translate(-200, -200);
        wrapper.drawString("Un Translated Text", 30, 30, Color.RED);

        pane = new Pane(canvas);
        scrollPane = new ScrollPane(pane);
        scrollPane.viewportBoundsProperty().addListener((observableValue, bounds, t1) -> {
            System.out.println("viewport changed");
            doLayout();
        });
        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void doLayout() {
        Bounds viewportBounds = scrollPane.getViewportBounds();
        pane.setPrefSize(viewportBounds.getWidth(), viewportBounds.getHeight());
    }

    private static class Launcher {
        public static void main(String[] args) {
            Application.launch(CanvasGraphicsWrapperDemo.class, args);
        }
    }
}
