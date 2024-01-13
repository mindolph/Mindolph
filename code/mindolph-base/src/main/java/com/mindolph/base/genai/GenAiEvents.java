package com.mindolph.base.genai;

import com.mindolph.base.genai.llm.Constants;
import com.mindolph.base.genai.llm.Constants.OutputAdjust;
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
    public Map<Object, EventSource<Constants.ActionType>> actionEventSource = new HashMap<>();


    public void subscribeGenerateEvent(Object editorId, Consumer<Input> consumer) {
        generateEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
    }

    public void emitGenerateEvent(Object editorId, Input input) {
        generateEventSource.computeIfPresent(editorId, (o, inputEventSource) -> {
            inputEventSource.push(input);
            return inputEventSource;
        });
    }

    public void subscribeActionEvent(Object editorId, Consumer<Constants.ActionType> consumer) {
        actionEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
    }

    public void emitActionEvent(Object editorId, Constants.ActionType actionType) {
        EventSource<Constants.ActionType> es = actionEventSource.computeIfPresent(editorId, (o, actionEventSource) -> {
            actionEventSource.push(actionType);
            return actionEventSource;
        });
    }

    public record Input(String text, float temperature, OutputAdjust outputAdjust, boolean isRetry) {
    }

    public record Output(String generatedText, boolean isRetry) {
    }

}
