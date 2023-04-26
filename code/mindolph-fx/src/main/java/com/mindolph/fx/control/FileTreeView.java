package com.mindolph.fx.control;

import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;
import com.mindolph.fx.IconBuilder;
import com.mindolph.fx.constant.IconName;
import com.mindolph.fx.util.DisplayUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * A simple tree view for displaying files.
 *
 * @author mindolph.com@gmail.com
 */
public class FileTreeView extends TreeView<FileTreeView.FileTreeViewData> {

    public FileTreeView() {
    }

    public FileTreeView(TreeItem<FileTreeViewData> root) {
        super(root);
    }

    public void init(SearchParams searchParams) {
        this.setCellFactory(param -> {
            TreeCell<FileTreeViewData> treeCell = new TreeCell<>() {
                @Override
                protected void updateItem(FileTreeViewData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        if (item.isParent) {
                            setText(DisplayUtils.displayFile(searchParams.getWorkspaceDir(), item.getFile()));
                            if (item.getFile().isFile()) {
                                setGraphic(new IconBuilder().fileData(new NodeData(item.getFile())).build());
                            }
                            else if (item.getFile().isDirectory()) {
                                setGraphic(new IconBuilder().name(IconName.FOLDER).build());
                            }
                            // styleProperty().set("-fx-background-color: gainsboro");
                        }
                        else {
                            setText(StringUtils.normalizeSpace(item.getInfo()));
                            setGraphic(null);
                        }
                    }
                    else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };
            return treeCell;
        });
    }

    public static class FileTreeViewData {
        private boolean isParent;
        private File file;
        private String info;

        public FileTreeViewData(boolean isParent, File file) {
            this.isParent = isParent;
            this.file = file;
        }

        public FileTreeViewData(boolean isParent, File file, String info) {
            this.isParent = isParent;
            this.file = file;
            this.info = info;
        }

        public boolean isParent() {
            return isParent;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
