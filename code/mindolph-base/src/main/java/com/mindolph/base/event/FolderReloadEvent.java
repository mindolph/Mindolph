package com.mindolph.base.event;

import java.io.File;
import java.util.List;

/**
 * Sends a reload folder event to the workspace with a list of folders where files have changed.
 *
 * @since 1.12.4
 */
public class FolderReloadEvent {

    /**
     * dirs need to be reloaded
     */
    private List<File> dirs;

    public FolderReloadEvent() {
    }

    public FolderReloadEvent(List<File> dirs) {
        this.dirs = dirs;
    }

    public List<File> getDirs() {
        return dirs;
    }

    public void setDirs(List<File> dirs) {
        this.dirs = dirs;
    }
}
