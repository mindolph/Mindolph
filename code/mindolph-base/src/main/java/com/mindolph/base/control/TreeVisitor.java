package com.mindolph.base.control;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.apache.commons.lang3.NotImplementedException;

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
     * @param callback return true means keep traversing, return false will interrupt the traversing.
     */
    public static <T> void dfsTraverse(TreeItem<T>  root, Callback<TreeItem<T> , Boolean> callback) {
        if (root == null && callback == null) {
            return;
        }
        ObservableList<TreeItem<T> > children = root.getChildren();
        for (TreeItem<T>  child : children) {
            Boolean keepDoing = callback.call(child);
            if (keepDoing != null && keepDoing == Boolean.FALSE) {
                return;
            }
            if (!child.isLeaf()) {
                dfsTraverse(child, callback);
            }
        }
    }


    public static <T> void bfsTraverse(TreeItem<T>  root, Callback<TreeItem<T> , Boolean> callback) {
        throw new NotImplementedException();
    }
}
