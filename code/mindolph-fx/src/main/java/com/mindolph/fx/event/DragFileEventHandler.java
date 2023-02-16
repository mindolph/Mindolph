package com.mindolph.fx.event;

import com.mindolph.core.model.NodeData;

import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface DragFileEventHandler {

    void onFilesDragged(List<NodeData> files, NodeData target);
}
