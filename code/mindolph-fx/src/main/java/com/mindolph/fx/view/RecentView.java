package com.mindolph.fx.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.AsyncUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @author mindolph.com@gmail.com
 * @see RecentViewCell
 */
public class RecentView extends BaseView implements EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(RecentView.class);

    @FXML
    private ListView<NodeData> listView;

    private ContextMenu itemContextMenu = null;
    private MenuItem miOpenFile;
    private MenuItem miRemove;

    public RecentView() {
        super("/view/recent_view.fxml", false);
        listView.setCellFactory(param -> new RecentViewCell());
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                this.openSelectedFile();
            }
        });
        itemContextMenu = createItemContextMenu();
        listView.setContextMenu(itemContextMenu);
        // listen and update the path changed file
        EventBus.getIns().subscribeFilePathChanged(filePathChangedEvent -> updateRecentFile(filePathChangedEvent.getNodeData(), filePathChangedEvent.getNewFile()));
    }

    public void load() {
        log.info("Load recent files.");
        AsyncUtils.fxAsync(() -> {
            return RecentManager.getInstance().loadRecent();
        }, recentFiles -> {
            // create item data in batch.
            listView.getItems().addAll(recentFiles.stream().map(NodeData::new).toList());
            // link each item to workspace(if opened)
            WorkspaceList workspaceList = WorkspaceManager.getIns().getWorkspaceList();
            listView.getItems().forEach(nodeData -> {
                WorkspaceMeta workspaceMeta = workspaceList.matchByFilePath(nodeData.getFile().getPath());
                if (workspaceMeta != null) {
                    nodeData.setWorkspaceData(new NodeData(new File(workspaceMeta.getBaseDirPath())));
                }
            });
            // listen to file opening and fresh list with the new opened file
            EventBus.getIns().subscribeOpenFile(openedFile -> refresh(openedFile.getNodeData()));
            // listen to file deletion and remove from the recent list if the file is deleted
            EventBus.getIns().subscribeDeletedFile(nodeData -> this.removeRecentFile(nodeData.getFile()));
        });
    }

    private ContextMenu createItemContextMenu() {
        itemContextMenu = new ContextMenu();
        miOpenFile = new MenuItem("Open");
        miOpenFile.setMnemonicParsing(false);
        miOpenFile.setOnAction(this);
        miRemove = new MenuItem("Remove");
        miRemove.setMnemonicParsing(false);
        miRemove.setOnAction(this);
        itemContextMenu.getItems().addAll(miOpenFile, miRemove);
        return itemContextMenu;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        NodeData selectedNode = listView.getSelectionModel().getSelectedItem();
        log.debug(selectedNode.getFile().getPath());
        log.debug(actionEvent.getSource().toString());
        if (actionEvent.getSource() == miOpenFile) {
            this.openSelectedFile();
        }
        else if (actionEvent.getSource() == miRemove) {
            this.removeRecentFile(selectedNode.getFile());
        }
    }

    private void openSelectedFile() {
        NodeData selectedData = listView.getSelectionModel().getSelectedItem();
        File file = selectedData.getFile();
        if (!file.exists()) {
            DialogFactory.infoDialog("The file has already been deleted or moved");
            removeRecentFile(file);
        }
        else {
            EventBus.getIns().notifyOpenFile(new OpenFileEvent(file, selectedData.getWorkspaceData() != null));
            refresh(selectedData);
        }
    }

    /**
     * Update the order of recent files list for one file was opened.
     *
     * @param fileData
     */
    public void refresh(NodeData fileData) {
        // refresh the list
        Platform.runLater(() -> {
            if (fileData.isFolder()) {
                return;
            }
            listView.getItems().remove(fileData);
            listView.getItems().addFirst(fileData);
            if (listView.getItems().size() > RecentManager.MAX_SIZE) {
                listView.getItems().removeLast(); // remove last
            }
            listView.getSelectionModel().clearSelection();
        });
    }

    /**
     * Remove file item from the recent file list.
     *
     * @param file
     */
    public void removeRecentFile(File file) {
        RecentManager.getInstance().removeFromRecent(file);
        Platform.runLater(() -> {
            listView.getItems().remove(new NodeData(file));
            listView.getSelectionModel().clearSelection();
            listView.refresh();
        });
    }

    /**
     * Update record in the recent file list with the new file path.
     *
     * @param nodeData
     * @param newFile
     */
    public void updateRecentFile(NodeData nodeData, File newFile) {
        RecentManager.getInstance().removeFromRecent(nodeData.getFile());
        RecentManager.getInstance().addToRecent(newFile);
        int idx = listView.getItems().indexOf(nodeData);
        if (idx >= 0) {
            listView.getItems().remove(nodeData);
            listView.getSelectionModel().clearSelection();
            listView.getItems().add(idx, new NodeData(newFile));
            listView.refresh();
        }
    }

    public boolean hasData() {
        return !listView.getItems().isEmpty();
    }

}
