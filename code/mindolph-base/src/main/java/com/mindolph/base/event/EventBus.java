package com.mindolph.base.event;

import com.mindolph.core.model.Snippet;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.Anchor;
import javafx.scene.control.TreeItem;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Subscribe and emit events globally.
 *
 * @author mindolph.com@gmail.com
 */
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private static final EventBus ins = new EventBus();

    private final EventSource<NotificationType> simpleNotification = new EventSource<>();
    // Events for workspace
    private EventSource<String> workspacesRestored;
    private EventSource<TreeItem<NodeData>> workspaceLoaded;
    //    private EventSource<NodeData> fileLoaded;
    private final Map<NodeData, EventSource<NodeData>> fileLoadedEvents = new HashMap<>();
    private EventSource<Anchor> locateInFileEvents;
    private EventSource<Tree> outlineEvents;
    private EventSource<WorkspaceRenameEvent> workspaceRenamed;
    private EventSource<WorkspaceMeta> workspaceClosed;
    private final EventSource<TreeExpandCollapseEvent> treeExpandCollapseEventEventSource = new EventSource<>();

    // Events for files
    private final EventSource<List<File>> openedFileChange = new EventSource<>();
    // private final EventSource<File> newFileToWorkspace = new EventSource<>();
    private final EventSource<FileChangeEvent> fileChangeInWorkspace = new EventSource<>();
    private final EventSource<FolderReloadEvent> folderRefreshInWorkspace = new EventSource<>();
    private final EventSource<OpenFileEvent> openFile = new EventSource<>();
    private final EventSource<NodeData> openFileFail = new EventSource<>();
    private final EventSource<FileActivatedEvent> fileActivated = new EventSource<>();
    private final EventSource<NodeData> locateInWorkspace = new EventSource<>();
    private final EventSource<NodeData> fileDeleted = new EventSource<>();
    private final EventSource<FilePathChangedEvent> filePathChanged = new EventSource<>();
    private final EventSource<Snippet> snippetApply = new EventSource<>();

    // Events for menu
    private final Map<MenuTag, EventStream<Boolean>> menuStateEvens = new HashMap<>(); // events to enable/disable menu items

    // file editor send status msg to anywhere listening
    private final Map<File, EventSource<StatusMsg>> statusMsgEvents = new HashMap<>();

    // preference changed with file type (null for all file types)
    private final EventSource<String> preferenceChanged = new EventSource<>();

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

    public EventBus notifySnippetApply(Snippet snippet) {
        snippetApply.push(snippet);
        return this;
    }

    public EventBus subscribeSnippetApply(Consumer<Snippet> consumer) {
        snippetApply.subscribe(consumer);
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

    public EventBus subscribeMenuStateChange(MenuTag menuTag, Consumer<Boolean> subscriber) {
        if (!menuStateEvens.containsKey(menuTag)) {
            EventSource<Boolean> eventSource = new EventSource<>();
            menuStateEvens.put(menuTag, eventSource);
        }
        else {
            log.debug("Subscriber for %s already exists, no registration again".formatted(menuTag));
        }
        EventStream<Boolean> eventSource = menuStateEvens.get(menuTag);
        eventSource.subscribe(subscriber);
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

    public EventBus notifyWorkspaceLoaded(TreeItem<NodeData> treeItem) {
        workspaceLoaded.push(treeItem);
        return this;
    }

    public EventBus subscribeWorkspaceLoaded(int max, Consumer<TreeItem<NodeData>> subscriber) {
        if (workspaceLoaded == null) workspaceLoaded = new EventSource<>();
        workspaceLoaded.subscribeFor(max, subscriber);
        return this;
    }

    public EventBus notifyFileLoaded(NodeData fileData) {
        this.notify(fileLoadedEvents, fileData, fileData);
        return this;
    }

    public EventBus subscribeFileLoaded(NodeData fileData, Consumer<NodeData> consumer) {
        this.subscribe(fileLoadedEvents, fileData, consumer);
        return this;
    }

    public EventBus unsubscribeFileLoaded(NodeData fileData) {
        this.unsubscribe(fileLoadedEvents, fileData);
        return this;
    }

    public EventBus notifyFileActivated(FileActivatedEvent fileChange) {
        this.fileActivated.push(fileChange);
        return this;
    }

    public EventBus subscribeFileActivated(Consumer<FileActivatedEvent> anchor) {
        this.fileActivated.subscribe(anchor);
        return this;
    }

    public EventBus notifyLocateInFile(Anchor anchor) {
        this.locateInFileEvents.push(anchor);
        return this;
    }

    public EventBus subscribeLocateInFile(Consumer<Anchor> anchor) {
        if (locateInFileEvents == null) locateInFileEvents = new EventSource<>();
        this.locateInFileEvents.subscribe(anchor);
        return this;
    }

    public EventBus notifyOutline(Tree outlineTree) {
        this.outlineEvents.push(outlineTree);
        return this;
    }

    public EventBus subscribeOutline(Consumer<Tree> outlineTree) {
        if (outlineEvents == null) outlineEvents = new EventSource<>();
        this.outlineEvents.subscribe(outlineTree);
        return this;
    }

    public EventBus notifyWorkspacesRestored() {
        workspacesRestored.push("");
        return this;
    }

    public EventBus subscribeWorkspacesRestored(Consumer<String> subscriber) {
        if (workspacesRestored == null) workspacesRestored = new EventSource<>();
        workspacesRestored.subscribe(subscriber);
        return this;
    }

    public EventBus notifyWorkspaceRenamed(WorkspaceRenameEvent event) {
        workspaceRenamed.push(event);
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

    public EventBus subscribeStatusMsgEvent(File file, Consumer<StatusMsg> consumer) {
        EventSource<StatusMsg> eventSource = statusMsgEvents.get(file);
        if (eventSource == null) {
            eventSource = new EventSource<>();
            statusMsgEvents.put(file, eventSource);
        }
        eventSource.subscribe(consumer);
        return this;
    }

    public EventBus notifyOpenedFileChange(List<File> openedFiles) {
        openedFileChange.push(openedFiles);
        return this;
    }

    public EventBus subscribeOpenedFileChanges(Consumer<List<File>> consumer) {
        openedFileChange.subscribe(consumer);
        return this;
    }

    public EventBus notifyFileChangeInWorkspace(FileChangeEvent fileChange) {
        fileChangeInWorkspace.push(fileChange);
        return this;
    }

    public EventBus subscribeFileChangeInWorkspace(Consumer<FileChangeEvent> consumer) {
        fileChangeInWorkspace.subscribe(consumer);
        return this;
    }

    public EventBus notifyFolderRefreshInWorkspace(FolderReloadEvent event) {
        folderRefreshInWorkspace.push(event);
        return this;
    }

    public EventBus subscribeFolderRefreshInWorkspace(Consumer<FolderReloadEvent> consumer) {
        folderRefreshInWorkspace.subscribe((consumer));
        return this;
    }

//    public EventBus subscribeNewFileToWorkspace(Consumer<File> consumer) {
//        newFileToWorkspace.subscribe(consumer);
//        return this;
//    }
//
//    public EventBus notifyNewFileToWorkspace(File file) {
//        newFileToWorkspace.push(file);
//        return this;
//    }

    public EventBus subscribeOpenFileFail(Consumer<NodeData> consumer) {
        openFileFail.subscribe(consumer);
        return this;
    }

    public EventBus unsubscribeOpenFileFail(Consumer<NodeData> consumer) {
        openFileFail.removeObserver(consumer);
        return this;
    }

    public EventBus notifyOpenFileFail(NodeData event) {
        openFileFail.push(event);
        return this;
    }

    public EventBus subscribeOpenFile(Consumer<OpenFileEvent> consumer) {
        openFile.subscribe(consumer);
        return this;
    }

    public EventBus notifyOpenFile(OpenFileEvent event) {
        openFile.push(event);
        return this;
    }


    public EventBus notifyLocateInWorkspace(NodeData data) {
        locateInWorkspace.push(data);
        return this;
    }

    public EventBus subscribeLocateInWorkspace(Consumer<NodeData> consumer) {
        locateInWorkspace.subscribe(consumer);
        return this;
    }

    /**
     * Used for relative handling, not for workspace.
     *
     * @param fileData
     * @return
     */
    public EventBus notifyDeletedFile(NodeData fileData) {
        fileDeleted.push(fileData);
        return this;
    }

    public EventBus subscribeDeletedFile(Consumer<NodeData> consumer) {
        fileDeleted.subscribe(consumer);
        return this;
    }

    public EventBus subscribePreferenceChanged(Consumer<String> consumer) {
        preferenceChanged.subscribe(consumer);
        return this;
    }

    public EventBus notifyPreferenceChanged(String fileType) {
        preferenceChanged.push(fileType);
        return this;
    }


    /**
     * Emit event with payload <code>p</code> to all it's consumers.
     *
     * @param map Mapping from target to event source.
     * @param t Target for event emitting.
     * @param p Payload for event emitting.
     * @param <T> Type of target
     * @param <P> Type of payload
     */
    private <T, P> void notify(Map<T, EventSource<P>> map, T t, P p) {
        if (map.containsKey(t)) {
            EventSource<P> eventSource = map.get(t);
            if (eventSource != null) {
                eventSource.push(p);
            }
        }
    }

    /**
     * Subscribe event for target <code>t</code> with <code>consumer</code>.
     * Invoking this method again to same target registers multiple consumers.
     *
     * @param map Mapping from target to event source.
     * @param t Target for event emitting.
     * @param consumer Consumer for handling event to target
     * @param <T> Type of target
     * @param <P> Type of payload
     */
    private <T, P> void subscribe(Map<T, EventSource<P>> map, T t, Consumer<P> consumer) {
        if (!map.containsKey(t)) {
            EventSource<P> eventSource = new EventSource<>();
            map.put(t, eventSource);
        }
        EventSource<P> eventSource = map.get(t);
        eventSource.subscribe(consumer);
    }

    /**
     * Remove subscription.
     *
     * @param map
     * @param t
     * @param <T>
     * @param <P>
     */
    private <T, P> void unsubscribe(Map<T, EventSource<P>> map, T t) {
        map.remove(t);
    }


    public enum MenuTag {
        UNDO, REDO, CUT, COPY, PASTE, NEW_FILE, OPEN_FILE, SAVE, SAVE_AS, SAVE_ALL, PRINT, CLOSE_TAB, FIND, REPLACE, REMOVE_COLLECTION
    }

}
