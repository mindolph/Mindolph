package com.mindolph.base.control;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

/**
 * Tree finder for JavaFX TreeView to find one TreeItem.
 *
 * @author mindolph.com@gmail.com
 */
public class TreeFinder {

    /**
     * Find one tree item by the {@code predicate} in the sub-tree of parent tree item.
     *
     * @param parent
     * @param predicate
     * @return
     */
    public static <T> TreeItem<T> findTreeItem(TreeItem<T> parent, Predicate<TreeItem<T>> predicate) {
        if (predicate.test(parent)) {
            return parent;
        }
        else {
            for (TreeItem<T> child : parent.getChildren()) {
                TreeItem<T> found = findTreeItem(child, predicate);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
    }

    /**
     * Find one tree item by the {@code pathPredicate} and {@code targetPredicate} in the sub-tree of parent tree item.
     * Not only the target item but also the items in the tree path must be predicated.
     *
     * @param parent
     * @param pathPredicate   predicate the items in the tree path.
     * @param targetPredicate predicate the target item.
     * @return
     */
    public static <T> TreeItem<T> findTreeItemPathMatch(TreeItem<T> parent,
                                                        Predicate<TreeItem<T>> pathPredicate,
                                                        Predicate<TreeItem<T>> targetPredicate) {
        if (targetPredicate.test(parent)) {
            return parent;
        }
        else {
            if (pathPredicate.test(parent)) {
                for (TreeItem<T> child : parent.getChildren()) {
                    TreeItem<T> found = findTreeItemPathMatch(child, pathPredicate, targetPredicate);
                    if (found != null) {
                        return found;
                    }
                }
            }
            return null;
        }
    }
}
