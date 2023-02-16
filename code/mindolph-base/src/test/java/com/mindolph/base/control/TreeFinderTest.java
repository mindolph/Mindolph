package com.mindolph.base.control;

import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
class TreeFinderTest {

    ThreadLocal<TreeItem<String>> root = new ThreadLocal<>();

    @BeforeEach
    public void setup() {
        TreeItem<String> root = new TreeItem<>("root");
        TreeItem<String> l1 = new TreeItem<>("l1");
        TreeItem<String> l1_1 = new TreeItem<>("l1_1");
        TreeItem<String> l1_2 = new TreeItem<>("l1_2");
        TreeItem<String> l1_2_1 = new TreeItem<>("l1_2_1");
        TreeItem<String> l2 = new TreeItem<>("l2");
        root.getChildren().addAll(l1, l2);
        l1.getChildren().addAll(l1_1, l1_2);
        l1_2.getChildren().addAll(l1_2_1);
        this.root.set(root);
    }

    @Test
    void findTreeItem() {
        TreeItem root = this.root.get();
        Assertions.assertEquals("l1_2", TreeFinder.findTreeItem(root, o -> "l1_2".equals(o.getValue())).getValue());
        Assertions.assertEquals("l1_2_1", TreeFinder.findTreeItem(root, o -> "l1_2_1".equals(o.getValue())).getValue());
        Assertions.assertEquals("l2", TreeFinder.findTreeItem(root, o -> "l2".equals(o.getValue())).getValue());
    }

    @Test
    void findTreeItemPathMatch() {
        TreeItem root = this.root.get();
        TreeItem found = TreeFinder.findTreeItemPathMatch(root,
                treeItem -> "root".equals(treeItem.getValue()) || ((String) treeItem.getValue()).startsWith("l1"),
                treeItem -> "l1_2_1".equals(treeItem.getValue()));
        System.out.println(found);
        Assertions.assertEquals("l1_2_1", found.getValue());
    }
}