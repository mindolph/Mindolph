package com.mindolph.genai;

import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.genai.GenaiUiConstants.MessageType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @since 1.13.0
 */
public class ChatPane extends BaseView {

    @FXML
    private VBox vbChat;
    @FXML
    private ScrollPane scrollPane;

    private TextFlow chatDisplay;

    private boolean isAutoScroll = true;


    public ChatPane() {
        super("/genai/chat_pane.fxml", false);
        scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < oldValue.doubleValue()) {
                pauseAutoScroll();
            }
        });
    }

    public void clearChatHistory() {
        vbChat.getChildren().clear();
        chatDisplay = null;
    }

    public void waitForAnswer() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setPrefSize(16, 16);
        vbChat.getChildren().add(pi);
    }

    public void endWaiting() {
        if (!vbChat.getChildren().isEmpty() && vbChat.getChildren().getLast() instanceof ProgressIndicator pi) {
            vbChat.getChildren().remove(pi);
        }
    }

    public synchronized void appendChatPartial(ChatPartial chatPartial) {
        this.endWaiting();
        if (chatDisplay == null) {
            // first partial arrived.
            HBox hBox = new HBox();
            Text text = new Text(chatPartial.getText());
            chatDisplay = new TextFlow(text);
            chatDisplay.setPadding(new Insets(5, 10, 5, 10));
            if (chatPartial.getType() == MessageType.HUMAN) {
                chatDisplay.setStyle(
                        "-fx-color: rgb(239, 242, 255);" +
                                "-fx-background-color: rgb(200, 200, 200);" +
                                "-fx-background-radius: 15px;");
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 20));
            }
            else if (chatPartial.getType() == MessageType.AI) {
                hBox.setAlignment(Pos.TOP_LEFT);
                hBox.setPadding(new Insets(5, 20, 5, 5));
                Label lblAvatar = new Label();
                lblAvatar.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEN_AI));
                hBox.getChildren().add(lblAvatar);
            }
            hBox.getChildren().add(chatDisplay);
            vbChat.getChildren().add(hBox);
            if (chatPartial.isLast()) {
                chatDisplay = null;
            }
        }
        else {
            chatDisplay.getChildren().add(new Text(chatPartial.getText()));
            if (chatPartial.isLast()) {
                chatDisplay = null;
            }
        }
    }

    public void scrollToBottom() {
        if (isAutoScroll) {
            scrollPane.setVvalue(1.0);
        }
    }

    public void pauseAutoScroll() {
        isAutoScroll = false;
    }

    public void resumeAutoScroll() {
        isAutoScroll = true;
    }

}
