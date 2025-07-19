package com.mindolph.base.util;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * @author mindolph.com@gmail.com
 */
public class EventUtils {


    public static boolean isEditableInput(KeyEvent event) {
        String str = event.getText();
        return StringUtils.isAsciiPrintable(str)
                || Strings.CS.equalsAny(str, " ", "\r", "\t")
                || KeyCode.BACK_SPACE.equals(event.getCode())
                || KeyCode.ESCAPE.equals(event.getCode());
    }

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
