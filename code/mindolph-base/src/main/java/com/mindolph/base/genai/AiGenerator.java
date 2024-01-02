package com.mindolph.base.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.plugin.Generator;
import com.mindolph.base.plugin.Plugin;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
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

    private Consumer<Object> cancelConsumer;
    private Consumer<String> generateConsumer;
    private Consumer<Boolean> completeConsumer;

    private Plugin plugin;

    private Map<Object, Input> inputMap = new HashMap<>();

    public AiGenerator(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public MenuItem contextMenuItem(String selectedText) {
        Text icon = FontIconManager.getIns().getIcon(IconKey.MAGIC);
        return new MenuItem("Generate (Experiment)", icon);
    }

    @Override
    public StackPane inputDialog(Object editorId) {
        AiInputDialog aiInputDialog = new AiInputDialog(editorId);
        GenAiEvents.getIns().subscribeGenerateEvent(editorId, input -> {
            inputMap.put(editorId, input);
            new Thread(() -> {
                try {
                    String generatedText = LlmService.getIns().predict(input.text(), input.temperature(), input.outputAdjust());
                    log.debug(generatedText);
                    Platform.runLater(() -> generateConsumer.accept(generatedText));
                } catch (Exception e) {
                    log.error("", e);
                    Platform.runLater(() -> cancelConsumer.accept(null));
                }
            }).start();
        });
        GenAiEvents.getIns().subscribeActionEvent(editorId, actionType -> {
            switch (actionType) {
                case KEEP -> {
                    log.debug("action type: %s".formatted(actionType));
                    completeConsumer.accept(true);
                }
                case DISCARD -> {
                    log.debug("action type: %s".formatted(actionType));
                    completeConsumer.accept(false);
                }
                case CANCEL -> {
                    log.debug("action type: %s".formatted(actionType));
                    cancelConsumer.accept(null);
                }
                default -> {
                    log.warn("unknown action type: %s".formatted(actionType));
                }
            }
        });
        return aiInputDialog;
    }

    @Override
    public void onCancel(Consumer<Object> consumer) {
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
    public StackPane reframeDialog(Object editorId) {
        Input input = inputMap.get(editorId);
        AiReframeDialog aiReframeDialog = new AiReframeDialog(editorId, input.text(), input.temperature());
        return aiReframeDialog;
    }

}
