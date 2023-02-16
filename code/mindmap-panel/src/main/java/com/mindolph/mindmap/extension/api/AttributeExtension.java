package com.mindolph.mindmap.extension.api;

import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.mindmap.model.TopicNode;

/**
 * Interface for plug-ins processing attributes of model topics.
 *
 */
public interface AttributeExtension extends Extension, TopicFinder<TopicNode> {

    /**
     * Attribute name which can be returned if plug-in doesn't process attributes.
     */
    String NULL_ATTRIBUTE = ",=,";

    /**
     * Get name of attribute key associated with the plug-in.
     *
     * @return the name of the attribute key.
     * @see #NULL_ATTRIBUTE
     */
    String getAttributeKey();
}
