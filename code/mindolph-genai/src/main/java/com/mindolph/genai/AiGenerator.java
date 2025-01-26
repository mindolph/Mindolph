package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.GenAiEvents.StreamOutput;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.StreamToken;
import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.base.plugin.Generator;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.core.constant.GenAiConstants.ProviderInfo;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.GenAiModelProvider.ProviderType;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.mindolph.core.constant.GenAiConstants.FILE_OUTPUT_MAPPING;

/**
 * Each generator is created for each editor.
 * The setParentPane() should be called to setup anchored pane before using this Generator.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiGenerator implements Generator {

    private static final Logger log = LoggerFactory.getLogger(AiGenerator.class);

    private Consumer<Boolean> cancelConsumer;
    private Consumer<Void> beforeGenerateConsumer;
    private Consumer<StreamOutput> streamOutputConsumer;
    private Consumer<GenAiEvents.Output> generateConsumer;
    private Consumer<Boolean> completeConsumer;
    private Consumer<StackPane> panelShowingConsumer;

    private final Plugin plugin;
    private final Object editorId; // used for event handing only
    private final String fileType;
    private Pane parentPane;
    private SkinBase<?> parentSkin;

    private final Map<Object, Input> inputMap = new HashMap<>();
    private AiInputPane inputPanel;
    private AiSummaryPane summarizePanel;
    private AiReframePane reframePanel;

    public AiGenerator(Plugin plugin, Object editorId, String fileType) {
        this.plugin = plugin;
        this.editorId = editorId; // the editor seems not useful yet.
        this.fileType = fileType;
        this.listenGenAiEvents();
    }

    private void listenGenAiEvents() {
        GenAiEvents.getIns().subscribeGenerateEvent(editorId, input -> {
            inputMap.put(editorId, input);

            Consumer<String> onError = (msg) -> {
                Platform.runLater(() -> {
                    cancelConsumer.accept(false);
                    String err = "Failed to generate content by %s.\n %s\n".formatted(LlmService.getIns().getActiveAiProvider(), msg);
                    if (inputPanel != null)
                        inputPanel.onStop(err);
                    if (reframePanel != null)
                        reframePanel.onStop(err);
                });
            };

            Consumer<StreamToken> showReframePane = (streamToken) -> {
                AiGenerator.this.removeFromParent(reframePanel);
                AiGenerator.this.removeFromParent(inputPanel);
                reframePanel = new AiReframePane(editorId, input.text(), input.temperature(), streamToken.outputTokens());
                AiGenerator.this.addToParent(reframePanel);
                panelShowingConsumer.accept(reframePanel);
            };

            if (input.isStreaming()) {
                Platform.runLater(() -> {
                    if (beforeGenerateConsumer != null)
                        beforeGenerateConsumer.accept(null);
                });
                if (streamOutputConsumer == null) {
                    onError.accept("Not support stream generation");
                }
                LlmService.getIns().stream(input, new OutputParams(input.outputAdjust(), FILE_OUTPUT_MAPPING.get(fileType)),
                        streamToken -> {
                            if (streamToken.isError()) {
                                log.warn("error from streaming: {}", streamToken);
                                onError.accept(streamToken.text());
                            }
                            else {
                                // accept streaming output (even with `stop` one).
                                streamOutputConsumer.accept(new StreamOutput(streamToken, input.isRetry()));
                                if (streamToken.isStop()) {
                                    Platform.runLater(() -> showReframePane.accept(streamToken));
                                }
                            }
                        });
            }
            else {
                new Thread(() -> {
                    try {
                        StreamToken generated = LlmService.getIns().predict(input, new OutputParams(input.outputAdjust(), FILE_OUTPUT_MAPPING.get(fileType)));
                        if (generated == null) {
                            // probably stopped by user.
                            return;
                        }
                        log.debug(generated.text());
                        Platform.runLater(() -> {
                            generateConsumer.accept(new GenAiEvents.Output(generated.text(), input.isRetry()));
                            showReframePane.accept(generated);
                        });
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage(), e);
                        onError.accept(e.getLocalizedMessage());
                    }
                }).start();
            }
        });
        GenAiEvents.getIns().subscribeActionEvent(editorId, actionType -> {
            log.debug("action type: %s".formatted(actionType));
            switch (actionType) {
                case KEEP -> {
                    completeConsumer.accept(true);
                    removeFromParent(reframePanel);
                }
                case DISCARD -> {
                    completeConsumer.accept(false);
                    removeFromParent(reframePanel);
                }
                case CANCEL -> {
                    cancelConsumer.accept(true);
                    removeFromParent(inputPanel);
                }
                case STOP -> {
                    LlmService.getIns().stop();
                }
                case ABORT -> {
                    LlmService.getIns().stop();
                    removeFromParent(summarizePanel);
                }
                default -> log.warn("unknown action type: %s".formatted(actionType));
            }
        });
    }

    @Override
    public ProviderInfo getProviderInfo() {
        LlmConfig config = LlmConfig.getIns();
        String activeAiProvider = config.getActiveAiProvider();
        Map<String, ProviderProps> providers = config.loadGenAiProviders();
        if (providers.containsKey(activeAiProvider)) {
            ProviderProps props = providers.get(activeAiProvider);
            return new ProviderInfo(activeAiProvider, props.aiModel());
        }
        return new ProviderInfo(activeAiProvider, null);
    }

    @Override
    public MenuItem generationMenuItem(String selectedText) {
        Text icon = FontIconManager.getIns().getIcon(IconKey.MAGIC);
        return new MenuItem("Generate...", icon);
    }

    @Override
    public MenuItem summaryMenuItem() {
        return new MenuItem("Summarize...", FontIconManager.getIns().getIcon(IconKey.MAGIC));
    }

    @Override
    public StackPane showInputPanel(String defaultInput) {
        if (!checkSettings()) {
            DialogFactory.warnDialog("You have to set up the Gen-AI provider properly first.");
            return null;
        }
        inputPanel = new AiInputPane(editorId, fileType, defaultInput);
        addToParent(inputPanel);
        panelShowingConsumer.accept(inputPanel);
        return inputPanel;
    }

    @Override
    public StackPane showSummarizePanel(String input) {
        if (!checkSettings()) {
            DialogFactory.warnDialog("You have to set up the Gen-AI provider properly first.");
            return null;
        }
        summarizePanel = new AiSummaryPane(editorId, fileType, input);
        addToParent(summarizePanel);
        panelShowingConsumer.accept(summarizePanel);
        return summarizePanel;
    }

    private void addToParent(StackPane panel) {
        if (parentPane != null) {
            parentPane.getChildren().add(panel);
        }
        else if (parentSkin != null) {
            parentSkin.getChildren().add(panel);
            panel.setManaged(false);
        }
        else {
            throw new RuntimeException("Parent pane or skin is not set.");
        }
    }

    /**
     * Remove pane from it's parent.
     *
     * @param panel
     */
    private void removeFromParent(StackPane panel) {
        if (parentPane != null) {
            parentPane.getChildren().remove(panel);
            parentPane.requestFocus();
        }
        else if (parentSkin != null) {
            parentSkin.getChildren().remove(panel);
        }
        else {
            throw new RuntimeException("Parent pane or skin is not set.");
        }
    }

    private boolean checkSettings() {
        LlmConfig config = LlmConfig.getIns();
        String activeProvider = config.getActiveAiProvider();
        if (StringUtils.isNotBlank(activeProvider)) {
            Map<String, ProviderProps> propsMap = config.loadGenAiProviders();
            if (propsMap.containsKey(activeProvider)) {
                GenAiModelProvider provider = GenAiModelProvider.fromName(activeProvider);
                ProviderProps props = propsMap.get(activeProvider);
                if (provider == null || props == null) return false;
                log.debug("Provider: %s".formatted(provider));
                log.trace(String.valueOf(props));
                if (provider.getType() == ProviderType.PUBLIC) {
                    return StringUtils.isNotBlank(props.apiKey()) && StringUtils.isNotBlank(props.aiModel());
                }
                else if (provider.getType() == ProviderType.PRIVATE) {
                    return StringUtils.isNotBlank(props.baseUrl()) && StringUtils.isNotBlank(props.aiModel());
                }
            }
        }
        return false;
    }


    @Override
    public void setOnCancel(Consumer<Boolean> consumer) {
        this.cancelConsumer = consumer;
    }

    @Override
    public void setOnComplete(Consumer<Boolean> consumer) {
        this.completeConsumer = consumer;
    }

    @Override
    public void setBeforeGenerate(Consumer<Void> consumer) {
        this.beforeGenerateConsumer = consumer;
    }

    @Override
    public void setOnStreaming(Consumer<StreamOutput> consumer) {
        this.streamOutputConsumer = consumer;
    }

    @Override
    public void setOnGenerated(Consumer<GenAiEvents.Output> consumer) {
        this.generateConsumer = consumer;
    }

    @Override
    public void setOnPanelShowing(Consumer<StackPane> consumer) {
        this.panelShowingConsumer = consumer;
    }

    @Override
    public void setParentPane(Pane pane) {
        this.parentPane = pane;
    }

    @Override
    public void setParentSkin(SkinBase<?> parentSkin) {
        this.parentSkin = parentSkin;
    }
}
