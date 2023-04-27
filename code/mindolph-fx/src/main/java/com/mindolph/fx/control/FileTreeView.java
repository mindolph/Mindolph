package com.mindolph.fx.control;

import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;
import com.mindolph.fx.IconBuilder;
import com.mindolph.fx.constant.IconName;
import com.mindolph.fx.util.DisplayUtils;
import com.mindolph.mfx.util.FontUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.substring;


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
                            BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::indexOf : StringUtils::indexOfIgnoreCase;
                            TextFlow textFlow = new TextFlow();
                            String normalText = normalizeSpace(item.getInfo());
                            String normalKeyword = normalizeSpace(searchParams.getKeywords());
                            int start = indexOf.apply(normalText, normalKeyword);
                            if (start >= 0) {
                                int end = start + normalKeyword.length();
                                String pre = substring(normalText, 0, start);
                                String center = substring(normalText, start, end);
                                String post = substring(normalText, end);
                                textFlow.getChildren().add(new Text(pre));
                                Text hit = new Text(center);
                                Font font = FontUtils.newFontWithSize(hit.getFont(), hit.getFont().getSize() * 1.1);
                                hit.setFont(font);
                                hit.setFill(Color.BLUE);
                                textFlow.getChildren().add(hit);
                                textFlow.getChildren().add(new Text(post));
                                setGraphic(textFlow);
                            }
                            else {
                                setText(normalText);
                                setGraphic(null);
                            }
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
