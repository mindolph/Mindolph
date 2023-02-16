package com.mindolph.base;

import com.mindolph.core.model.NodeData;
import javafx.geometry.Orientation;

/**
 * @author mindolph.com@gmail.com
 */
public class EditorContext {

    protected NodeData workspaceData; // ref to project data

    protected NodeData fileData;

    protected Orientation orientation;

    public NodeData getWorkspaceData() {
        return workspaceData;
    }

    public void setWorkspaceData(NodeData workspaceData) {
        this.workspaceData = workspaceData;
    }

    public NodeData getFileData() {
        return fileData;
    }

    public void setFileData(NodeData fileData) {
        this.fileData = fileData;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}
