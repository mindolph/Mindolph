package com.mindolph.base.util;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Utils for FX Node.
 *
 * @author mindolph.com@gmail.com
 */
public class NodeUtils {

    /**
     * @param disable
     * @param nodes
     * @since 1.7
     */
    public static void setDisable(boolean disable, Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(disable);
        }
    }

    /**
     * @param nodes
     * @since 1.7
     */
    public static void enable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    /**
     * @param nodes
     * @since 1.7
     */
    public static void disable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    /**
     * TODO Move to mfx
     * @param s
     * @param font
     * @return
     */
    public static Bounds getTextBounds(String s, Font font) {
        Text text = new Text(s);
        text.setFont(font);
//        StackPane stackPane = new StackPane(text);
//        stackPane.layout();
        return text.getLayoutBounds();
    }

}
