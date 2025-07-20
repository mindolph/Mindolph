package com.mindolph.base.event;

import java.io.File;

/**
 * event of file change including creation, deletion.
 *
 * @since 1.12.3
 */
public class FileChangeEvent {
    private FileChangeType type;
    private File file;

    public FileChangeEvent(FileChangeType type, File file) {
        this.type = type;
        this.file = file;
    }

    public FileChangeType getType() {
        return type;
    }

    public void setType(FileChangeType type) {
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public enum FileChangeType {
        CREATE, DELETE
    }
}
