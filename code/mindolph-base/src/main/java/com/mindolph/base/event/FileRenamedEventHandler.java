package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;

import java.io.File;

/**
 * File renamed event handler, not only for file, but for folder and project(dir).
 * @deprecated to EventBus
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface FileRenamedEventHandler {

    /**
     *
     * @param fileData
     * @param newFile
     */
    void onFileRenamed(NodeData fileData, File newFile);

}
