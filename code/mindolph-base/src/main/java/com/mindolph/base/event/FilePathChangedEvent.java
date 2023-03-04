package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;

import java.io.File;

/**
 * @author allen
 */
public class FilePathChangedEvent {
    private NodeData nodeData;
    private File newFile;

    public FilePathChangedEvent(NodeData nodeData, File newFile) {
        this.nodeData = nodeData;
        this.newFile = newFile;
    }

    public NodeData getNodeData() {
        return nodeData;
    }

    public FilePathChangedEvent setNodeData(NodeData nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    public File getNewFile() {
        return newFile;
    }

    public FilePathChangedEvent setNewFile(File newFile) {
        this.newFile = newFile;
        return this;
    }
}
