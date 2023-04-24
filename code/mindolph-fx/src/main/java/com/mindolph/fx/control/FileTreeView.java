package com.mindolph.fx.control;

import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;
import com.mindolph.fx.IconBuilder;
import com.mindolph.fx.constant.IconName;
import com.mindolph.fx.util.DisplayUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;

/**
 * A simple tree view for displaying files.
 *
 * @author allen
 */
public class FileTreeView extends TreeView<File> {

    public FileTreeView() {
    }

    public FileTreeView(TreeItem<File> root) {
        super(root);
    }

    public void init(SearchParams searchParams) {
        this.setCellFactory(param -> {
            return new TreeCell<>() {
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(DisplayUtils.displayFile(searchParams.getWorkspaceDir(), item));
                        if (item.isFile()) {
                            setGraphic(new IconBuilder().fileData(new NodeData(item)).build());
                        }
                        else if (item.isDirectory()) {
                            setGraphic(new IconBuilder().name(IconName.FOLDER).build());
                        }
                    }
                    else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };
        });
    }
}
