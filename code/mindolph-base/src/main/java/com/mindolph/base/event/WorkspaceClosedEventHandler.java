package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface WorkspaceClosedEventHandler {

    void onWorkspaceClosed(NodeData workspaceData);
}
