package com.mindolph.base.event;

import com.mindolph.core.search.SearchParams;

import java.io.File;

public class OpenFileEvent {
    private File file; // TODO could be NodeData with file
    private SearchParams searchParams;
    private boolean visibleInWorkspace;

    public OpenFileEvent(File file) {
        this.file = file;
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace) {
        this.file = file;
        this.visibleInWorkspace = visibleInWorkspace;
    }

    public OpenFileEvent(File file, boolean visibleInWorkspace, SearchParams searchParams) {
        this.file = file;
        this.visibleInWorkspace = visibleInWorkspace;
        this.searchParams = searchParams;
    }

    public SearchParams getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(SearchParams searchParams) {
        this.searchParams = searchParams;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isVisibleInWorkspace() {
        return visibleInWorkspace;
    }

    public void setVisibleInWorkspace(boolean visibleInWorkspace) {
        this.visibleInWorkspace = visibleInWorkspace;
    }
}
