package com.mindolph.base.canvas;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.util.FxImageUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Demo to show how to use BaseScalableView(Skin) with ScalableScrollPane.
 *
 * @author mindolph.com@gmail.com
 */
public class CanvasDemo implements Initializable {
    private final Logger log = LoggerFactory.getLogger(CanvasDemo.class);


    @FXML
    private Button btnSnapshot;

    @FXML
    private Label statusBar;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox vbox;

    public static boolean isDrawContent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnSnapshot.setOnAction(event -> {

        });

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.GREEN);

        gc.fillText("hello", 20, 20);

        gc.translate(100, 100);
        Text icon = FontIconManager.getIns().getIcon(IconKey.FILE_LINK);

        PathElement[] selectionShape = icon.getSelectionShape();
        System.out.println(selectionShape.length);
        gc.beginPath();
        for (PathElement element : selectionShape) {
            System.out.println(element);
            if (element instanceof MoveTo) {
                gc.moveTo(((MoveTo) element).getX(), ((MoveTo) element).getY());
            }
            else if (element instanceof LineTo) {
                gc.lineTo(((LineTo) element).getX(), ((LineTo) element).getY());
            }
        }
        gc.stroke();
        gc.fill();
        gc.closePath();

        // test font icon
        Text t = FontIconManager.getIns().getIcon(IconKey.FILE_LINK);
        ImageView iv = new ImageView();

//        l.setGraphic(t);
//        vbox.getChildren().add(l);
        StackPane sp = new StackPane(t);
        sp.layout();
        System.out.println(sp.getLayoutBounds());
        WritableImage snapshot = new WritableImage(100, 100);
        iv.snapshot(null, snapshot);
        System.out.println(snapshot.getWidth() + " " + snapshot.getHeight());
        System.out.println(FxImageUtils.dumpImage(snapshot));

//        gc.drawImage(iconImage, 300, 300);
//        WritableImage img = canvas.snapshot(null, null);
//        System.out.println(FxImageUtils.dumpImage(img));

    }

}
