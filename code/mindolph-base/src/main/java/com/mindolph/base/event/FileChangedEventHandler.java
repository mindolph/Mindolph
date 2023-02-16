package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;

/**
 * for file is moved, or it's content is changed.
 *
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface FileChangedEventHandler {

    void onFileChanged(NodeData fileData);
}
