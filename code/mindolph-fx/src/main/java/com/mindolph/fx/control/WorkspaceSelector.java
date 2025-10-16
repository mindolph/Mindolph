package com.mindolph.fx.control;

import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.mfx.PausableChangeListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * workspace dir path -> workspace meta
 * NOTE: If you want to be able to pause the value change listener, use listenValueChange() method to register listener.
 *
 * @since 1.8
 */
public class WorkspaceSelector extends ComboBox<Pair<String, WorkspaceSelector.LabeledWorkspaceMeta>> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceSelector.class);

    /**
     * a pausable wrapped value change listener for the value of component.
     */
    PausableChangeListener<Pair<String, LabeledWorkspaceMeta>> valueChangeListener;

    public WorkspaceSelector() {
        super();
        super.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, LabeledWorkspaceMeta> pair) {
                return pair == null ? "" : pair.getValue().getWorkspaceMeta().getName();
            }

            @Override
            public Pair<String, LabeledWorkspaceMeta> fromString(String string) {
                return null;
            }
        });
        super.setCellFactory(pairListView -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, LabeledWorkspaceMeta> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                }
                else {
                    if (StringUtils.isBlank(item.getValue().getLabel())) {
                        setText(item.getValue().getWorkspaceMeta().getName());
                    }
                    else {
                        // No exact format here to let the
                        setText("%s%s".formatted(item.getValue().getWorkspaceMeta().getName(), item.getValue().getLabel()));
                    }
                }
            }
        });
    }

    /**
     * Load workspaces and select the specified workspace, if not provided and selectFirst=true, select the first workspace in the list.
     *
     * @param workspaceList
     * @param currentWorkspace
     * @param selectFirst      select first workspace only when currentWorkspace == null
     */
    public void loadWorkspaces(WorkspaceList workspaceList, WorkspaceMeta currentWorkspace, boolean selectFirst) {
        super.getItems().clear();
        super.getItems().addAll(workspaceList.getProjects().stream().map(meta -> new Pair<>(meta.getBaseDirPath(), new LabeledWorkspaceMeta(meta))).toList());

        // init the active workspace if the workspace exists
        if (currentWorkspace != null) {
            String activeWorkspacePath = currentWorkspace.getBaseDirPath();
            log.debug("Last active workspace: %s".formatted(currentWorkspace.getBaseDirPath()));
            if (StringUtils.isNotBlank(activeWorkspacePath)
                    && super.getItems().stream().anyMatch(p -> p.getKey().equals(activeWorkspacePath))) {
                // if the workspace dir is sub-dir of existing workspace, must exactly match the path.
                // (the WorkspaceList.matchByFilePath() matches a workspace with the deepest path).
                super.setValue(new Pair<>(activeWorkspacePath, new LabeledWorkspaceMeta(workspaceList.matchByExactPath(activeWorkspacePath))));
            }
            else {
                if (selectFirst) this.selectFirst(workspaceList);
            }
        }
        else {
            if (selectFirst) this.selectFirst(workspaceList);
        }
    }

    /**
     *
     * @param listener
     * @since 1.13.3
     */
    public void listenValueChange(ChangeListener<Pair<String, LabeledWorkspaceMeta>> listener) {
        this.valueChangeListener = PausableChangeListener.wrap(listener);
        super.valueProperty().addListener(this.valueChangeListener);
    }

    /**
     *
     * @param labelGenerator
     * @since 1.13.3
     */
    public void updateWorkspaceLabels(Function<WorkspaceMeta, String> labelGenerator) {
        super.getItems().forEach(item -> {
            item.getValue().setLabel(labelGenerator.apply(item.getValue().getWorkspaceMeta()));
        });
        this.reloadItems();
    }

    // reload all items and recover the selection without value changes emitted.
    private void reloadItems() {
        if (this.valueChangeListener != null) this.valueChangeListener.pause();
        int selected = super.getSelectionModel().getSelectedIndex();
        List<Pair<String, LabeledWorkspaceMeta>> items = super.getItems().stream().toList();
        super.getItems().clear();
        super.getItems().addAll(items);
        super.getSelectionModel().select(selected);
        if (this.valueChangeListener != null) this.valueChangeListener.resume();
    }

    private void selectFirst(WorkspaceList workspaceList) {
        Optional<WorkspaceMeta> first = workspaceList.getProjects().stream().findFirst();
        first.ifPresent(workspaceMeta -> super.setValue(new Pair<>(workspaceMeta.getBaseDirPath(), new LabeledWorkspaceMeta(workspaceMeta))));
    }

    public void select(WorkspaceMeta workspaceMeta) {
        super.getSelectionModel().select(new Pair<>(workspaceMeta.getBaseDirPath(), new LabeledWorkspaceMeta(workspaceMeta)));
    }

    public WorkspaceMeta getSelectedWorkspace() {
        return super.getSelectionModel().getSelectedItem().getValue().getWorkspaceMeta();
    }

    /**
     * Wrap the WorkspaceMeta with label.
     */
    public static class LabeledWorkspaceMeta {
        private WorkspaceMeta workspaceMeta;
        private String label;

        private LabeledWorkspaceMeta(WorkspaceMeta workspaceMeta, String label) {
            this.workspaceMeta = workspaceMeta;
            this.label = label;
        }

        public LabeledWorkspaceMeta(WorkspaceMeta workspaceMeta) {
            this(workspaceMeta, "");
        }

        public WorkspaceMeta getWorkspaceMeta() {
            return workspaceMeta;
        }

        public void setWorkspaceMeta(WorkspaceMeta workspaceMeta) {
            this.workspaceMeta = workspaceMeta;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (LabeledWorkspaceMeta) obj;
            return Objects.equals(this.workspaceMeta, that.workspaceMeta) &&
                    Objects.equals(this.label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workspaceMeta, label);
        }

    }
}
