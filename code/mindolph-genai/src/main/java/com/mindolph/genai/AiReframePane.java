package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import static com.mindolph.core.constant.GenAiConstants.MAX_GENERATION_TOKENS;

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
    @FXML
    private Label lbMsg;

    private Object editorId;

    private String inputText;

    private float temperature;

    private ContextMenu adjustMenu;

    public AiReframePane(Object editorId, String inputText, float temperature, int outputTokens) {
        this.editorId = editorId;
        this.inputText = inputText;
        this.temperature = temperature;
        FxmlUtils.loadUri("/genai/ai_reframe_pane.fxml", this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        lbMsg.setText("Done with %d tokens".formatted(outputTokens));

        btnKeep.setGraphic(FontIconManager.getIns().getIcon(IconKey.YES));
        btnRetry.setGraphic(FontIconManager.getIns().getIcon(IconKey.REFRESH));
        btnAdjust.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEAR));
        btnDiscard.setGraphic(FontIconManager.getIns().getIcon(IconKey.DELETE));

        btnKeep.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, GenAiConstants.ActionType.KEEP);
            this.onWorking();
        });
        btnRetry.setOnAction(event -> {
            GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(inputText, this.temperature, MAX_GENERATION_TOKENS, null, true, true));
            this.onWorking();
        });
        btnAdjust.setOnMouseClicked(event -> {
            if (adjustMenu == null) {
                adjustMenu = createAdjustMenu();
            }
            adjustMenu.show(btnAdjust, event.getScreenX(), event.getScreenY());
        });
        btnDiscard.setOnAction(event -> {
            GenAiEvents.getIns().emitActionEvent(editorId, GenAiConstants.ActionType.DISCARD);
            this.onWorking();
        });
    }

    private ContextMenu createAdjustMenu() {
        ContextMenu menu = new ContextMenu();
        EventHandler<ActionEvent> eventHandler = event -> {
            MenuItem mi = (MenuItem) event.getSource();
            GenAiEvents.getIns().emitGenerateEvent(editorId, new Input(inputText, this.temperature, MAX_GENERATION_TOKENS, (OutputAdjust) mi.getUserData(), true, true));
            onWorking();
        };
        MenuItem miShorter = new MenuItem("Shorter", FontIconManager.getIns().getIcon(IconKey.SHORT_TEXT));
        MenuItem miLonger = new MenuItem("Longer", FontIconManager.getIns().getIcon(IconKey.LONG_TEXT));
        miShorter.setUserData(GenAiConstants.OutputAdjust.SHORTER);
        miLonger.setUserData(GenAiConstants.OutputAdjust.LONGER);
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
        lbMsg.setText(null);
    }

    public void onStop(String reason) {
        pbWaiting.setVisible(false);
        lbMsg.setText(reason);
        NodeUtils.enable(btnKeep, btnRetry, btnAdjust, btnDiscard);
    }

}
