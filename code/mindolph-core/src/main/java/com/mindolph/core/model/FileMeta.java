package com.mindolph.core.model;

import java.io.File;

/**
 * File meta with project dir it belongs to.
 */
public class FileMeta {
    /**
     * Dir of the project of this file(or) folder.
     */
    private final File workspaceDir;

    /**
     * File of a file or a folder.
     */
    private final File dirOrFile;

    public FileMeta(File workspaceDir, File dirOrFile) {
        this.workspaceDir = workspaceDir;
        this.dirOrFile = dirOrFile;
    }

    public File getWorkspaceDir() {
        return workspaceDir;
    }

    public File getDirOrFile() {
        return dirOrFile;
    }
}
