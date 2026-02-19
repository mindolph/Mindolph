package com.mindolph.base.plugin;

import com.mindolph.base.plugin.PluginEvent.EventType;
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

    private final EventSource<PluginEvent<?>> preferenceChangeEvent = new EventSource<>();

    public void subscribePreferenceChanges(Consumer<PluginEvent<?>> consumer) {
        preferenceChangeEvent.subscribe(consumer);
    }

    public void emitPreferenceChanges(EventType eventType) {
        preferenceChangeEvent.push(new PluginEvent<>(eventType));
    }

    public void emitPreferenceChanges(EventType eventType, Object payload) {
        preferenceChangeEvent.push(new PluginEvent<>(eventType, payload));
    }
}
