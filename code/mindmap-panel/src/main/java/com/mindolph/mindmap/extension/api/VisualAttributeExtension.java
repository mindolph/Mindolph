package com.mindolph.mindmap.extension.api;

import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.image.Image;

/**
 * Plug-in to provide visual representation of attributes.
 *
 */
public interface VisualAttributeExtension extends AttributeExtension {
    /**
     * Get renderable object represents the attribute.
     *
     * @param config the configuration of context were it will be rendered
     * @param topic  the topic
     * @return object to render the attribute, null if it is not shown
     *
     */
    Image getScaledImage(MindMapConfig config, TopicNode topic);

    /**
     * Process click on image represents the attribute,
     *
     * @param context             the extension context
     * @param topic               the topic
     * @param activeGroupModifier true if any modifier to work with topic group (like SHIFT or CTRL) is active
     * @param clickCount          detected number of mouse clicks
     * @return true if the map was changed for the operation, false otherwise
     * 
     */
    boolean onClick(ExtensionContext context, TopicNode topic, boolean activeGroupModifier, int clickCount);

    /**
     * Get tool-tip for image represents the attribute.
     *
     * @param topic the topic
     * @return text to be shown as tool-tip or null if nothing
     */

    String getToolTip(TopicNode topic);

    /**
     * Is the visual attribute clickable one.
     *
     * @param topic the topic
     * @return true if the attribute is clickable one, false otherwise
     */
    boolean isClickable(TopicNode topic);
}
