package com.mindolph.fx.view;

import com.mindolph.base.FontIconManager;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * @author mindolph.com@gmail.com
 * @see RecentView
 */
public class RecentViewCell extends ListCell<NodeData> {

    @Override
    protected void updateItem(NodeData item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            FXMLLoader fxmlLoader = FxmlUtils.loadUri("/view/recent_view_item.fxml", new ItemController(item));
            Node root = fxmlLoader.getRoot();
            setGraphic(root);
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    private static class ItemController extends AnchorPane implements Initializable {
        @FXML
        private Label lbIcon;
        @FXML
        Label lbFileName;
        @FXML
        Label lbFilePath;
        private NodeData fileData;

        public ItemController(NodeData fileData) {
            this.fileData = fileData;
        }

        public ItemController(NodeData fileData, Node... children) {
            super(children);
            this.fileData = fileData;
        }

        private void init() {
            WorkspaceMeta workspaceMeta = WorkspaceManager.getIns().getWorkspaceList().matchByFilePath(fileData.getFile().getPath());
            String desc;
            if (workspaceMeta != null) {
                desc = "%s%s".formatted(FilenameUtils.getBaseName(workspaceMeta.getBaseDirPath()),
                        StringUtils.substringBetween(fileData.getFile().getPath(), workspaceMeta.getBaseDirPath(), FilenameUtils.getName(fileData.getFile().getPath())));
            }
            else {
                desc = fileData.getFile().getPath(); // for external files
            }
            lbFileName.setText(fileData.getName());
            lbFilePath.setText(desc);
            lbIcon.setGraphic(FontIconManager.getIns().getIconForFile(fileData, 24));
        }

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            this.init();
        }
    }
}
