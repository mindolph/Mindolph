package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.container.ScalableScrollPane;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.view.AttributesView;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.ClasspathResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.ShortcutConstants.KEY_MD_COMMENT;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.META_DOWN;

/**
 * @author mindolph.com@gmail.com
 */
public class ExtraMindMapViewDemo extends Application implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ExtraMindMapViewDemo.class);
    @FXML
    private ScalableScrollPane scrollPane;
    @FXML
    private AttributesView attributesView;

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
        // set the mind map view to attributes view, so that attributes view can notify mind map view to save file when note changes.
        attributesView.setMindMapView(extraMindMapView);
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
        extraMindMapView.selectionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.size() != 1) {
                attributesView.loadAttributes(null);
            }
            else {
                TopicNode topic = newValue.getFirst();
                log.debug("On selected topic: " + StringUtils.abbreviate(topic.getText(), 100));
                attributesView.loadAttributes(topic);
            }
        });
        extraMindMapView.setModelChangedEventHandler(() -> {
            log.info("Mind map model changed");
        });
    }

    @FXML
    public void onUndo(ActionEvent event) {
        this.extraMindMapView.undo();
    }

    @FXML
    public void onRedo(ActionEvent event) {
        this.extraMindMapView.redo();
    }


    @Override
    public void start(Stage stage) throws Exception {
        // for i18n
        FxPreferences.getInstance().init(ExtraMindMapViewDemo.class);
        List<String> bundleNames = List.of("i18n/mindmap-panel", "i18n/mindolph-base");
        I18nHelper.getInstance().addBundles(bundleNames);
        I18nHelper.getInstance().reloadAllBundles();
        System.out.println(I18nHelper.getInstance().get("search.bar.find"));
        // for Markdown editor.
        ShortcutManager sm = ShortcutManager.getIns();
        if (SystemUtils.IS_OS_MAC) {
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, META_DOWN));
        }
        else if (SystemUtils.IS_OS_LINUX) {
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
        }
        // start
        Parent root = FXMLLoader.load(getClass().getResource("/extra_mind_map_view_demo.fxml"),
                I18nHelper.getInstance().getResourceBundle());
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Hello Extra Mind Map");
        stage.setScene(scene);
        stage.show();
    }

    public static class ExtraMindMapViewDemoLauncher {

        public static void main(String[] args) {
            launch(ExtraMindMapViewDemo.class, args);
        }
    }
}
