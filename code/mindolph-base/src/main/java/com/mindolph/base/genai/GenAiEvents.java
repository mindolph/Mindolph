package com.mindolph.base.genai;

import org.reactfx.EventSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
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

    public void emitActionEvent(Object editorId, ActionType actionType){
        EventSource<ActionType> es = actionEventSource.computeIfPresent(editorId, (o, actionEventSource) -> {
            System.out.println("##");
            actionEventSource.push(actionType);
            return actionEventSource;
        });
        System.out.println(es);
    }

    public record Input(String text, float temperature, OutputLength outputLength) {
//        public static final Input DEFAULT = new Input("", Temperature.DEFAULT);
    }

    public enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        ABANDON // abandon the generated text
    }

    public enum OutputLength {
        SHORT,
        MEDIUM,
        LONG
    }
}
