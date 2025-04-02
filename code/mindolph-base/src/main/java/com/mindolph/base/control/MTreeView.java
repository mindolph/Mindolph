package com.mindolph.base.control;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindolph.core.model.ItemData;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;

/**
 * @see ItemData
 * @since 1.8
 */
public class MTreeView<T extends ItemData> extends TreeView<T> {

    private static final Logger log = LoggerFactory.getLogger(MTreeView.class);

    public void collapseAll() {
        collapseTreeNodes(super.getRoot(), true);
    }

    /**
     * Collapse all it's sub nodes.
     *
     * @param treeItem
     * @param includeParent specified tree item collapse either.
     */
    public void collapseTreeNodes(TreeItem<T> treeItem, boolean includeParent) {
        log.debug("Collapse all expanded nodes under %s".formatted(treeItem));
        setExpanded(treeItem, includeParent, false);
    }

    public void expandAll() {
        expandTreeNodes(super.getRoot(), true);
    }

    public void expandTreeNodes(TreeItem<T> treeItem, boolean includeParent) {
        log.debug("Expand all expanded nodes under %s".formatted(treeItem));
        setExpanded(treeItem, includeParent, true);
    }

    private void setExpanded(TreeItem<T> treeItem, boolean includeParent, boolean expand) {
        TreeVisitor.dfsTraverse(treeItem, item -> {
            log.trace("%s node: %s".formatted(expand ? "Expand" : "Collapse", item));
            item.setExpanded(expand);
            return Boolean.TRUE;
        });
        if (includeParent) treeItem.setExpanded(expand);
        super.refresh();
    }

    public void select(TreeItem<T> treeItem) {
        super.getSelectionModel().select(treeItem);
    }

    /**
     * Clear current selection and select specified tree item.
     *
     * @param treeItem
     */
    public void reselect(TreeItem<T> treeItem) {
        super.getSelectionModel().clearSelection();
        super.getSelectionModel().select(treeItem);
    }

    /**
     * Clear current selection and select specified tree items.
     * @param treeItems
     */
    public void reselect(List<TreeItem<T>> treeItems) {
        super.getSelectionModel().clearSelection();
        treeItems.forEach(super.getSelectionModel()::select);
    }

    public void scrollToSelected() {
        Platform.runLater(() -> {
            if (!this.isItemVisible(this.getSelectedTreeItem())) {
                log.debug("Scroll to invisible selected tree item");
                super.scrollTo(super.getSelectionModel().getSelectedIndex());
            }
        });
    }

    public TreeItem<T> getSelectedTreeItem() {
        return super.getSelectionModel().getSelectedItem();
    }

    public Optional<T> getSelectedData() {
        TreeItem<T> selectedItem = getSelectedTreeItem();
        if (selectedItem == null) {
            return Optional.empty();
        }
        else {
            return Optional.ofNullable(selectedItem.getValue());
        }
    }

    public List<T> getSelectedItemsData() {
        return super.getSelectionModel().getSelectedItems().stream().map(TreeItem::getValue).toList();
    }

    public boolean isSingleSelected() {
        return super.getSelectionModel().getSelectedItems().size() == 1;
    }


    public boolean isItemVisible(TreeItem<T> item) {
        if (item == null) return false;
        TreeViewSkin<?> skin = (TreeViewSkin<?>) super.getSkin();
        VirtualFlow<?> vf = (VirtualFlow<?>) skin.getChildren().get(0);
        if (vf == null || vf.getFirstVisibleCell() == null || vf.getLastVisibleCell() == null) {
            return false;
        }
        int f = vf.getFirstVisibleCell().getIndex();
        int l = vf.getLastVisibleCell().getIndex();
        Integer i = item.getValue().getDisplayIndex();
        log.trace("The index of target tree item %d".formatted(i));
        log.trace("  is between %d and %d".formatted(f, l));
        return i != null && (i >= f && i <= l);
    }

    public void removeSelectedTreeItem() {
        this.removeTreeItem(getSelectedTreeItem());
    }

    public void removeTreeItem(TreeItem<T> treeItem) {
        if (treeItem == null) {
            return;
        }
        treeItem.getParent().getChildren().remove(treeItem);
        super.refresh();
    }

//    public void removeTreeItemByData(T itemData) {
//        TreeItem<T> selectedTreeItem = this.getSelectedTreeItem();
//        if (selectedTreeItem.getValue() == itemData) {
//            selectedTreeItem.getParent().getChildren().remove(selectedTreeItem);
//            super.refresh();
//        }
//    }

    public void removeAll() {
        this.getRoot().getChildren().clear();
    }

}
