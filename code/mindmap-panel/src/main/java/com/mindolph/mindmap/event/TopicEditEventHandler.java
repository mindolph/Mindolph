package com.mindolph.mindmap.event;

import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;

/**
 * Handle the topic edit event from {@link com.mindolph.mindmap.MindMapView}
 *
 * @author mindolph.com@gmail.com
 */
public interface TopicEditEventHandler {

    /**
     * Raise start edit event to the text editor holder.
     *
     * @param text
     * @param font
     * @param topicBounds
     */
    void startEdit(String text, Font font, Rectangle2D topicBounds);

    /**
     * Raise end edit event to the text editor holder.
     */
    void endEdit();

}
