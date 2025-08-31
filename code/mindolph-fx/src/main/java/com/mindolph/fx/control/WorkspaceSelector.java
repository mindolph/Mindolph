package com.mindolph.fx.control;

import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * workspace dir path -> workspace meta
 *
 * @since 1.8
 */
public class WorkspaceSelector extends ComboBox<Pair<String, WorkspaceMeta>> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceSelector.class);

    public WorkspaceSelector() {
        super();
        super.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, WorkspaceMeta> pair) {
                return pair == null ? "" : pair.getValue().getName();
            }

            @Override
            public Pair<String, WorkspaceMeta> fromString(String string) {
                return null;
            }
        });
        super.setCellFactory(pairListView -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, WorkspaceMeta> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    setText(item.getValue().getName());
                }
            }
        });
    }

    /**
     * Load workspaces and select the specified workspace, if not provided and selectFirst=true, select the first workspace in the list.
     *
     * @param workspaceList
     * @param currentWorkspace
     * @param selectFirst works only when currentWorkspace == null
     */
    public void loadWorkspaces(WorkspaceList workspaceList, WorkspaceMeta currentWorkspace, boolean selectFirst) {
        super.getItems().clear();
        super.getItems().addAll(workspaceList.getProjects().stream().map(meta -> new Pair<>(meta.getBaseDirPath(), meta)).toList());

        // init the active workspace if the workspace exists
        if (currentWorkspace != null) {
            String activeWorkspacePath = currentWorkspace.getBaseDirPath();
            log.debug("Last active workspace: %s".formatted(currentWorkspace.getBaseDirPath()));
            if (StringUtils.isNotBlank(activeWorkspacePath)
                    && super.getItems().stream().anyMatch(p -> p.getKey().equals(activeWorkspacePath))) {
                // if the workspace dir is sub-dir of existing workspace, must exactly match the path.
                // (the WorkspaceList.matchByFilePath() matches a workspace with the deepest path).
                super.setValue(new Pair<>(activeWorkspacePath, workspaceList.matchByExactPath(activeWorkspacePath)));
            }
            else {
                if (selectFirst) this.selectFirst(workspaceList);
            }
        }
        else {
            if (selectFirst) this.selectFirst(workspaceList);
        }
    }

    private void selectFirst(WorkspaceList workspaceList) {
        Optional<WorkspaceMeta> first = workspaceList.getProjects().stream().findFirst();
        first.ifPresent(workspaceMeta -> super.setValue(new Pair<>(workspaceMeta.getBaseDirPath(), workspaceMeta)));
    }

    public void select(WorkspaceMeta workspaceMeta) {
        super.getSelectionModel().select(new Pair<>(workspaceMeta.getBaseDirPath(), workspaceMeta));
    }

    public WorkspaceMeta getSelectedWorkspace() {
        return super.getSelectionModel().getSelectedItem().getValue();
    }
}
