package com.mindolph.base.genai.event;

/**
 * @since 1.13.2
 */
public class PrepareEvent extends Event {


    public PrepareEvent(String msg) {
        super(null, msg, true);
    }

    public PrepareEvent(String msg, boolean success) {
        super(null, msg, success);
    }

}
