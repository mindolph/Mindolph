package com.mindolph.base.control;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.editor.MarkdownCodeArea;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.Generator;
import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.mindolph.base.constant.ShortcutConstants.KEY_MD_COMMENT;
import static javafx.scene.input.KeyCombination.META_DOWN;

/**
 * test key event handler
 *
 * @author mindolph.com@gmail.com
 * @since 1.14.1
 *
 */
public class MarkdownCodeAreaDemo implements Initializable {

    @FXML
    private VBox vbox;

    @FXML
    private MarkdownCodeArea mdCodeArea;

    @FXML
    private Button btnEnable;

    @FXML
    private Button btnGetCaretPosition;

    @FXML
    private ToggleButton tbtnDisableUndo;
    @FXML
    private ToggleButton tbtnDisableRedo;

    static {
        // testing for macOS
        ShortcutManager.getIns().addShortCut(KEY_MD_COMMENT, new KeyCodeCombination(KeyCode.SLASH, META_DOWN));
    }

    @FXML
    public void onUndo() {
        mdCodeArea.undo();
    }

    @FXML
    public void onRedo() {
        mdCodeArea.redo();
    }

    @FXML
    public void onDisableUndo() {

    }

    @FXML
    public void onDisableRedo() {

    }

    @FXML
    public void onEnabled() {
        if (mdCodeArea.isDisabled()) {
            mdCodeArea.setDisable(false);
            btnEnable.setText("Disable");
        }
        else {
            mdCodeArea.setDisable(true);
            btnEnable.setText("Enable");
        }
    }

    @FXML
    private void onGetCaretPosition() {
        Platform.runLater(() -> {
            int caretPosition = mdCodeArea.getCaretPosition();
            System.out.println("caretPosition: " + caretPosition);
            btnEnable.requestFocus();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        vbox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            System.out.println("Got event: " + event);
        });

        mdCodeArea.disableUndoProperty().bind(tbtnDisableUndo.selectedProperty());
        mdCodeArea.disableRedoProperty().bind(tbtnDisableRedo.selectedProperty());

        mdCodeArea.disableUndoProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("disableUndo: " + newValue);
        });
        mdCodeArea.disableRedoProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("disableRedo: " + newValue);
        });

        // register plugins TODO
        PluginManager.getIns().registerPlugin(new TestPlugin());
        mdCodeArea.appendText("""
                foo
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                bar                                                                                                  foo
                """);
    }

    private static class TestPlugin extends BasePlugin {

        @Override
        public Integer getOrder() {
            return 100;
        }

        @Override
        public Collection<String> supportedFileTypes() {
            return SupportFileTypes.EDITABLE_TYPES;
        }

        @Override
        public Optional<InputHelper> getInputHelper() {
            return Optional.of(new InputHelper() {
                @Override
                public List<String> getHelpWords(Object editorId) {
                    return null;
                }

                @Override
                public void updateContextText(Object editorId, String text) {

                }
            });
        }

        @Override
        public Optional<Generator> getGenerator(Object editorId, String fileType) {
            // TODO
            return Optional.empty();
        }
    }

}
