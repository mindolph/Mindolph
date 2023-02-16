package com.mindolph.base.util;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Window;

/**
 * @author mindolph.com@gmail.com
 */
public class EventUtils {

    public static Window getWindowFromEvent(Event event) {
        Object source = event.getSource();
        if (Node.class.isAssignableFrom(source.getClass())) {
            return ((Node) event.getSource()).getScene().getWindow();
        }
        else {
            System.out.println(source.getClass());
            throw new RuntimeException("xx");
        }
    }

}
