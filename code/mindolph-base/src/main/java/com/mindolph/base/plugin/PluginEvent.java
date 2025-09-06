package com.mindolph.base.plugin;

/**
 *
 * @param <T> Type of event payload.
 *
 * @since 1.13.0
 */
public class PluginEvent<T> {

    private EventType eventType;

    private T payload;

    public PluginEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public enum EventType {
        GLOBAL,
        GENERAL_PREF_CHANGED,
        AGENT_PREF_CHANGED,
        DATASET_PREF_CHANGED,
        MODEL_PREF_CHANGED,
    }
}
