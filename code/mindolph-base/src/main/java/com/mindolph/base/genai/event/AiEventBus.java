package com.mindolph.base.genai.event;

import org.reactfx.EventSource;

import java.util.function.Consumer;

/**
 * @since 1.13.2
 */
public class AiEventBus {

    private static final AiEventBus aiEventBus = new AiEventBus();

    private final EventSource<Event> eventSource = new EventSource<>();

    private AiEventBus() {
    }

    public static AiEventBus getInstance() {
        return aiEventBus;
    }

    public void emitEvent(Event event) {
        this.eventSource.emit(event);
    }

    public void subscribeEvent(Consumer<Event> eventConsumer) {
        this.eventSource.subscribe(eventConsumer);
    }

    public void subscribeEventOnce(Consumer<Event> eventConsumer) {
        this.eventSource.subscribeForOne(eventConsumer);
    }
}
