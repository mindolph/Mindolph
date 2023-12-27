package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.AiInputDialog.Temperature;
import com.mindolph.base.genai.GenAiEvents.ActionType;
import com.mindolph.base.genai.GenAiEvents.OutputLength;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

/**
 * @author mindolph.com@gmail.com
 */
public class AiReframeDialog extends StackPane {

    @FXML
    private Button btnKeep;
    @FXML
    private Button btnRetry;
    @FXML
    private Button btnAdjust;
    @FXML
    private Button btnAbandon;
    @FXML
    private ProgressBar pbWaiting;

    private Object editorId;

    private String inputText;

    private ContextMenu adjustMenu;

    public AiReframeDialog(Object editorId, String inputText) {
        this.editorId = editorId;
        this.inputText = inputText;
        FxmlUtils.loadUri("/genai/ai_reframe_dialog.fxml", this);

        btnKeep.setGraphic(FontIconManager.getIns().getIcon(IconKey.YES));
        btnRetry.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnAdjust.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnAbandon.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));

        btnKeep.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.KEEP);
            this.working();
        });
        btnRetry.setOnAction(event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new GenAiEvents.Input(inputText, Temperature.DEFAULT.value(), null));// todo
            this.working();
        });
        btnAdjust.setOnMouseClicked(event -> {
            if (adjustMenu == null) {
                adjustMenu = createAdjustMenu();
                adjustMenu.show(btnAdjust, event.getScreenX(), event.getScreenY());
            }
            else {
                adjustMenu.getItems().clear();
                adjustMenu.hide();
            }
        });
        btnAdjust.setOnAction(event -> {
        });
        btnAbandon.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.ABANDON);
            this.working();
        });
    }

    private ContextMenu createAdjustMenu() {
        ContextMenu menu = new ContextMenu();
        EventHandler<ActionEvent> eventHandler = event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new GenAiEvents.Input(inputText, Temperature.DEFAULT.value(), null));// todo
            working();
        };
        MenuItem miShorter = new MenuItem("Shorter", FontIconManager.getIns().getIcon(IconKey.SHORT_TEXT));
        MenuItem miLonger = new MenuItem("Longer", FontIconManager.getIns().getIcon(IconKey.LONG_TEXT));
        miShorter.setUserData(OutputLength.SHORTER);
        miLonger.setUserData(OutputLength.LONGER);
        miShorter.setOnAction(eventHandler);
        miLonger.setOnAction(eventHandler);
        menu.getItems().addAll(miShorter, miLonger);
        return menu;
    }

    private void working() {
        btnKeep.setDisable(true);
        btnRetry.setDisable(true);
        btnAdjust.setDisable(true);
        btnAbandon.setDisable(true);
//        piProcessing.setDisable(false);
        pbWaiting.setVisible(true);
    }

}
