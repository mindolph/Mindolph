package com.mindolph.genai;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.container.FixedSplitPane;
import com.mindolph.base.genai.rag.RagService;
import com.mindolph.base.plugin.PluginEvent.EventType;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.genai.GenaiUiConstants.MessageType;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.ShortcutConstants.KEY_AGENT_SEND;
import static com.mindolph.genai.GenaiUiConstants.PAYLOAD_VECTOR_DB;

/**
 * @since 1.13.0
 */
public class ChatView extends BaseView implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    @FXML
    private AgentSelector agentSelector;
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
    @FXML
    private Button btnClear;
    @FXML
    private FixedSplitPane splitPanel;

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
                .in(selectedAgent -> {
                    lblAgent.setText("%s: %s".formatted(selectedAgent.getChatProvider().getDisplayName(), selectedAgent.getChatModel()));
                    chatPane.setDisable(false);
                    taInput.setDisable(false);
                    taInput.setPromptText("Chat with your agent \"%s\", hit %s to send your message.".formatted(selectedAgent.getName(), ShortcutManager.getIns().getKeyCombination(KEY_AGENT_SEND)));
                    taInput.requestFocus();
                    btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
                    btnSend.setDisable(true);
                    btnClear.setDisable(true);
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
                    btnClear.setDisable(false);
                })
                .state(ChatState.CHATTING)
                .in(p -> {
                    taInput.setDisable(true);
                    btnSend.setDisable(true);
                    btnClear.setDisable(true);
                    chatPane.scrollToBottom();
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
                .state(ChatState.STOPPED)
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
                .action("User clear input", ChatState.TYPING, ChatState.READY)
                .action("Send chat", ChatState.TYPING, ChatState.CHATTING)
                .action("Streaming response begin", ChatState.CHATTING, ChatState.STREAMING)
                .action("Streaming response", ChatState.STREAMING, ChatState.STREAMING)
                .action("Streaming response stop with failure", ChatState.STREAMING, ChatState.READY)
                .action("User stop streaming response", ChatState.STREAMING, ChatState.STOPING)
                .action("Streaming response is stopped", ChatState.STOPING, ChatState.STOPPED)
                .action("Streaming response completed", ChatState.STREAMING, ChatState.READY)
                .action("User type again", ChatState.STOPPED, ChatState.TYPING)
                .action("Switch agent on STOPPED state", ChatState.STOPPED, ChatState.SWITCHING)
                .action("Switch agent on TYPING state", ChatState.TYPING, ChatState.SWITCHING);
        chatStateMachine = new StateMachine<>(builder);
//        chatStateMachine.setSilent(true);
        chatStateMachine.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initStateMachine();
        agentSelector.setConverter(GenaiUiConstants.agentConverter);
        agentSelector.valueProperty().addListener((observable, oldSelectedAgent, selectedAgent) -> {
            if (selectedAgent == null || selectedAgent == oldSelectedAgent) {
                return;
            }

            AgentMeta selectedAgentMeta = selectedAgent.getValue();

            // key is condition state, value is target state.
            chatStateMachine.postOnState(new HashMap<>() {
                {
                    put(ChatState.INIT, ChatState.LOADING);
                    put(ChatState.LOAD_FAILED, ChatState.LOADING);
                    put(ChatState.READY, ChatState.SWITCHING);
                    put(ChatState.SWITCH_FAILED, ChatState.SWITCHING);
                    put(ChatState.TYPING, ChatState.SWITCHING);
                    put(ChatState.STOPPED, ChatState.SWITCHING);
                }
            });
            RagService.getInstance().useAgent(selectedAgentMeta, (payload) -> {
                if (payload instanceof Exception e) {
                    log.error("Failed to use agent: %s".formatted(selectedAgentMeta.getName()), e);
                    Platform.runLater(() -> {
                        chatStateMachine.postWithPayloadOnState(ChatState.LOAD_FAILED, ChatState.LOADING, ChatState.SWITCH_FAILED, ChatState.SWITCHING, selectedAgentMeta);
//                        DialogFactory.errDialog("Failed to use agent: \n%s".formatted(e.getLocalizedMessage()));
                        Notifications.create().title("Use Agent").text("Failed to use agent: \n%s".formatted(e.getLocalizedMessage())).showWarning();
                    });
                    return;
                }

                Platform.runLater(() -> {
                    if (currentAgentMeta == null || !selectedAgentMeta.getId().equals(currentAgentMeta.getId())) {
                        chatPane.clearChatHistory();
                        ChatPartial cp = new ChatPartial("I'm %s, ask me anything.".formatted(selectedAgentMeta.getName()), MessageType.AI, true);
                        chatPane.appendChatPartial(cp);
                    }
                    chatStateMachine.postWithPayload(ChatState.READY, selectedAgentMeta);
                    currentAgentMeta = selectedAgentMeta;
                });
            });
        });
        PluginEventBus.getIns().subscribePreferenceChanges(pluginEvent -> {
            log.debug("Got preference change event: %s".formatted(pluginEvent.getEventType()));
            log.debug("Payload: %s".formatted(pluginEvent.getPayload()));
            if (pluginEvent.getEventType() == EventType.AGENT_PREF_CHANGED) {
                this.loadAgents();
            }
            else if (pluginEvent.getEventType() == EventType.DATASET_PREF_CHANGED) {
                if (pluginEvent.getPayload() instanceof DatasetMeta dm) {
                    // Only changes to the dataset contained in the agent will trigger an agent reload.
                    log.debug("Dataset '%s'(%s) is changed".formatted(dm.getName(), dm.getId()));
                    log.debug(StringUtils.join(currentAgentMeta.getDatasetIds(), ","));
                    if (currentAgentMeta.getDatasetIds().contains(dm.getId())) {
                        this.loadAgents();
                    }
                }
            }
            else if (pluginEvent.getEventType() == EventType.OPTIONS_PREF_CHANGED) {
                if (PAYLOAD_VECTOR_DB.equals(pluginEvent.getPayload())) {
                    this.loadAgents();
                }
            }
        });
        this.loadAgents();

        splitPanel.setFixedSize(100);
        splitPanel.setFixed(splitPanel.getSecondary());
        taInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isBlank(newValue)) {
                chatStateMachine.post(ChatState.TYPING);
            }
            else {
                // return to ready when clear the input only on typing state (distinguish with sending chat clear the input text)
                chatStateMachine.postWithPayloadOnState(ChatState.READY, ChatState.TYPING, currentAgentMeta);
            }
        });
        taInput.setOnKeyReleased(event -> {
            if (ShortcutManager.getIns().isKeyEventMatch(event, KEY_AGENT_SEND)) {
                this.sendChat();
            }
        });

        btnSend.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEND));
        btnSend.setOnAction(event -> {
            this.sendChat();
        });

        btnClear.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLEAR));
        btnClear.setOnAction(event -> {
            taInput.clear();
        });
    }

    private void sendChat() {
        if (chatStateMachine.isState(ChatState.STREAMING)) {
            // stop the streaming
            if (RagService.getInstance().stop()) {
                chatStateMachine.post(ChatState.STOPING);
            }
        }
        else {
            if (currentAgentMeta == null) {
                DialogFactory.infoDialog("Please choose an agent you want to chat with");
                return;
            }
            if (StringUtils.isNotBlank(taInput.getText())) {
                chatStateMachine.post(ChatState.CHATTING);
                // append chat from human to the chat box.
                ChatPartial chat = new ChatPartial(taInput.getText(), MessageType.HUMAN);
                chat.setLast(true);
                taInput.clear();
                chatPane.appendChatPartial(chat);
                // to do the LLM chatting
                chatPane.waitForAnswer();
                RagService.getInstance().chat(chat.getText(), tokenStream -> {
                    final StringBuilder buffer = new StringBuilder();
                    tokenStream.onRetrieved(contents -> {
                                log.debug("retrieved %d contents from embedding store".formatted(contents.size()));
                                log.debug("with size: %s".formatted(contents.stream().map(c -> String.valueOf(c.textSegment().text().length())).collect(Collectors.joining(","))));
                                Platform.runLater(() -> {
                                    chatStateMachine.post(ChatState.STREAMING);
                                });
                            })
                            .onPartialResponse(s -> {
                                buffer.append(s);
                                Platform.runLater(() -> {
                                    ChatPartial chatPartial = new ChatPartial(s, MessageType.AI);
                                    chatPane.appendChatPartial(chatPartial);
                                    chatStateMachine.post(ChatState.STREAMING);
                                });
                            })
                            .onCompleteResponse(resp -> {
                                Platform.runLater(() -> {
                                    if (!buffer.toString().equals(resp.aiMessage().text())) {
                                        ChatPartial chatPartial = new ChatPartial(resp.aiMessage().text(), MessageType.AI);
                                        chatPartial.setLast(true);
                                        chatPane.appendChatPartial(chatPartial);
                                    }
                                    buffer.delete(0, buffer.length());
                                    chatPane.scrollToBottom();
                                    chatStateMachine.postWithPayloadOnState(ChatState.READY, ChatState.STREAMING, ChatState.STOPING, ChatState.STOPPED, currentAgentMeta);
                                });
                            })
                            .onError(e -> {
                                Platform.runLater(() -> {
                                    log.error(e.getMessage(), e);
                                    // the STOP action might cause the LLM provider response with error, so only exception from LLM provider (which means in state STREAMING) shows error to user.
                                    if (chatStateMachine.isState(ChatState.STREAMING)) {
                                        String errMsg = RagService.getInstance().extractErrorMessageFromLLM(currentAgentMeta.getChatProvider(), e.getLocalizedMessage());
                                        Notifications.create().title("Error from agent").text(errMsg).hideAfter(Duration.seconds(15)).show();
                                        chatPane.endWaiting();
                                        RagService.getInstance().stop(); // make sure the http connection is closed.
                                    }
                                    // to READY if LLM provider response with errors.
                                    // to STOPPED if user stopped the streaming.
                                    chatStateMachine.postWithPayloadOnState(ChatState.READY, ChatState.STREAMING, ChatState.STOPPED, ChatState.STOPING, currentAgentMeta);
                                });
                            })
                            .start();
                });
            }
            else {
//                taInput.setPromptText("Chat with your agent, hit %s to send your message.".formatted(ShortcutManager.getIns().getKeyCombination(KEY_AGENT_SEND)));
            }
        }
    }

    private void loadAgents() {
        if (chatStateMachine.isStateIn(ChatState.LOADING, ChatState.SWITCHING)) {
            log.debug("The agent is loading...");
            return;
        }
        log.debug("Reload agents");
        agentSelector.reloadAgents();
    }

    private enum ChatState {
        INIT, LOADING, LOAD_FAILED, READY, SWITCHING, SWITCH_FAILED, TYPING, CHATTING, STREAMING, STOPING, STOPPED
    }
}
