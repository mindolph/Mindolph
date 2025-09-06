package com.mindolph.base.control;

import com.mindolph.core.model.NodeData;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.function.Function;

/**
 * @since 1.13.0
 */
public class FileTreeHelper {

    /**
     * Find tree item by file and update the name.
     *
     * @param rootItem
     * @param file
     * @return
     */
    public static void findAndUpdateName(TreeItem<NodeData> rootItem, File file,
                                                       Function<NodeData, String> namer) {
        TreeItem<NodeData> foundItem = FileTreeHelper.findTreeItemByFile(rootItem, file);
        if (foundItem != null) {
            foundItem.getValue().setFormatted(namer.apply(foundItem.getValue()));
        }
        else {
            throw new RuntimeException("No tree item found for %s".formatted(file.getAbsolutePath()));
        }
    }

    /**
     * Find a tree item by searching exactly the path of the file
     * because it's faster than traversing the whole tree.
     *
     * @param rootItem
     * @param file
     * @return tree item that matches the file, root tree item is always excluded.
     */
    public static TreeItem<NodeData> findTreeItemByFile(TreeItem<NodeData> rootItem, File file) {
        return TreeFinder.findTreeItemPathMatch(rootItem, treeItem -> {
            File nodeFile = treeItem.getValue().getFile();
            return rootItem == treeItem ||
                    file.getPath().startsWith(nodeFile.getPath());
        }, treeItem -> {
            if (treeItem == rootItem) {
                return false;
            }
            File nodeFile = treeItem.getValue().getFile();
            return nodeFile.equals(file);
        });
    }
}
