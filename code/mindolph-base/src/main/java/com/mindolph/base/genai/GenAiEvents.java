package com.mindolph.base.genai;

import com.mindolph.base.genai.llm.StreamToken;
import com.mindolph.core.constant.GenAiConstants.ActionType;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import org.reactfx.EventSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class GenAiEvents {

    private static final GenAiEvents ins = new GenAiEvents();

    private GenAiEvents() {
    }

    public static GenAiEvents getIns() {
        return ins;
    }

    public Map<Object, EventSource<Input>> generateEventSource = new HashMap<>();
    public Map<Object, EventSource<Input>> summarizeEventSource = new HashMap<>();
    public Map<Object, EventSource<ActionType>> actionEventSource = new HashMap<>();


    public void subscribeGenerateEvent(Object editorId, Consumer<Input> consumer) {
        generateEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
    }

    public void emitGenerateEvent(Object editorId, Input input) {
        generateEventSource.computeIfPresent(editorId, (o, inputEventSource) -> {
            inputEventSource.push(input);
            return inputEventSource;
        });
    }

//    public void subscribeSummarizeEvent(Object editorId, Consumer<Input> consumer) {
//        summarizeEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
//    }
//
//    public void emitSummarizeEvent(Object editorId, Input input) {
//        summarizeEventSource.computeIfPresent(editorId, (o, summarizeEventSource) -> {
//            summarizeEventSource.push(input);
//            return summarizeEventSource;
//        });
//    }

    public void subscribeActionEvent(Object editorId, Consumer<ActionType> consumer) {
        actionEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
    }

    public void emitActionEvent(Object editorId, ActionType actionType) {
        EventSource<ActionType> es = actionEventSource.computeIfPresent(editorId, (o, actionEventSource) -> {
            actionEventSource.push(actionType);
            return actionEventSource;
        });
    }

    public record Input(String model, String text, float temperature, int maxTokens, OutputAdjust outputAdjust, String outputLanguage,
                        boolean isRetry, boolean isStreaming) {
    }

    public record Output(String generatedText, boolean isRetry) {
    }

    public record StreamOutput(StreamToken streamToken, boolean isRetry) {
    }

}
