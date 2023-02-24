package com.mindolph.base.event;

import com.mindolph.core.meta.WorkspaceMeta;

/**
 * @author mindolph.com@gmail.com
 */
public class WorkspaceRenameEvent {
    private WorkspaceMeta original;
    private WorkspaceMeta target;

    public WorkspaceRenameEvent() {
    }

    public WorkspaceRenameEvent(WorkspaceMeta original, WorkspaceMeta target) {
        this.original = original;
        this.target = target;
    }

    public WorkspaceMeta getOriginal() {
        return original;
    }

    public WorkspaceRenameEvent setOriginal(WorkspaceMeta original) {
        this.original = original;
        return this;
    }

    public WorkspaceMeta getTarget() {
        return target;
    }

    public WorkspaceRenameEvent setTarget(WorkspaceMeta target) {
        this.target = target;
        return this;
    }
}
