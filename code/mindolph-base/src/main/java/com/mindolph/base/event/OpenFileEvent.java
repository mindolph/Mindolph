package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.SearchParams;

import java.io.File;

/**
 * The event that opens a file either from tree/list or search results.
 */
public class OpenFileEvent {
    /**
     * file to be opened.
     */
    private NodeData nodeData;
    /**
     * can be null, if provided, the editor will locate the first match automatically.
     */
    private SearchParams searchParams;
    private boolean visibleInWorkspace;

    public OpenFileEvent(File file) {
        this.nodeData = new NodeData(file);
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace) {
        this.nodeData = new NodeData(file);
        this.visibleInWorkspace = visibleInWorkspace;
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace, SearchParams searchParams) {
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
