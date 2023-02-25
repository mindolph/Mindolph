package com.mindolph.base.control;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Function;

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
     * @param function return null means keep traversing, otherwise return the matched TreeItem.
     */
    public static <T> TreeItem<T> dfsSearch(TreeItem<T> root, Function<TreeItem<T>, TreeItem<T>> function) {
        if (root == null && function == null) {
            return null;
        }
        ObservableList<TreeItem<T>> children = root.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<T> child : children) {
            TreeItem<T> found = function.apply(child);
            if (found != null) {
                return found;
            }
            if (!child.isLeaf()) {
                found = dfsSearch(child, function);
                if (found != null) {
                    return found;
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
        if (root == null && callback == null) {
            return;
        }
        ObservableList<TreeItem<T>> children = root.getChildren();
        if (children == null) {
            return;
        }
        for (TreeItem<T> child : children) {
            Boolean keepDoing = callback.call(child);
            if (keepDoing == Boolean.FALSE) {
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
