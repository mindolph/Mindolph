package com.mindolph.fx.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * @author mindolph.com@gmail.com
 */
public class FileChangeEvent extends Event {

    public FileChangeEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public FileChangeEvent(Object source, EventTarget target, EventType<? extends Event> eventType) {
        super(source, target, eventType);
    }
}
