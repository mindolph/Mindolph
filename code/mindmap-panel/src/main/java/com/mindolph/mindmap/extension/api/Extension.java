package com.mindolph.mindmap.extension.api;

import com.mindolph.mindmap.extension.MindMapExtensionRegistry;

/**
 * The Main interface for any plug-in to be provided for mind map panel.
 *
 * @see MindMapExtensionRegistry
 */
public interface Extension extends Comparable<Extension> {
    /**
     * Recommended start order for custom plug-ins.
     */
    int EXT_EXTENSION_ORDER_BASE = 1000;

    /**
     * Order of the plug-in among another plug-ins.
     *
     * @return the order.
     */
    int getOrder();
}
