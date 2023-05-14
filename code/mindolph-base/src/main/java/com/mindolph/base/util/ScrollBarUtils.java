package com.mindolph.base.util;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

/**
 * @author
 */
public class ScrollBarUtils {

    /**
     * Find scroll bar instance if the node doesn't provide apis to get that.
     *
     * @param parent
     * @param or
     * @return
     */
    public static ScrollBar findScrollBar(Node parent, Orientation or) {
        for (Node n : parent.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar bar) {
                if (bar.getOrientation() == or) {
                    return bar;
                }
            }
        }
        return null;
    }
}
