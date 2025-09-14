package com.mindolph.genai;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @since 1.13.0
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

    private StateMachine<ChatState, AgentMeta> chatStateMachine;

    public ChatView() {
        super("/genai/chat_view.fxml", false);
    }

    private void initStateMachine() {
        StateBuilder<ChatState, AgentMeta> builder = new StateBuilder<>();
        builder.initialize(ChatState.INIT)
                .in(p -> {
                    piAgent.setVisible(false);
                    piAgent.setManaged(false);
                    taInput.setDisable(true);
                    btnSend.setDisable(true);
                    lblAgent.setText("");
                })
                .state(ChatState.LOADING)
                .in(payload -> {
                    Platform.runLater(() -> {
                        lblAgent.setText("Loading...");
                        piAgent.setVisible(true);
                        piAgent.setManaged(true);
                        taInput.setDisable(true);
                    });
                })
                .state(ChatState.LOAD_FAILED)
                .in(payload -> {
                    lblAgent.setText("Fail to load agent: %s".formatted(payload.getName()));
                    piAgent.setVisible(false);
                    piAgent.setManaged(false);
                })
                .state(ChatState.READY)
                .in(p -> {
                    lblAgent.setText("%s: \n%s\n".formatted(currentAgentMeta.getChatProvider().name(), p.getChatModel()));
                    chatPane.setDisable(false);
                    taInput.setDisable(false);
                    taInput.setPromptText("Chat with your agent \"%s\"".formatted(p.getName()));
                    taInput.requestFocus();
                    btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
                    piAgent.setVisible(false);
                    piAgent.setManaged(false);
                })
                .state(ChatState.SWITCHING)
                .in(payload -> {
                    lblAgent.setText("Switching...");
                    piAgent.setVisible(true);
                    piAgent.setManaged(true);
                    taInput.setDisable(true);
                })
                .state(ChatState.SWITCH_FAILED)
                .in(payload -> {
                    lblAgent.setText("Fail");
                    piAgent.setVisible(false);
                    piAgent.setManaged(false);
                })
                .state(ChatState.TYPING)
                .in(p -> {
                    btnSend.setDisable(false);
                })
                .state(ChatState.CHATTING)
                .in(p -> {
                    taInput.setDisable(true);
                    btnSend.setDisable(true);
                    chatPane.scrollToBottom();
                    chatPane.waitForAnswer();
                })
                .state(ChatState.STREAMING)
                .in(p -> {
                    btnSend.setDisable(false);
                    btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.STOP));
                    chatPane.resumeAutoScroll();
                    chatPane.scrollToBottom();
                })
                .state(ChatState.STOPING)
                .in(p -> {
                    btnSend.setDisable(true);
                })
                .state(ChatState.STOPED)
                .in(p -> {
                    taInput.setDisable(false);
                    btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
                })
                .initialize(ChatState.INIT)
                .action("Load agent", ChatState.INIT, ChatState.LOADING)
                .action("Load agent fail", ChatState.LOADING, ChatState.LOAD_FAILED)
                .action("Load agent again", ChatState.LOAD_FAILED, ChatState.LOADING)
                .action("Load agent success", ChatState.LOADING, ChatState.READY)
                .action("Switch agent", ChatState.READY, ChatState.SWITCHING)
                .action("Switch agent success", ChatState.SWITCHING, ChatState.READY)
                .action("Switch agent fail", ChatState.SWITCHING, ChatState.SWITCH_FAILED)
                .action("Switch agent again", ChatState.SWITCH_FAILED, ChatState.SWITCHING)
                .action("User type(with original agent)", ChatState.SWITCH_FAILED, ChatState.TYPING)
                .action("User type", ChatState.READY, ChatState.TYPING)
                .action("User keep typing", ChatState.TYPING, ChatState.TYPING)
                .action("Send chat", ChatState.TYPING, ChatState.CHATTING)
                .action("Streaming response begin", ChatState.CHATTING, ChatState.STREAMING)
                .action("Streaming response", ChatState.STREAMING, ChatState.STREAMING)
                .action("Streaming response stop with failure", ChatState.STREAMING, ChatState.READY)
                .action("User stop streaming response", ChatState.STREAMING, ChatState.STOPING)
                .action("Streaming response is stoped", ChatState.STOPING, ChatState.STOPED)
                .action("Streaming response completed", ChatState.STREAMING, ChatState.READY)
                .action("User type again", ChatState.STOPED, ChatState.TYPING)
                .action("Switch agent on STOPPED state", ChatState.STOPED, ChatState.SWITCHING)
                .action("Switch agent on TYPING state", ChatState.TYPING, ChatState.SWITCHING);
        chatStateMachine = new StateMachine<>(builder);
        chatStateMachine.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initStateMachine();
        lblAgentIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.GEN_AI));
        cbAgent.setConverter(GenaiUiConstants.agentConverter);
        cbAgent.valueProperty().addListener((observable, oldValue, selectedAgent) -> {
            if (selectedAgent == null) {
                return;
            }
            // key is condition state, value is target state.
            chatStateMachine.postOnState(new HashMap<>() {
                {
                    put(ChatState.INIT, ChatState.LOADING);
                    put(ChatState.LOAD_FAILED, ChatState.LOADING);
                    put(ChatState.READY, ChatState.SWITCHING);
                    put(ChatState.SWITCH_FAILED, ChatState.SWITCHING);
                    put(ChatState.TYPING, ChatState.SWITCHING);
                    put(ChatState.STOPED, ChatState.SWITCHING);
                }
            });
            RagService.getInstance().listenOnProgressEvent(s -> {
                log.debug("RagService progress: {}", s);
            });
            RagService.getInstance().useAgent(selectedAgent.getValue(), (palyload) -> {
                if (palyload instanceof Exception e) {
                    log.error("Failed to use agent: %s".formatted(selectedAgent.getValue().getName()), e);
                    Platform.runLater(() -> {
                        chatStateMachine.postOnState(ChatState.LOAD_FAILED, ChatState.LOADING, ChatState.SWITCH_FAILED, ChatState.SWITCHING);
                        DialogFactory.errDialog("Failed to use agent: \n%s".formatted(e.getLocalizedMessage()));
                    });
                    return;
                }
                currentAgentMeta = selectedAgent.getValue();
                Platform.runLater(() -> {
                    chatPane.clearChatHistory();
                    ChatPartial cp = new ChatPartial("Ask me anything", MessageType.AI, true);
                    chatPane.appendChatPartial(cp);
                    chatStateMachine.postWithPayload(ChatState.READY, currentAgentMeta);
                });
            });
        });
        PluginEventBus.getIns().subscribePreferenceChanges(pluginEvent -> {
            if (pluginEvent.getEventType() == EventType.DATASET_PREF_CHANGED
                    || pluginEvent.getEventType() == EventType.AGENT_PREF_CHANGED
                    || pluginEvent.getEventType() == EventType.OPTIONS_PREF_CHANGED) {
                log.debug("Got preference change event: %s".formatted(pluginEvent.getEventType()));
                this.loadAgents();
            }
        });
        this.loadAgents();

        taInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isBlank(newValue)) {
                chatStateMachine.post(ChatState.TYPING);
            }
        });

        btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
        btnSend.setOnAction(event -> {
            if (chatStateMachine.isState(ChatState.STREAMING)) {
                // stop the streaming
                chatStateMachine.post(ChatState.STOPING);
                RagService.getInstance().stop();
            }
            else {
                if (currentAgentMeta == null) {
                    DialogFactory.infoDialog("Please choose an agent to chat with");
                    return;
                }
                if (StringUtils.isNotBlank(taInput.getText())) {
                    chatStateMachine.post(ChatState.CHATTING);
                    ChatPartial chat = new ChatPartial(taInput.getText(), MessageType.HUMAN);
                    chat.setLast(true);
                    taInput.clear();
                    chatPane.appendChatPartial(chat);
                    // to do the LLM chatting
                    RagService.getInstance().chat(chat.getText(), tokenStream -> {
                        tokenStream.onRetrieved(contents -> {
                                    log.debug("retrieved %d contents from embedding store".formatted(contents.size()));
                                    log.debug("with size: %s".formatted(contents.stream().map(c -> String.valueOf(c.textSegment().text().length())).collect(Collectors.joining(","))));
                                    Platform.runLater(() -> {
                                        chatStateMachine.post(ChatState.STREAMING);
                                    });
                                })
                                .onPartialResponse(s -> {
                                    Platform.runLater(() -> {
                                        ChatPartial chatPartial = new ChatPartial(s, MessageType.AI);
                                        chatPane.appendChatPartial(chatPartial);
                                        chatStateMachine.post(ChatState.STREAMING);
                                    });
                                })
                                .onCompleteResponse(resp -> {
                                    Platform.runLater(() -> {
                                        ChatPartial chatPartial = new ChatPartial(resp.aiMessage().text(), MessageType.AI);
                                        chatPartial.setLast(true);
                                        chatPane.appendChatPartial(chatPartial);
                                        chatPane.scrollToBottom();
                                        chatStateMachine.postWithPayloadOnState(ChatState.READY, ChatState.STREAMING, ChatState.STOPING, ChatState.STOPED, currentAgentMeta);
                                    });
                                })
                                .onError(e -> {
                                    Platform.runLater(() -> {
                                        // the STOP action might cause the LLM provider response with error, so it is inappropriate to show dialogs.
                                        // DialogFactory.errDialog(e.getMessage());
                                        chatStateMachine.postWithPayloadOnState(ChatState.READY, ChatState.STREAMING, ChatState.STOPED, ChatState.STOPING, currentAgentMeta);
                                    });
                                })
                                .start();
                    });
                }
                else {
                    taInput.setPromptText("Chat with your agent");
                }
            }
        });
    }

    private void loadAgents() {
        if (chatStateMachine.isStateIn(ChatState.LOADING, ChatState.SWITCHING)) {
            log.debug("The agent is loading...");
            return;
        }
        log.debug("Reload agents");
        Pair<String, AgentMeta> selectedItem = cbAgent.getSelectionModel().getSelectedItem();
        String agentId = null;
        if (selectedItem != null) {
            agentId = selectedItem.getKey();
        }

        cbAgent.getItems().clear();
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        cbAgent.getItems().addAll(agentMap.values().stream().map(agentMeta -> new Pair<>(agentMeta.getId(), agentMeta)).toList());
        if (StringUtils.isNotBlank(agentId)) {
            AgentMeta agentMeta = agentMap.get(agentId);
            if (agentMeta != null) {
                log.debug("Reload agent: %s".formatted(agentMeta.getName()));
                cbAgent.getSelectionModel().select(new Pair<>(agentId, agentMeta));
            }
        }
    }

    private enum ChatState {
        INIT, LOADING, LOAD_FAILED, READY, SWITCHING, SWITCH_FAILED, TYPING, CHATTING, STREAMING, STOPING, STOPED
    }
}
