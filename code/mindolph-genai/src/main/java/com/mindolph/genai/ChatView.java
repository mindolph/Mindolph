package com.mindolph.genai;

import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.RagService;
import com.mindolph.base.plugin.PluginEvent.EventType;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.genai.GenaiUiConstants.MessageType;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @since unknown
 */
public class ChatView extends BaseView implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    @FXML
    private Label lblAgentIcon;
    @FXML
    private ChoiceBox<Pair<String, AgentMeta>> cbAgent;
    @FXML
    private ProgressIndicator piAgent;
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblAgentIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEN_AI));
        cbAgent.setConverter(GenaiUiConstants.agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Platform.runLater(() -> {
                chatPane.clearChatHistory();
                piAgent.setVisible(true);
                piAgent.setManaged(true);
                taInput.setDisable(true);
            });
            RagService.getInstance().listenOnProgressEvent(s -> {
                log.debug("RagService progress: {}", s);
            });
            RagService.getInstance().useAgent(newValue.getValue(), (palyload) -> {
                if (palyload instanceof Exception e) {
                    log.error("Failed to use agent: %s".formatted(newValue.getValue().getName()), e);
                    Platform.runLater(() -> {
                        piAgent.setVisible(false);
                        piAgent.setManaged(false);
                        DialogFactory.errDialog("Failed to use agent: \n%s".formatted(e.getLocalizedMessage()));
                    });
                    return;
                }
                currentAgentMeta = newValue.getValue();
                Platform.runLater(() -> {
                    piAgent.setVisible(false);
                    piAgent.setManaged(false);
                    String lb = "%s: %s\n".formatted(currentAgentMeta.getProvider().getName(), currentAgentMeta.getChatModel().getName());
                    lblAgent.setText(lb);
                    ChatPartial cp = new ChatPartial("Ask me anything", MessageType.AI, true);
                    chatPane.appendChatPartial(cp);
                    taInput.setDisable(false);
                    taInput.requestFocus();
                    updateNoticeInformation(currentAgentMeta.getName());
                });
            });
        });
        PluginEventBus.getIns().subscribePreferenceChanges(pluginEvent -> {
            if (pluginEvent.getEventType() == EventType.AGENT_PREF_CHANGED
                    || pluginEvent.getEventType() == EventType.MODEL_PREF_CHANGED) {
                this.loadAgents();
            }
        });
        this.loadAgents();

        taInput.textProperty().addListener((observable, oldValue, newValue) -> {
            btnSend.setDisable(StringUtils.isBlank(newValue));
        });

        btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
        btnSend.setOnAction(event -> {
            if (currentAgentMeta == null) {
                DialogFactory.infoDialog("Please choose an agent to chat with");
                return;
            }
            if (StringUtils.isNotBlank(taInput.getText())) {
                ChatPartial chat = new ChatPartial(taInput.getText(), MessageType.HUMAN);
                chat.setLast(true);
                chatPane.appendChatPartial(chat);
                taInput.clear();
                chatPane.scrollToBottom();
                chatPane.waitForAnswer();
                // to do real LLM chatting
                RagService.getInstance().chat(chat.getText(), tokenStream -> {
                    tokenStream.onRetrieved(contents -> {
                                log.debug("retrieved %d contents from embedding store".formatted(contents.size()));
                                log.debug("with size: %s".formatted(contents.stream().map(c -> String.valueOf(c.textSegment().text().length())).collect(Collectors.joining(","))));
                                chatPane.resumeAutoScroll();
                            })
                            .onPartialResponse(s -> {
                                Platform.runLater(() -> {
                                    ChatPartial chatPartial = new ChatPartial(s, MessageType.AI);
                                    chatPane.appendChatPartial(chatPartial);
                                    chatPane.scrollToBottom();
                                });
                            })
                            .onCompleteResponse(resp -> {
                                Platform.runLater(() -> {
                                    ChatPartial chatPartial = new ChatPartial(resp.aiMessage().text(), MessageType.AI);
                                    chatPartial.setLast(true);
                                    chatPane.appendChatPartial(chatPartial);
                                    chatPane.scrollToBottom();
                                });
                            })
                            .onError(e -> {
                                Platform.runLater(() -> {
                                    DialogFactory.errDialog(e.getMessage());
                                    // TODO reset the UI
                                });
                            })
                            .start();
                });
            }
            else {
                taInput.setPromptText("Chat with your agent");
            }
        });
    }

    private void loadAgents() {
        cbAgent.getItems().clear();
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getName(), agentMeta)).toList());
    }

    public void updateNoticeInformation(String agentName) {
        taInput.setPromptText("Chat with your agent \"%s\"".formatted(agentName));
    }
}
