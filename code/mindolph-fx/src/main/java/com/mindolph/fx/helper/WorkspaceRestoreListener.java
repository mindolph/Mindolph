package com.mindolph.fx.helper;

import com.mindolph.core.meta.WorkspaceList;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface WorkspaceRestoreListener {

    /**
     *
     * @param workspaces couldn't be null
     */
    void onWorkspacesRestore(WorkspaceList workspaces);
}
