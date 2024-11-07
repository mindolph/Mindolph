package com.mindolph.base.event;

import com.mindolph.core.model.NodeData;

/**
 *
 * @param oldData data of file that has been deactivated.
 * @param newData data of file that has been activated.
 * @since 1.10
 */
public record FileActivatedEvent(NodeData oldData, NodeData newData) {
}
