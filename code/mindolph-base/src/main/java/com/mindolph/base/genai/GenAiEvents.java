package com.mindolph.base.genai;

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

    public void subscribeActionEvent(Object editorId, Consumer<ActionType> consumer) {
        actionEventSource.computeIfAbsent(editorId, o -> new EventSource<>()).subscribe(consumer);
    }

    public void emitActionEvent(Object editorId, ActionType actionType) {
        EventSource<ActionType> es = actionEventSource.computeIfPresent(editorId, (o, actionEventSource) -> {
            actionEventSource.push(actionType);
            return actionEventSource;
        });
    }

    public record Input(String text, float temperature, OutputAdjust outputAdjust) {
    }

    public enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        DISCARD // discard the generated text
    }

    public enum OutputAdjust {
        SHORTER,
        LONGER
    }
}
