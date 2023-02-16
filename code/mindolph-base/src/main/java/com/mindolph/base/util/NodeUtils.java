package com.mindolph.base.util;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

import java.util.Set;

/**
 * @author mindolph.com@gmail.com
 */
public class NodeUtils {

    /**
     * Get a scroll bar from a JavaFX node.
     *
     * @param node
     * @param orientation
     * @return
     */
    private ScrollBar getScrollBar(Node node, Orientation orientation) {
        Set<Node> scrolls = node.lookupAll(".scroll-bar");
        for (Node scrollNode : scrolls) {
            if (scrollNode instanceof ScrollBar scroll) {
                if (scroll.getOrientation() == orientation) {
                    return scroll;
                }
            }
        }
        return null;
    }
}
