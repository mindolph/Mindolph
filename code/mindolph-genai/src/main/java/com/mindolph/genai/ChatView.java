package com.mindolph.genai;

import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.RagService;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.genai.GenaiUiConstants.MessageType;
import com.mindolph.mfx.dialog.DialogFactory;
import dev.langchain4j.service.TokenStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since unknown
 */
public class ChatView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    @FXML
    private ChoiceBox<Pair<String, AgentMeta>> cbAgent;
    @FXML
    private Label lblAgent;
    @FXML
    private ChatPane chatPane;
    @FXML
    private TextArea taInput;
    @FXML
    private Button btnSend;

    private AgentMeta currentAgentMeta;

    public ChatView() {
        super("/genai/chat_view.fxml", false);

        cbAgent.setConverter(GenaiUiConstants.agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, newValue) -> {
            RagService.getInstance().useAgent(newValue.getValue());
            currentAgentMeta = newValue.getValue();
            String lb = "%s: %s\n %d files".formatted(currentAgentMeta.getProvider().getName(), currentAgentMeta.getChatModel().name(), currentAgentMeta.getFiles().size());
            lblAgent.setText(lb);
        });
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getName(), agentMeta)).toList());

        btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
        btnSend.setOnAction(event -> {
            if (currentAgentMeta == null) {
                DialogFactory.infoDialog("Please choose an agent to chat with");
                return;
            }
            if (StringUtils.isNotBlank(taInput.getText())) {
                ChatPartial chat = new ChatPartial(taInput.getText(), MessageType.HUMAN);
                chat.setLast(true);
                Platform.runLater(() -> {

                });
                chatPane.appendChatPartial(chat);
                taInput.clear();
                chatPane.scrollToBottom();
                // to do real LLM chatting
                TokenStream tokenStream = RagService.getInstance().chat(chat.getText());
                tokenStream.onRetrieved(contents -> {
                            log.debug("retrieved %d contents from embedding store".formatted(contents.size()));
                            log.debug("with size: %s".formatted(contents.stream().map(c -> String.valueOf(c.textSegment().text().length())).collect(Collectors.joining(","))));
                            chatPane.resumeAutoScroll();
                        })
                        .onPartialResponse(s -> {
                            ChatPartial chatPartial = new ChatPartial(s, MessageType.AI);
                            chatPane.appendChatPartial(chatPartial);
                            chatPane.scrollToBottom();
                        })
                        .onCompleteResponse(resp -> {
                            ChatPartial chatPartial = new ChatPartial(resp.aiMessage().text(), MessageType.AI);
                            chatPartial.setLast(true);
                            chatPane.appendChatPartial(chatPartial);
                            chatPane.scrollToBottom();
                        })
                        .onError(e -> DialogFactory.errDialog(e.getMessage()))
                        .start();
            }
            else {
                taInput.setPromptText("Chat with your agent");
            }
        });
    }
}
