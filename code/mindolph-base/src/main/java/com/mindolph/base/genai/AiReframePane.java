package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.AiInputPane.Temperature;
import com.mindolph.base.genai.GenAiEvents.ActionType;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.GenAiEvents.OutputAdjust;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiReframePane extends StackPane {

    @FXML
    private Label lbIcon;
    @FXML
    private Button btnKeep;
    @FXML
    private Button btnRetry;
    @FXML
    private Button btnAdjust;
    @FXML
    private Button btnDiscard;
    @FXML
    private ProgressBar pbWaiting;

    private Object editorId;

    private String inputText;

    private float temperature;

    private ContextMenu adjustMenu;

    public AiReframePane(Object editorId, String inputText, float temperature) {
        this.editorId = editorId;
        this.inputText = inputText;
        this.temperature = temperature;
        FxmlUtils.loadUri("/genai/ai_reframe_pane.fxml", this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));

        btnKeep.setGraphic(FontIconManager.getIns().getIcon(IconKey.YES));
        btnRetry.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnAdjust.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnDiscard.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));

        btnKeep.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.KEEP);
            this.onWorking();
        });
        btnRetry.setOnAction(event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(inputText, Temperature.SAFE.value(), null));// todo
            this.onWorking();
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
        btnDiscard.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, ActionType.DISCARD);
            this.onWorking();
        });
    }

    private ContextMenu createAdjustMenu() {
        ContextMenu menu = new ContextMenu();
        EventHandler<ActionEvent> eventHandler = event -> {
            MenuItem mi = (MenuItem) event.getSource();
            GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(inputText, this.temperature, (OutputAdjust) mi.getUserData()));
            onWorking();
        };
        MenuItem miShorter = new MenuItem("Shorter", FontIconManager.getIns().getIcon(IconKey.SHORT_TEXT));
        MenuItem miLonger = new MenuItem("Longer", FontIconManager.getIns().getIcon(IconKey.LONG_TEXT));
        miShorter.setUserData(OutputAdjust.SHORTER);
        miLonger.setUserData(OutputAdjust.LONGER);
        miShorter.setOnAction(eventHandler);
        miLonger.setOnAction(eventHandler);
        menu.getItems().addAll(miShorter, miLonger);
        return menu;
    }

    private void onWorking() {
        btnKeep.setDisable(true);
        btnRetry.setDisable(true);
        btnAdjust.setDisable(true);
        btnDiscard.setDisable(true);
        pbWaiting.setVisible(true);
    }

    public void onStop(String reason) {
        pbWaiting.setVisible(false);
//        lbMsg.setText(reason);
        NodeUtils.enable(btnKeep, btnRetry, btnAdjust, btnDiscard);
    }

}
