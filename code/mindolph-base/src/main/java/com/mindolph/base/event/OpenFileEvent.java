package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;

import java.io.File;

public class OpenFileEvent {
//    private File file; // TODO could be NodeData with file
    private NodeData nodeData;
    private SearchParams searchParams;
    private boolean visibleInWorkspace;

    public OpenFileEvent(File file) {
//        this.file = file;
        this.nodeData = new NodeData(file);
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace) {
//        this.file = file;
        this.nodeData = new NodeData(file);
        this.visibleInWorkspace = visibleInWorkspace;
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace, SearchParams searchParams) {
//        this.file = file;
        this.nodeData = new NodeData(file);
        this.visibleInWorkspace = visibleInWorkspace;
        this.searchParams = searchParams;
    }

    public OpenFileEvent(NodeData nodeData, boolean visibleInWorkspace) {
        this.nodeData = nodeData;
        this.visibleInWorkspace = visibleInWorkspace;
    }

    public SearchParams getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(SearchParams searchParams) {
        this.searchParams = searchParams;
    }

//    public File getFile() {
//        return file;
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }


    public NodeData getNodeData() {
        return nodeData;
    }

    public OpenFileEvent setNodeData(NodeData nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    public boolean isVisibleInWorkspace() {
        return visibleInWorkspace;
    }

    public void setVisibleInWorkspace(boolean visibleInWorkspace) {
        this.visibleInWorkspace = visibleInWorkspace;
    }
}
