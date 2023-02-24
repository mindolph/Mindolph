package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @see WorkspaceListCell
 */
public class WorkspaceDialog extends BaseDialogController<Void> {
    private final Logger log = LoggerFactory.getLogger(WorkspaceDialog.class);

    @FXML
    private ListView<WorkspaceMeta> lvWorkspaces;

    public WorkspaceDialog() {
        dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Manage Workspaces")
                .fxmlUri("dialog/workspace_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .defaultValue(null)
                .resizable(true)
                .controller(this)
                .width(500)
                .build();

        lvWorkspaces.setCellFactory(param -> new WorkspaceListCell());
        lvWorkspaces.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                dialog.close();
            }
        });

        EventBus.getIns().subscribeWorkspaceRenamed(workspaceMeta -> {
            loadWorkspaceListView();
        });

        EventBus.getIns().subscribeWorkspaceClosed(workspaceMeta -> {
            loadWorkspaceListView();
        });
        WorkspaceList workspaceList = WorkspaceManager.getIns().getWorkspaceList();
        loadWorkspaceListView();


    }

    private void loadWorkspaceListView() {
        lvWorkspaces.getItems().clear();
        lvWorkspaces.getItems().addAll(WorkspaceManager.getIns().getWorkspaceList().getProjects());
        lvWorkspaces.refresh();
    }
}
