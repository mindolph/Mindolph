package com.mindolph.base.control;

import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.base.util.MindolphFileUtils;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.FxImageUtils;
import com.mindolph.mfx.util.RectangleUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Demo to show how to use BaseScalableView(Skin) with ScalableScrollPane.
 *
 * @author mindolph.com@gmail.com
 */
public class ScalableViewDemo implements Initializable {
    private final Logger log = LoggerFactory.getLogger(ScalableViewDemo.class);

    @FXML
    private ScalableScrollPane scrollPane;

    @FXML
    private Button btnSnapshot;

    @FXML
    private CheckBox switchDrawContent;

    @FXML
    private CheckBox showStatusBar;

    @FXML
    private Label statusBar;

    public static boolean isDrawContent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DemoScalableView demoScalableView = new DemoScalableView();
        //scrollPane.setContent(demoScalableView);
        scrollPane.setScalableView(demoScalableView);

        switchDrawContent.selectedProperty().addListener((observableValue, aBoolean, newChecked) -> {
            isDrawContent = newChecked;
            demoScalableView.forceRefresh();
        });

        showStatusBar.selectedProperty().addListener((observableValue, aBoolean, isShow) -> {
            statusBar.setVisible(isShow);
        });

        btnSnapshot.setOnAction(event -> {
            WritableImage writableImage = demoScalableView.takeViewportSnapshot();
            File snapshotFile = MindolphFileUtils.getTempFile("snapshot.png");
            try {
                if (snapshotFile != null) {
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", snapshotFile);
                }
                log.info("[DEMO] Snapshot file saved to: " + snapshotFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static class DemoScalableView extends BaseScalableView {
        private Image image;

        public DemoScalableView() {
            try {
                image = FxImageUtils.readImageFromResource("/img_medium.jpg");// TODO
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public WritableImage takeSnapshot() {
            return (WritableImage) image;
        }

        @Override
        protected DemoScalableViewSkin createDefaultSkin() {
            return new DemoScalableViewSkin(this);
        }

        public Image getImage() {
            return image;
        }
    }

    public static class DemoScalableViewSkin extends BaseScalableViewSkin<DemoScalableView> {
        private final Logger log = LoggerFactory.getLogger(DemoScalableViewSkin.class);

        public DemoScalableViewSkin(DemoScalableView control) {
            super(control);
        }

        @Override
        public void reLayout(double newScale) {
            Image image = control.getImage();
            if (image != null) {
                double w = newScale * image.getWidth();
                double h = newScale * image.getHeight();
                this.control.setPrefSize(w, h);
                this.control.getParent().layout(); // this is important, may cause parent container aware that the bounds is changed.
                this.control.setDimension(new Dimension2D(w, h)); // listeners will act by the dimension changes.
                this.control.fitViewport();
            }
        }

        @Override
        protected void drawBackground() {
            super.drawBackground();
            Bounds bounds = this.control.getLayoutBounds();
            Rectangle2D vr = this.control.getViewportRectangle();
            double scale = this.control.getScale();
            final double scaledGridSize = GRID_SIZE * scale;

//            log.debug("[DEMO] Draw rectangle for control bounds: %s".formatted(RectangleUtils.rectangleInStr(vr)));

            log.debug("[DEMO] Draw bounds for control: %s".formatted(BoundsUtils.boundsInString(bounds)));
            log.debug("[DEMO] Draw viewport: %s".formatted(RectangleUtils.rectangleInStr(vr)));
            gc.setFill(Color.PINK);
            gc.fillRect(bounds.getMinX() - vr.getMinX(), bounds.getMinY() - vr.getMinY(), bounds.getWidth(), bounds.getHeight());

            // draw lines (the grid size changed with scale)
            super.translateGraphicsContext(false);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            // Choose different begin coordinate and dimension because drawing in viewport and over viewport are different.
            double beginX = vr.getMinX() < 0 ? vr.getMinX() : 0;
            double beginY = vr.getMinY() < 0 ? vr.getMinY() : 0;
            double totalWidth = vr.getWidth() + (vr.getMinX() < 0 ? 0 : vr.getMinX());
            double totalHeight = vr.getHeight() + (vr.getMinY() < 0 ? 0 : vr.getMinY());
            // Draw vertical lines (only within the viewport)
            log.debug("[DEMO] Draw vertical line from (%.1f, %.1f)(%.1f x %.1f)".formatted(beginX, beginY, totalWidth, totalHeight));
            for (double x = beginX; x < totalWidth; x += scaledGridSize) {
                if (x > vr.getMinX()) {
                    double y1 = vr.getMinY();
                    gc.strokeLine(x, limitY(y1), x, totalHeight);
                    gc.strokeText(String.valueOf(Integer.valueOf((int) (x / scaledGridSize))), x, gc.getFont().getSize());
                }
            }
            // Draw horizontal lines (only within the viewport)
            for (double y = beginY; y < totalHeight; y += scaledGridSize) {
                if (y > vr.getMinY()) {
                    double x1 = vr.getMinX();
                    gc.strokeLine(limitX(x1), y, totalWidth, y);
                    gc.strokeText(String.valueOf(Integer.valueOf((int) (y / scaledGridSize))), 0, y);
                }
            }
            super.translateGraphicsContext(true);
            // draw canvas border
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        protected void drawContent() {
            if (isDrawContent) {
                super.translateGraphicsContext(false);
                Image image = control.getImage();
                if (image != null && !image.isError()) {
                    gc.drawImage(image, 0, 0, this.control.getScale() * image.getWidth(), this.control.getScale() * image.getHeight());
                }
                super.translateGraphicsContext(true);
            }
        }
    }
}
