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
    private final EventSource<TreeExpandCollapseEvent> treeExpandCollapseEventEventSource = new EventSource<>();

    // Events for files
    private final EventSource<List<File>> openedFileChange = new EventSource<>();
    private final EventSource<File> newFileToWorkspace = new EventSource<>();
    private final EventSource<OpenFileEvent> openFile = new EventSource<>();
    private final EventSource<NodeData> locateInWorkspace = new EventSource<>();
    private final EventSource<NodeData> fileDeleted = new EventSource<>();
    private final EventSource<FilePathChangedEvent> filePathChanged = new EventSource<>();

    // Events for menu
    private final Map<MenuTag, EventStream<Boolean>> menuStateEvens = new HashMap<>(); // events to enable/disable menu items

    // file editor send status msg to anywhere listening
    private final Map<File, EventSource<StatusMsg>> statusMsgEvents = new HashMap<>();

    public static EventBus getIns() {
        return ins;
    }

    private EventBus() {
    }

    public EventBus notify(NotificationType notificationType) {
        simpleNotification.push(notificationType);
        return this;
    }

    public EventBus subscribe(Consumer<NotificationType> consumer) {
        simpleNotification.subscribe(consumer);
        return this;
    }

    public EventBus notifyTreeExpandCollapse(TreeItem<?> treeItem, boolean isExpand) {
        treeExpandCollapseEventEventSource.push(new TreeExpandCollapseEvent(treeItem, isExpand));
        return this;
    }

    public EventBus subscribeTreeExpandCollapse(Consumer<TreeExpandCollapseEvent> consumer) {
        treeExpandCollapseEventEventSource.subscribe(consumer);
        return this;
    }

    public EventBus notifyFilePathChanged(NodeData nodeData, File newFile) {
        filePathChanged.push(new FilePathChangedEvent(nodeData, newFile));
        return this;
    }

    public EventBus subscribeFilePathChanged(Consumer<FilePathChangedEvent> consumer) {
        filePathChanged.subscribe(consumer);
        return this;
    }

    public EventBus notifyWorkspaceLoaded(TreeItem<NodeData> treeItem) {
        workspaceLoaded.push(treeItem);
        return this;
    }

    public EventBus notifyWorkspacesRestored() {
        workspacesRestored.push("");
        return this;
    }

    public EventBus notifyWorkspaceRenamed(WorkspaceRenameEvent event) {
        workspaceRenamed.push(event);
        return this;
    }

    public EventBus notifyWorkspaceClosed(WorkspaceMeta workspaceMeta) {
        workspaceClosed.push(workspaceMeta);
        return this;
    }

    public EventBus notifyMenuStateChange(MenuTag menuTag, boolean enable) {
        if (menuStateEvens.containsKey(menuTag)) {
            EventStream<Boolean> menuEventStream = menuStateEvens.get(menuTag);
            if (menuEventStream instanceof EventSource<Boolean> eventSource) {
                eventSource.push(enable);
            }
        }
        else {
            log.warn("No listener registered for menu item: %s".formatted(menuTag));
        }
        return this;
    }

    /**
     * send empty status msg.
     *
     * @param file
     */
    public EventBus notifyStatusMsg(File file) {
        notifyStatusMsg(file, new StatusMsg());
        return this;
    }

    public EventBus notifyStatusMsg(File file, StatusMsg statusMsg) {
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
        return this;
    }

    public EventBus disableMenuItems(MenuTag... menuTags) {
        for (MenuTag menuTag : menuTags) {
            notifyMenuStateChange(menuTag, false);
        }
        return this;
    }

    public EventBus enableMenuItems(MenuTag... menuTags) {
        for (MenuTag menuTag : menuTags) {
            notifyMenuStateChange(menuTag, true);
        }
        return this;
    }

    public EventBus notifyOpenedFileChange(List<File> openedFiles) {
        openedFileChange.push(openedFiles);
        return this;
    }

    public EventBus notifyNewFileToWorkspace(File file) {
        newFileToWorkspace.push(file);
        return this;
    }

    public EventBus notifyOpenFile(OpenFileEvent event) {
        openFile.push(event);
        return this;
    }

    public EventBus subscribeOpenFile(Consumer<OpenFileEvent> consumer) {
        openFile.subscribe(consumer);
        return this;
    }

    public EventBus notifyLocateInWorkspace(NodeData data) {
        locateInWorkspace.push(data);
        return this;
    }

    public EventBus notifyDeletedFile(NodeData fileData) {
        fileDeleted.push(fileData);
        return this;
    }


    public EventBus subscribeWorkspaceLoaded(int max, Consumer<TreeItem<NodeData>> subscriber) {
        if (workspaceLoaded == null) workspaceLoaded = new EventSource<>();
        workspaceLoaded.subscribeFor(max, subscriber);
        return this;
    }

    public EventBus subscribeWorkspacesRestored(Consumer<String> subscriber) {
        if (workspacesRestored == null) workspacesRestored = new EventSource<>();
        workspacesRestored.subscribe(subscriber);
        return this;
    }

    public EventBus subscribeWorkspaceRenamed(Consumer<WorkspaceRenameEvent> subscriber) {
        if (workspaceRenamed == null) workspaceRenamed = new EventSource<>();
        workspaceRenamed.subscribe(subscriber);
        return this;
    }

    public EventBus subscribeWorkspaceClosed(Consumer<WorkspaceMeta> subscriber) {
        if (workspaceClosed == null) workspaceClosed = new EventSource<>();
        workspaceClosed.subscribe(subscriber);
        return this;
    }

    public EventBus subscribeMenuStateChange(MenuTag menuTag, Consumer<Boolean> subscriber) {
        if (!menuStateEvens.containsKey(menuTag)) {
            EventSource<Boolean> eventSource = new EventSource<>();
            eventSource.subscribe(subscriber);
            menuStateEvens.put(menuTag, eventSource);
        }
        else {
            log.debug("Subscriber for %s already exists, no registration again".formatted(menuTag));
        }
        return this;
    }

    public EventBus subscribeStatusMsgEvent(File file, Consumer<StatusMsg> consumer) {
        EventSource<StatusMsg> eventSource = statusMsgEvents.get(file);
        if (eventSource == null) {
            eventSource = new EventSource<>();
            statusMsgEvents.put(file, eventSource);
        }
        eventSource.subscribe(consumer);
        return this;
    }

    public EventBus subscribeOpenedFileChanges(Consumer<List<File>> consumer) {
        openedFileChange.subscribe(consumer);
        return this;
    }

    public EventBus subscribeNewFileToWorkspace(Consumer<File> consumer) {
        newFileToWorkspace.subscribe(consumer);
        return this;
    }

    public EventBus subscribeLocateInWorkspace(Consumer<NodeData> consumer) {
        locateInWorkspace.subscribe(consumer);
        return this;
    }

    public EventBus subscribeDeletedFile(Consumer<NodeData> consumer) {
        fileDeleted.subscribe(consumer);
        return this;
    }

    public enum MenuTag {
        UNDO, REDO, CUT, COPY, PASTE, NEW_FILE, OPEN_FILE, SAVE, SAVE_AS, SAVE_ALL, PRINT, CLOSE_TAB, FIND, REPLACE
    }


}
