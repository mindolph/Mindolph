package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.fx.control.WorkspaceSelector;
import com.mindolph.fx.view.WorkspaceViewSimple;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @since 1.8
 */
public class WorkspaceDialog extends BaseDialogController<WorkspaceDialog.Selection> {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceDialog.class);
    @FXML
    private WorkspaceSelector workspaceSelector;
    @FXML
    private WorkspaceViewSimple workspaceView;

    public WorkspaceDialog(String title, WorkspaceList workspaceList, WorkspaceMeta currentWorkspace) {
        dialog = new CustomDialogBuilder<Selection>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title)
                .fxmlUri("dialog/workspace_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(null)
                .resizable(true)
                .controller(this)
                .width(500)
                .build();
        workspaceSelector.getSelectionModel().selectedItemProperty().addListener((observableValue, workspaceMeta, selectedWorkspace) -> {
            if (selectedWorkspace != null) {
                workspaceView.loadWorkspace(selectedWorkspace.getValue(), true, true);
            }
            else {
                // clear the tree view if last workspace is closed.
                workspaceView.clear();
            }
        });

        workspaceSelector.loadWorkspaces(workspaceList, currentWorkspace);

        workspaceView.setOnKeyReleased(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                dialog.close();
            }
        });

        // after target folder selected.
        workspaceView.subscribeSelected(nodeData -> {
            log.debug("Selected target workspace: " + workspaceSelector.getSelectionModel().getSelectedItem().getKey());
            log.debug("Selected target folder: " + nodeData.getFile().getPath());
            result = new Selection(workspaceSelector.getSelectionModel().getSelectedItem().getValue(), nodeData.getFile());
        });
    }

    /**
     * Selection from WorkspaceDialog.
     *
     * @param workspaceMeta
     * @param folderPath
     */
    public record Selection(WorkspaceMeta workspaceMeta, File folderPath) {
    }
}
