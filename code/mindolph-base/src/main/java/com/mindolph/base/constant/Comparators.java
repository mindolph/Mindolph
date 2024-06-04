package com.mindolph.base.constant;

import com.mindolph.core.model.NodeData;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Comparator;

/**
 * @since 1.8
 */
public interface Comparators {


    Comparator<TreeItem<NodeData>> SORTING_TREE_ITEMS = (o1, o2) -> {
        File f1 = o1.getValue().getFile();
        File f2 = o2.getValue().getFile();
        if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
            return f1.getName().compareTo(f2.getName());
        }
        else if (f1.isDirectory()) {
            return -1;
        }
        else {
            return 1;
        }
    };
}
