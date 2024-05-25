package com.mindolph.base.plugin;

import org.reactfx.EventSource;

import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class PluginEventBus {

    private static PluginEventBus ins;

    public PluginEventBus() {
    }

    public static synchronized PluginEventBus getIns() {
        if (ins == null) {
            ins = new PluginEventBus();
        }
        return ins;
    }

    private final EventSource<Object> preferenceChangeEvent = new EventSource<>();

    public void subscribePreferenceChanges(Consumer<Object> consumer) {
        preferenceChangeEvent.subscribe(consumer);
    }

    public void emitPreferenceChanges() {
        preferenceChangeEvent.push(null);
    }
}
