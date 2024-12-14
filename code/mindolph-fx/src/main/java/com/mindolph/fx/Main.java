package com.mindolph.fx;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.event.WindowEventHandler;
import com.mindolph.fx.helper.SceneRestore;
import com.mindolph.fx.helper.WindowRestoreListener;
import com.mindolph.fx.preference.Rectangle2DStringConverter;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.MessageTextBlockDialog;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.RectangleUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.pref.StringConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

import static com.mindolph.base.constant.ShortcutConstants.KEY_MD_COMMENT;
import static com.mindolph.plantuml.constant.ShortcutConstants.KEY_PUML_COMMENT;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.META_DOWN;

public class Main extends Application implements WindowRestoreListener {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // SceneRestore as the window change events handler.
    private final WindowEventHandler windowEventHandler = SceneRestore.getInstance();

    private Window window;
    private Scene scene;
    private Rectangle2D winRect = new Rectangle2D(0, 0, 0, 0);

    public Main() {
        // debug system out
//        CustomOutputStream cos = new CustomOutputStream(log);
//        System.setErr(new PrintStream(cos));
//        System.setOut(new PrintStream(cos));
//        System.out.println("XXXXXXXXXX");


        FxPreferences.getInstance().addConverter(Rectangle2D.class, new Rectangle2DStringConverter());
        FxPreferences.getInstance().addConverter(Orientation.class, new StringConverter<Orientation>() {

            @Override
            public Orientation deserialize(String prefValue) {
                return Orientation.valueOf(prefValue);
            }

            @Override
            public String serialize(Orientation valueObject) {
                return valueObject.name();
            }
        });
        ShortcutManager sm = ShortcutManager.getIns();
        // puml
        if (SystemUtils.IS_OS_MAC) {
            sm.addShortCut(KEY_PUML_COMMENT, new KeyCodeCombination(KeyCode.SLASH, META_DOWN));
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, META_DOWN));
        }
        else if (SystemUtils.IS_OS_LINUX) {
            sm.addShortCut(KEY_PUML_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            sm.addShortCut(KEY_PUML_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
            sm.addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, ALT_DOWN));
        }

        if (sm.hasConflict()) {
            throw new RuntimeException("There are shortcuts conflicts");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // start is called on the FX Application Thread,
        // so Thread.currentThread() is the FX application thread:
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Mindolph Error: ", throwable);
            MessageTextBlockDialog dialog = new MessageTextBlockDialog(this.window, "Unexpected Exception",
                    "An unexpected exception occurred",
                    ExceptionUtils.getStackTrace(throwable),
                    false);
            dialog.showAndWait();
        });

        // register restore listeners to restore scene.
        SceneRestore.getInstance().setWindowRestoreListener(this);

        try {
            log.info("load main scene");
            URL resource = getClass().getResource("/main.fxml");
            if (resource == null) {
                throw new RuntimeException("fxml file not found");
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            primaryStage.setTitle("Mindolph");
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/app_512.png")));
            scene = new Scene(root, 1100, 800);
            scene.getStylesheets().add(getClass().getResource("/style/syntax.css").toExternalForm());
//            scene.getStylesheets().add(getClass().getResource("/style/markdown_syntax.css").toExternalForm());
//            scene.getStylesheets().add(getClass().getResource("/style/plantuml_syntax.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/style/editor.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/text_editor.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/editor/csv_editor.css").toExternalForm());
            //scene.getStylesheets().add(getClass().getResource("/style/dark.css").toExternalForm());

            primaryStage.setScene(scene);

            MainController controller = loader.getController();
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                ActionEvent actionEvent = new ActionEvent();
                controller.onMenuExit(actionEvent);
            });
            primaryStage.show();
            // listen events to save scene states
            window = scene.getWindow();
            window.xProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(newValue, oldValue))
                    winRect = RectangleUtils.newWithX(winRect, newValue.doubleValue());
                windowEventHandler.onWindowResized(winRect);
            });
            window.yProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(newValue, oldValue))
                    winRect = RectangleUtils.newWithY(winRect, newValue.doubleValue());
                windowEventHandler.onWindowResized(winRect);
            });
            window.widthProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(newValue, oldValue))
                    winRect = RectangleUtils.newWithWidth(winRect, newValue.doubleValue());
                windowEventHandler.onWindowResized(winRect);
            });
            window.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(newValue, oldValue))
                    winRect = RectangleUtils.newWithHeight(winRect, newValue.doubleValue());
                windowEventHandler.onWindowResized(winRect);
            });
            DialogFactory.DEFAULT_WINDOW = window;
            // begin to restore scene.
            SceneRestore.getInstance().restoreScene();
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.errDialog("Failed to startup Mindolph for unexpected exception: \n" + e.getLocalizedMessage());
        }
    }


    @Override
    public void onWindowRestore(Rectangle2D rectangle) {
        log.info("Restore to : " + rectangle);
        winRect = rectangle;
        this.window.setX(rectangle.getMinX());
        this.window.setY(rectangle.getMinY());
        this.window.setWidth(rectangle.getWidth());
        this.window.setHeight(rectangle.getHeight());
    }

    /**
     * THIS IS FOR DEBUGGING STDOUT OF THE BUNDLED VERSION.
     */
    static class CustomOutputStream extends OutputStream {
        Logger logger;
        StringBuilder stringBuilder;

        public CustomOutputStream(Logger logger) {
            this.logger = logger;
            stringBuilder = new StringBuilder();
        }

        @Override
        public final void write(int i) throws IOException {
            char c = (char) i;
            if (c == '\r' || c == '\n') {
                if (!stringBuilder.isEmpty()) {
                    logger.info(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }
            }
            else
                stringBuilder.append(c);
        }
    }
}
