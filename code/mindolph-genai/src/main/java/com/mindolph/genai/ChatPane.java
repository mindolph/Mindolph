package com.mindolph.genai;

import com.mindolph.base.BaseView;
import com.mindolph.genai.GenaiUiConstants.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @since unknown
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

    public synchronized void appendChatPartial(ChatPartial chatPartial) {
        if (chatDisplay == null) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            Text text = new Text(chatPartial.getText());
            chatDisplay = new TextFlow(text);
            chatDisplay.setStyle(
                    "-fx-color: rgb(239, 242, 255);" +
                            "-fx-background-color: rgb(15, 125, 242);" +
                            "-fx-background-radius: 20px;");
            chatDisplay.setPadding(new Insets(5, 10, 5, 10));
            if (chatPartial.getType() == MessageType.HUMAN) {
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 20));
            }
            else if (chatPartial.getType() == MessageType.AI) {
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5, 20, 5, 5));
            }
            Platform.runLater(() -> {
                hBox.getChildren().add(chatDisplay);
                vbChat.getChildren().add(hBox);
                if (chatPartial.isLast()) {
                    chatDisplay = null;
                }
            });
        }
        else {
            Platform.runLater(() -> {
                chatDisplay.getChildren().add(new Text(chatPartial.getText()));
                if (chatPartial.isLast()) {
                    chatDisplay = null;
                }
            });
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
