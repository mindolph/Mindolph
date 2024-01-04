package com.mindolph.base.util;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Set;

/**
 * Utils for FX Node.
 *
 * @author mindolph.com@gmail.com
 */
public class NodeUtils {

    /**
     *
     * @param disable
     * @param nodes
     * @since 1.7
     */
    public static void setDisable(boolean disable, Node... nodes){
        for (Node node : nodes) {
            node.setDisable(disable);
        }
    }

    /**
     *
     * @param nodes
     * @since 1.7
     */
    public static void enable(Node... nodes){
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    /**
     *
     * @param nodes
     * @since 1.7
     */
    public static void disable(Node... nodes){
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    /**
     * Get a scroll bar from a JavaFX node.
     *
     * @param node
     * @param orientation
     * @return
     */
    public static ScrollBar getScrollBar(Node node, Orientation orientation) {
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

    public static Bounds getTextBounds(String s, Font font) {
        Text text = new Text(s);
        text.setFont(font);
        StackPane stackPane = new StackPane(text);
        stackPane.layout();
        return text.getLayoutBounds();
    }

}
