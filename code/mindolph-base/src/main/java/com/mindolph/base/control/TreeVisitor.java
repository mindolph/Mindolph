package com.mindolph.base.control;

import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

/**
 * Tree visitor for JavaFX TreeView.
 *
 * @author mindolph.com@gmail.com
 */
public class TreeVisitor {

    /**
     * Visit all nodes and sub-nodes of the root node (except itself) with deep-first-search strategy.
     *
     * @param root
     * @param function return null means stop traversing deeper; return empty TreeItem means keep traversing, otherwise return the matched TreeItem.
     */
    public static <T> TreeItem<T> dfsSearch(TreeItem<T> root, Function<TreeItem<T>, TreeItem<T>> function) {
        if (root == null) {
            return null;
        }
        ObservableList<TreeItem<T>> children = root.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<T> child : children) {
            TreeItem<T> found = function.apply(child);
            if (found != null) {
                if (found.getValue() == null) {
                    // go deeper if not leaf
                    if (!child.isLeaf()) {
                        found = dfsSearch(child, function);
                        return found;
                    }
                }
                else {
                    return found; // found matched
                }
            }
        }
        return null;
    }

    /**
     * Visit all nodes and sub-nodes of the root node (except itself) with deep-first-search strategy.
     *
     * @param root
     * @param callback return true means keep traversing, return false will interrupt the traversing.
     */
    public static <T> void dfsTraverse(TreeItem<T> root, Callback<TreeItem<T>, Boolean> callback) {
        if (root == null) {
            return;
        }
        ObservableList<TreeItem<T>> children = root.getChildren();
        if (children == null) {
            return;
        }
        for (TreeItem<T> child : children) {
            Boolean keepDoing = callback.call(child);
            if (Boolean.FALSE.equals(keepDoing)) {
                return;
            }
            if (!child.isLeaf()) {
                dfsTraverse(child, callback);
            }
        }
    }


    public static <T> void bfsTraverse(TreeItem<T> root, Callback<TreeItem<T>, Boolean> callback) {
        throw new NotImplementedException();
    }
}
