package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.base.util.MindolphFileUtils;
import com.mindolph.core.search.SearchUtils;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.theme.LightTheme;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class MindMapViewDemo extends Application implements Initializable, EventHandler<ActionEvent> {

    private final Logger log = LoggerFactory.getLogger(MindMapViewDemo.class);

    @FXML
    private ScalableScrollPane scrollPane;
    @FXML
    private Button btnZoomIn;
    @FXML
    private Button btnZoomOut;
    @FXML
    private Button btnResetZoom;
    @FXML
    private Button btnRepaint;
    @FXML
    private Button btnResize;
    @FXML
    private Button btnSnapshot;
    @FXML
    private Button btnFitToViewportWidth;
    @FXML
    private Button btnFitToViewportHeight;
    @FXML
    private Label label;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea textArea;
    @FXML
    private Label statusBar;
    @FXML
    private CheckBox showStatusBar;
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;

    private MindMapView mindMapView;
    private MindMapConfig mindMapConfig;

    private MindMap<TopicNode> mindMap;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com.mindolph.mindmap/mind_map_view_demo.fxml"));
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Hello Mind Map");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mindMapView = new MindMapView();
        mindMapView.setFocusTraversable(true);
//        myScrollPane.setContent(mindMapView);
        scrollPane.setScalableView(mindMapView);
        mindMapView.layout();
        // events
        btnZoomIn.setOnAction(this);
        btnZoomOut.setOnAction(this);
        btnResetZoom.setOnAction(this);
        btnResize.setOnAction(event -> {
            log.info("[DEMO] Resize scroll pane requested.");
            scrollPane.setPrefWidth(scrollPane.getWidth() + 100f);
            logContentBounds();
        });
        btnSnapshot.setOnAction(event -> {
            WritableImage writableImage = mindMapView.takeViewportSnapshot();
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
        btnFitToViewportWidth.setOnAction(event -> mindMapView.fitToViewportWidth());
        btnFitToViewportHeight.setOnAction(event -> mindMapView.fitToViewportHeight());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = SearchUtils.string2pattern(newValue, Pattern.CASE_INSENSITIVE);
            TextSearchOptions options = new TextSearchOptions();
            options.setCaseSensitive(true);
            options.setInTopic(true);
            mindMapView.findTopicByPattern(pattern, options, false);
        });
        showStatusBar.selectedProperty().addListener((observableValue, aBoolean, t1) -> statusBar.setVisible(t1));

        // undo/redo
        btnUndo.setOnAction((event) -> {
            mindMapView.undo();
        });
        btnRedo.setOnAction((event -> {
            mindMapView.redo();
        }));

        this.registerMindMapListeners();
        logContentBounds();
        mindMapView.setConfig(new MindMapConfig());
        mindMapView.getConfig().setTheme(new LightTheme());
        logAny("[DEMO] Ready.");
    }

    private void registerMindMapListeners() {
        mindMapView.dimensionProperty().addListener((observableValue, oldDim, newDim) -> {
            // log.debug("[DEMO] Mind map dimension changes from %s to %s".formatted(dimensionInStr(oldDim), dimensionInStr(newDim)));
            logContentBounds();
        });

        // Listen for logging
        mindMapView.scaleProperty().addListener((observable, oldScale, newScale) -> logAny("Set scale to: %.2f".formatted(newScale.doubleValue())));

        mindMapView.setModelChangedEventHandler(() -> infoAny("Model is changed"));
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
        mindMapView.setDiagramEventHandler((rootToCenter) -> {
            log.debug("[DEMO] setDiagramEventHandler: calculate and update viewport rectangle");
            scrollPane.calculateAndUpdateViewportRectangle();
            if (rootToCenter) {
                mindMapView.rootToCentre();
            }
        });
        mindMapView.loadModel(mindMap);
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
        mindMapView.setDiagramEventHandler((boolean rootToCenter) -> {
            scrollPane.calculateAndUpdateViewportRectangle();
            if (rootToCenter) {
                mindMapView.rootToCentre();
            }
        });
        mindMapView.loadModel(mindMap);
        scrollPane.calculateAndUpdateViewportRectangle();
    }

    @Override
    public void handle(ActionEvent event) {
        Button button = (Button) event.getSource();
        log.debug("[DEMO] Button clicked: " + button);
        double oldScale = mindMapView.getScale();
        double newScale = 1;
        if (button == btnZoomIn) {
            newScale = mindMapView.getScale() + 0.1;
        }
        else if (button == btnZoomOut) {
            newScale = mindMapView.getScale() - 0.1;
        }
        else if (button == btnResetZoom) {
            newScale = 1.0f;
        }
        else {
            newScale = 1.0f;
        }
        Dimension2D oldDim = mindMapView.getDimension();
        if (mindMapView.setScale(newScale)) {
            logAny("Try to set scale to: %.2f".formatted(newScale));
            Dimension2D newDim = mindMapView.getDimension();
            mindMapView.scrollInCenter(oldDim, newDim);
        }
        else {
            logAny("Try to set scale to %.2f but failed.".formatted(newScale));
        }
    }

    private void logAny(String text) {
        textArea.appendText("\n");
        textArea.appendText(text);
    }

    private void infoViewport() {
        Bounds viewportBounds = scrollPane.getViewportBounds();
        log.debug(String.valueOf(viewportBounds));
        label.setText(String.format("Viewport min bounds: %.1f, %.1f", viewportBounds.getMinX(), viewportBounds.getMinY()));
    }

    private void infoAny(String text) {
        label.setText(text);
    }

    private void logContentBounds() {
        Bounds layoutBounds = mindMapView.getLayoutBounds();
        textArea.appendText("\n");
        textArea.appendText(String.format("Content bounds: %.1fx%.1f", layoutBounds.getWidth(), layoutBounds.getHeight()));
    }

    private void logScroll(String requested) {
        textArea.appendText("\n");
        textArea.appendText(String.format("Scroll requested to %s actually %.3f, %.3f",
                requested, scrollPane.getHvalue(), scrollPane.getVvalue()));
        textArea.appendText("\n");
    }

    private void updateLabel() {
        Bounds layoutBounds = mindMapView.getLayoutBounds();
        Point2D mouseInViewport = null;
        Point2D mouseInCanvas = null;
        label.setText(String.format("Pos in canvas: %.1fx%.1f, Pos in viewport: %.1fx%.1f, Scale: %.3f  Content: %.1fx%.1f, scroll: %.3f, %.3f:",
                mouseInCanvas == null ? 0 : mouseInCanvas.getX(), mouseInCanvas == null ? 0 : mouseInCanvas.getY(),
                mouseInViewport == null ? 0 : mouseInViewport.getX(), mouseInViewport == null ? 0 : mouseInViewport.getY(),
                mindMapView.getScale(),
                layoutBounds.getWidth(), layoutBounds.getHeight(),
                scrollPane.getHvalue(), scrollPane.getVvalue()));
        textArea.appendText("\n");
        textArea.appendText(String.format("scroll: %.3f, %.3f", scrollPane.getHvalue(), scrollPane.getVvalue()));
    }

    public static class MindMapViewDemoLauncher {
        public static void main(String[] args) {
            Application.launch(MindMapViewDemo.class, args);
        }
    }
}
