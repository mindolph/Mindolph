package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants.ProviderProps;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.LlmService;
import com.mindolph.base.plugin.Generator;
import com.mindolph.base.plugin.Plugin;
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

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class AiGenerator implements Generator {

    private static final Logger log = LoggerFactory.getLogger(AiGenerator.class);

    private Consumer<Boolean> cancelConsumer;
    private Consumer<String> generateConsumer;
    private Consumer<Boolean> completeConsumer;
    private Consumer<StackPane> panelShowingConsumer;

    private final Plugin plugin;
    private final Object editorId;
    private Pane parentPane;
    private SkinBase<?> parentSkin;

    private final Map<Object, Input> inputMap = new HashMap<>();
    private AiInputPane inputPanel;
    private AiReframePane reframePanel;

    public AiGenerator(Plugin plugin, Object editorId) {
        this.plugin = plugin;
        this.editorId = editorId;
        this.listenGenAiEvents();
    }

    private void listenGenAiEvents() {
        GenAiEvents.getIns().subscribeGenerateEvent(editorId, input -> {
            inputMap.put(editorId, input);
            new Thread(() -> {
                try {
                    String generatedText = LlmService.getIns().predict(input.text(), input.temperature(), input.outputAdjust());
                    log.debug(generatedText);
                    Platform.runLater(() -> {
                        generateConsumer.accept(generatedText);
                        removeFromParent(reframePanel);
                        removeFromParent(inputPanel);
                        reframePanel = new AiReframePane(editorId, input.text(), input.temperature());
                        addToParent(reframePanel);
                        panelShowingConsumer.accept(reframePanel);
                    });
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                    Platform.runLater(() -> {
                        cancelConsumer.accept(false);
                        if (inputPanel != null)
                            inputPanel.onStop(e.getLocalizedMessage());
                        if (reframePanel != null)
                            reframePanel.onStop(e.getLocalizedMessage());
                    });
                }
            }).start();
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
                default -> log.warn("unknown action type: %s".formatted(actionType));
            }
        });
    }

    @Override
    public MenuItem contextMenuItem(String selectedText) {
        Text icon = FontIconManager.getIns().getIcon(IconKey.MAGIC);
        return new MenuItem("Generate (Experiment)", icon);
    }

    @Override
    public StackPane showInputPanel() {
        if (!checkSettings()) {
            // TODO Should change the way to displaying such error message.
            throw new RuntimeException("You have to set up the AI provider first.");
        }
        inputPanel = new AiInputPane(editorId);
        addToParent(inputPanel);
        panelShowingConsumer.accept(inputPanel);
        return inputPanel;
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

    private void removeFromParent(StackPane panel){
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
                ProviderProps props = propsMap.get(activeProvider);
                if (StringUtils.isNotBlank(props.apiKey()) && StringUtils.isNotBlank(props.aiModel())) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onCancel(Consumer<Boolean> consumer) {
        this.cancelConsumer = consumer;
    }

    @Override
    public void onComplete(Consumer<Boolean> consumer) {
        this.completeConsumer = consumer;
    }

    @Override
    public void onGenerated(Consumer<String> consumer) {
        this.generateConsumer = consumer;
    }

    @Override
    public void onPanelShowing(Consumer<StackPane> consumer) {
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
