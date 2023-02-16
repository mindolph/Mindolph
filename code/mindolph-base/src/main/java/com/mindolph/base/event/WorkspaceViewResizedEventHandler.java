package com.mindolph.base.event;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface WorkspaceViewResizedEventHandler {

    void onWorkspaceViewResized(double newSize);
}
