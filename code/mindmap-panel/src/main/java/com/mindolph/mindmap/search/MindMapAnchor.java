package com.mindolph.mindmap.search;

import com.mindolph.core.search.Anchor;

/**
 * use text and parent text to identify the topic,
 * which is not appropriate but acceptable workaround,
 * until the mmd model supports unique ID stored for each topic.
 *
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class MindMapAnchor implements Anchor {
    String text;
    String parentText;

    public String getText() {
        return text;
    }

    public MindMapAnchor setText(String text) {
        this.text = text;
        return this;
    }

    public String getParentText() {
        return parentText;
    }

    public void setParentText(String parentText) {
        this.parentText = parentText;
    }
}
