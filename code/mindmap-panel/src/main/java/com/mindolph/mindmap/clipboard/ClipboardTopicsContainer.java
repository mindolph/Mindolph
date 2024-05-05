package com.mindolph.mindmap.clipboard;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.TopicNode;

import java.io.Serializable;

/**
 *
 */
public final class ClipboardTopicsContainer implements Serializable {

    private final TopicNode[] topics;

    /**
     * @param topics topics that don't have any ancestor-descendant relationship,
     *               otherwise any operations on these topics might with redundant topics.
     */
    public ClipboardTopicsContainer(TopicNode[] topics) {
        MindMap<TopicNode> fakeMap = new MindMap<>();
        this.topics = new TopicNode[topics.length];
        for (int i = 0; i < topics.length; i++) {
            this.topics[i] = new TopicNode(fakeMap, topics[i], true);
        }
    }

    public boolean isEmpty() {
        return this.topics.length == 0;
    }

    public TopicNode[] getTopics() {
        return this.topics;
    }

}
