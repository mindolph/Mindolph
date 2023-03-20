package com.mindolph.base.event;

import javafx.scene.control.TreeItem;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
@FunctionalInterface
public interface CollapseEventHandler {

    void onTreeItemCollapsed(TreeItem<?> treeItem);
}
