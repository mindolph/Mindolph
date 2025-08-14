package com.mindolph.fx.helper;

import com.google.gson.Gson;
import com.mindolph.base.event.*;
import com.mindolph.core.WorkspaceManager;
import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.model.NodeData;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.RectangleUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_LAYOUT_MAIN_TREE_SIZE;

/**
 * A class used to save and restore the scene, including opening files, layout, etc.
 * Includes:
 * Main windows status(maximum, size of window).
 * Expanded folders in the workspace tree.
 * Size of layout.
 * Opened files.
 * Status of editor for each single file.
 * <p>
 * This class listens to some evens to collect data and save to preferences,
 * and also call listeners, which was registered outside, to restore the scene.
 *
 * @author mindolph.com@gmail.com
 * @see FxPreferences
 */
public class SceneRestore implements WindowEventHandler,
        WorkspaceViewResizedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SceneRestore.class);

    private final FxPreferences fxPreferences = FxPreferences.getInstance();
    private static SceneRestore ins;

    // Listeners for restore scene, should be assigned before calling restoreScene()
    private WorkspaceRestoreListener workspaceRestoreListener;
    private OpenedFileRestoreListener openingFileRestoreListener;
    private WindowRestoreListener windowRestoreListener;
    private WorkspaceViewSizeRestoreListener workspaceViewSizeRestoreListener;

    // If stopped, no state storing will be performed, for exiting app.
    private boolean isStopped = false;

    private SceneRestore() {
    }

    public static synchronized SceneRestore getInstance() {
        if (ins == null) {
            ins = new SceneRestore();
        }
        return ins;
    }

    // TODO probably need refactor
    public void saveScene(WorkspaceList workspaceList) {
        String json = new Gson().toJson(workspaceList);
        fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_PROJECTS, json);
    }

    public void restoreScene() {
        log.info("restore scene state from preferences");
        String disableReopen = System.getenv("disable-reopen");
        String disableWindowResize = System.getenv("disable-window-resize");
        Boolean reopenLastFiles = fxPreferences.getPreference("general.openLastFiles", Boolean.class, Boolean.TRUE);
        String jsonWorkspaces = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_PROJECTS, "{}");
        Rectangle2D rectWindow = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_WINDOW_RECTANGLE, Rectangle2D.class, new Rectangle2D(0, 0, 1000, 800));
        double workspaceViewSize = fxPreferences.getPreference(MINDOLPH_LAYOUT_MAIN_TREE_SIZE, 150.0);
        WorkspaceList workspaceList = WorkspaceManager.getIns().loadFromJson(jsonWorkspaces);
        if (!Boolean.parseBoolean(disableWindowResize)) {
            log.debug("restore window to: %s".formatted(RectangleUtils.rectangleInStr(rectWindow)));
            windowRestoreListener.onWindowRestore(rectWindow);
        }

        EventBus.getIns().subscribeWorkspacesRestored(s -> {
            log.info("Workspaces restored");
            log.info("Reopen disabled?: " + disableReopen);
            if (reopenLastFiles && !Boolean.parseBoolean(disableReopen)) {
                List<String> openedFiles = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_OPENED_FILE_LIST, new ArrayList<>());
                if (openedFiles != null) {
                    openingFileRestoreListener.onOpenedFileRestore(openedFiles.stream().limit(10).map(File::new).collect(Collectors.toList()));
                }
            }
            workspaceViewSizeRestoreListener.onWorkspaceViewSizeRestored(workspaceViewSize);
        });
        workspaceRestoreListener.onWorkspacesRestore(workspaceList);

        // listening save opened files
        EventBus.getIns().subscribeOpenedFileChanges(openedFiles -> {
            if (!isStopped) {
                // Opening files storage
                List<String> paths = openedFiles.stream().map(File::getPath).collect(Collectors.toList());
                fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_OPENED_FILE_LIST, paths);
            }
        }).subscribeTreeExpandCollapse(event -> {
            if (event.isExpand()) {
                onTreeItemExpanded(event.getTreeItem());
            }
            else {
                onTreeItemCollapsed(event.getTreeItem());
            }
        });
    }

    /**
     * Stop recording all states.
     */
    public void stop() {
        isStopped = true;
    }

    public void onTreeItemExpanded(TreeItem<?> treeItem) {
        if (!isStopped) {
            if (treeItem.isExpanded()) {
                NodeData nodeData = (NodeData) treeItem.getValue();
                String filePath = nodeData.getFile().getPath();
                List<String> expandedList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
                if (!expandedList.contains(filePath)) {
                    expandedList.add(filePath);
                    fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, expandedList);
                }
            }
        }
    }

    public void onTreeItemCollapsed(TreeItem<?> treeItem) {
        if (!isStopped) {
            if (!treeItem.isExpanded()) {
                log.debug("tree node collapsed: " + treeItem);
                NodeData nodeData = (NodeData) treeItem.getValue();
                String filePath = nodeData.getFile().getPath();
                this.removeFromExpandedList(filePath);
            }
        }
    }

    /**
     * Remove the file path from the expanded list (if exists)
     *
     * @param filePath
     */
    public void removeFromExpandedList(String filePath) {
        List<String> expandedList = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, new ArrayList<>());
        expandedList.removeIf(s -> s.trim().equals(filePath));
        fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_TREE_EXPANDED_LIST, expandedList);
    }

    @Override
    public void onWindowResized(Rectangle2D rectangle) {
        log.debug("Save application window position&size: " + RectangleUtils.rectangleInStr(rectangle));
        fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_WINDOW_RECTANGLE, rectangle);
    }

    @Override
    public void onWorkspaceViewResized(double newSize) {
        fxPreferences.savePreference(MINDOLPH_LAYOUT_MAIN_TREE_SIZE, newSize);
    }

    public void setWorkspacesRestoreListener(WorkspaceRestoreListener workspaceRestoreListener) {
        this.workspaceRestoreListener = workspaceRestoreListener;
    }

    public void setOpeningFileRestoreListener(OpenedFileRestoreListener openingFileRestoreListener) {
        this.openingFileRestoreListener = openingFileRestoreListener;
    }

    public void setWindowRestoreListener(WindowRestoreListener windowRestoreListener) {
        this.windowRestoreListener = windowRestoreListener;
    }

    public void setWorkspaceViewSizeRestoreListener(WorkspaceViewSizeRestoreListener workspaceViewSizeRestoreListener) {
        this.workspaceViewSizeRestoreListener = workspaceViewSizeRestoreListener;
    }
}
