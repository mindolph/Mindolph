package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.InputBuilder;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
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
    @FXML
    private Label lbMsg;

    private final Object editorId;

    private final Input input;

    private ContextMenu adjustMenu;

    public AiReframePane(Object editorId, Input input, int outputTokens) {
        this.editorId = editorId;
        this.input = input;
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
            GenAiEvents.getIns().emitGenerateEvent(editorId,
                    new InputBuilder().model(input.model()).text(input.text()).temperature(input.temperature())
                            .maxTokens(input.maxTokens()).outputAdjust(null).isRetry(true).isStreaming(input.isStreaming())
                            .createInput());
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
            GenAiEvents.getIns().emitGenerateEvent(editorId,
                    new InputBuilder().model(input.model()).text(input.text()).temperature(input.temperature())
                            .maxTokens(input.maxTokens()).outputAdjust((OutputAdjust) mi.getUserData()).isRetry(true).isStreaming(input.isStreaming())
                            .createInput());
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
