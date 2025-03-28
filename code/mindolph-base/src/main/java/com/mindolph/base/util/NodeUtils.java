package com.mindolph.base.util;

import javafx.scene.Node;

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

}
