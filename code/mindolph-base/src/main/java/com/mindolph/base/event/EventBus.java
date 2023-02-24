package com.mindolph.base.event;

import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import javafx.scene.control.TreeItem;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public class EventBus {

    private final Logger log = LoggerFactory.getLogger(EventBus.class);

    private static final EventBus ins = new EventBus();

    private final EventSource<NotificationType> simpleNotification = new EventSource<>();
    // Events for workspace
    private EventSource<String> workspacesRestored;
    private EventSource<TreeItem<NodeData>> workspaceLoaded;
    private EventSource<WorkspaceRenameEvent> workspaceRenamed;
    private EventSource<WorkspaceMeta> workspaceClosed;

    // Events for files
    private final EventSource<List<File>> openedFileChange = new EventSource<>();
    private final EventSource<File> newFileToWorkspace = new EventSource<>();
    private final EventSource<OpenFileEvent> openFile = new EventSource<>();
    private final EventSource<NodeData> locateInWorkspace = new EventSource<>();
    private final EventSource<NodeData> fileDeleted = new EventSource<>();

    // Events for menu
    private final Map<MenuTag, EventStream<Boolean>> menuStateEvens = new HashMap<>(); // events to enable/disable menu items

    // file editor send status msg to anywhere listening
    private final Map<File, EventSource<StatusMsg>> statusMsgEvents = new HashMap<>();

    public static EventBus getIns() {
        return ins;
    }

    private EventBus() {
    }

    public void notify(NotificationType notificationType) {
        simpleNotification.push(notificationType);
    }

    public void notifyWorkspaceLoaded(TreeItem<NodeData> treeItem) {
        workspaceLoaded.push(treeItem);
    }

    public void notifyWorkspacesRestored() {
        workspacesRestored.push("");
    }

    public void notifyWorkspaceRenamed(WorkspaceRenameEvent event) {
        workspaceRenamed.push(event);
    }

    public void notifyWorkspaceClosed(WorkspaceMeta workspaceMeta) {
        workspaceClosed.push(workspaceMeta);
    }

    public void notifyMenuStateChange(MenuTag menuTag, boolean enable) {
        if (menuStateEvens.containsKey(menuTag)) {
            EventStream<Boolean> menuEventStream = menuStateEvens.get(menuTag);
            if (menuEventStream instanceof EventSource<Boolean> eventSource) {
                eventSource.push(enable);
            }
        }
        else {
            log.warn("No listener registered for menu item: %s".formatted(menuTag));
        }
    }

    /**
     * send empty status msg.
     *
     * @param file
     */
    public void notifyStatusMsg(File file) {
        notifyStatusMsg(file, new StatusMsg());
    }

    public void notifyStatusMsg(File file, StatusMsg statusMsg) {
        EventSource<StatusMsg> eventSource = statusMsgEvents.get(file);
        if (eventSource != null) {
            if (!eventSource.isObservingInputs()) {
                log.warn("No observer for event of file: %s".formatted(file));
            }
            else {
                eventSource.push(statusMsg);
            }
        }
        else {
            log.warn("Not found");
        }
    }

    public void disableMenuItems(MenuTag... menuTags) {
        for (MenuTag menuTag : menuTags) {
            notifyMenuStateChange(menuTag, false);
        }
    }

    public void enableMenuItems(MenuTag... menuTags) {
        for (MenuTag menuTag : menuTags) {
            notifyMenuStateChange(menuTag, true);
        }
    }

    public void notifyOpenedFileChange(List<File> openedFiles) {
        openedFileChange.push(openedFiles);
    }

    public void notifyNewFileToWorkspace(File file) {
        newFileToWorkspace.push(file);
    }

    public void notifyOpenFile(OpenFileEvent event) {
        openFile.push(event);
    }

    public void notifyLocateInWorkspace(NodeData data) {
        locateInWorkspace.push(data);
    }

    public void notifyDeletedFile(NodeData fileData) {
        fileDeleted.push(fileData);
    }

    public void subscribe(Consumer<NotificationType> consumer) {
        simpleNotification.subscribe(consumer);
    }

    public void subscribeWorkspaceLoaded(int max, Consumer<TreeItem<NodeData>> subscriber) {
        if (workspaceLoaded == null) workspaceLoaded = new EventSource<>();
        workspaceLoaded.subscribeFor(max, subscriber);
    }

    public void subscribeWorkspacesRestored(Consumer<String> subscriber) {
        if (workspacesRestored == null) workspacesRestored = new EventSource<>();
        workspacesRestored.subscribe(subscriber);
    }

    public void subscribeWorkspaceRenamed(Consumer<WorkspaceRenameEvent> subscriber) {
        if (workspaceRenamed == null) workspaceRenamed = new EventSource<>();
        workspaceRenamed.subscribe(subscriber);
    }

    public void subscribeWorkspaceClosed(Consumer<WorkspaceMeta> subscriber) {
        if (workspaceClosed == null) workspaceClosed = new EventSource<>();
        workspaceClosed.subscribe(subscriber);
    }

    public void subscribeMenuStateChange(MenuTag menuTag, Consumer<Boolean> subscriber) {
        if (!menuStateEvens.containsKey(menuTag)) {
            EventSource<Boolean> eventSource = new EventSource<>();
            eventSource.subscribe(subscriber);
            menuStateEvens.put(menuTag, eventSource);
        }
        else {
            log.debug("Subscriber for %s already exists, no registration again".formatted(menuTag));
        }
    }

    public void subscribeStatusMsgEvent(File file, Consumer<StatusMsg> consumer) {
        EventSource<StatusMsg> eventSource = statusMsgEvents.get(file);
        if (eventSource == null) {
            eventSource = new EventSource<>();
            statusMsgEvents.put(file, eventSource);
        }
        eventSource.subscribe(consumer);
    }

    public void subscribeOpenedFileChanges(Consumer<List<File>> consumer) {
        openedFileChange.subscribe(consumer);
    }

    public void subscribeNewFileToWorkspace(Consumer<File> consumer) {
        newFileToWorkspace.subscribe(consumer);
    }

    public void subscribeOpenFile(Consumer<OpenFileEvent> consumer) {
        openFile.subscribe(consumer);
    }

    public void subscribeLocateInWorkspace(Consumer<NodeData> consumer) {
        locateInWorkspace.subscribe(consumer);
    }

    public void subscribeDeletedFile(Consumer<NodeData> consumer) {
        fileDeleted.subscribe(consumer);
    }

    public enum MenuTag {
        UNDO, REDO, CUT, COPY, PASTE, NEW_FILE, OPEN_FILE, SAVE, SAVE_AS, SAVE_ALL, PRINT, CLOSE_TAB, FIND, REPLACE
    }


}
