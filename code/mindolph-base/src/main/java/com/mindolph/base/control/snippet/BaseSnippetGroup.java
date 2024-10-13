package com.mindolph.base.control.snippet;

import com.mindolph.core.model.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Use '⨁' to define the position that caret will be after inserting code snippet.
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BaseSnippetGroup<T extends Snippet<?>> {

    protected String title = "[No title]";
    protected String description = "[No description]";
    protected boolean alwaysShow = false;

    protected List<T> snippets = new ArrayList<>();

    public BaseSnippetGroup() {
        init();
    }

    public abstract void init();

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public abstract String getFileType();

    public List<T> getSnippets() {
        return snippets;
    }

    public boolean isAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }
}
