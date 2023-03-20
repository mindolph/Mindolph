package com.mindolph.base.event;

import javafx.scene.control.TreeItem;

/**
 * @author mindolph.com@gmail.com
 */
public class TreeExpandCollapseEvent {

    private final TreeItem<?> treeItem;
    private final boolean isExpand;

    public TreeExpandCollapseEvent(TreeItem<?> treeItem, boolean isExpand) {
        this.treeItem = treeItem;
        this.isExpand = isExpand;
    }

    public TreeItem<?> getTreeItem() {
        return treeItem;
    }

    public boolean isExpand() {
        return isExpand;
    }
}
