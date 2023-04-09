package com.mindolph.mindmap.clipboard;

import com.igormaznitsa.mindmap.model.*;
import com.mindolph.mindmap.model.TopicNode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 *
 */
public final class ClipboardTopicsContainer implements Serializable {

    private final TopicNode[] topics;

    /**
     *
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

    private static String oneLineTitle(TopicNode topic) {
        return topic.getText().replace(LINE_SEPARATOR, " ").trim();
    }

    public static String convertTopics(List<TopicNode> topics) {
        StringBuilder result = new StringBuilder();
        for (TopicNode t : topics) {
            if (result.length() > 0) {
                result.append(LINE_SEPARATOR);
            }
            result.append(convertTopic(t, 0));
        }
        return result.toString();
    }

    private static String convertTopic(TopicNode topic, int level) {
        StringBuilder result = new StringBuilder();
        String lineIndent = StringUtils.repeat(" ", level * 2);
        result.append(lineIndent).append(oneLineTitle(topic));
        TopicNode linkedTopic = null;
        for (Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
            if (e.getKey() == Extra.ExtraType.TOPIC) {
                ExtraTopic topicLink = ((ExtraTopic) e.getValue());
                TopicNode root = topic.findRoot();
                linkedTopic = root.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, topicLink.getValue());
            }
        }

        if (!topic.getExtras().isEmpty()) {
            for (Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
                switch (e.getKey()) {
                    case NOTE: {
                        if (Boolean.parseBoolean(topic.getAttributes().get(ExtraNote.ATTR_ENCRYPTED))) {
                            result.append(LINE_SEPARATOR).append(lineIndent).append("<ENCRYPTED NOTE>");
                        }
                        else {
                            for (String s : e.getValue().getAsString().split(LINE_SEPARATOR)) {
                                result.append(LINE_SEPARATOR).append(lineIndent).append("> ").append(s.trim());
                            }
                        }
                    }
                    break;
                    case TOPIC: {
                        if (linkedTopic != null) {
                            result.append(LINE_SEPARATOR).append(lineIndent).append("#(").append(oneLineTitle(linkedTopic)).append(')');
                        }
                    }
                    break;
                    case FILE: {
                        result.append(LINE_SEPARATOR).append(lineIndent).append("FILE=").append(e.getValue().getAsString());
                    }
                    break;
                    case LINK: {
                        result.append(LINE_SEPARATOR).append(lineIndent).append(e.getValue().getAsString());
                    }
                    break;
                }
            }
        }
        result.append(LINE_SEPARATOR);
        if (topic.hasChildren()) {
            for (TopicNode c : topic.getChildren()) {
                result.append(convertTopic(c, level + 1));
            }
        }
        return result.toString();
    }

    public boolean isEmpty() {
        return this.topics.length == 0;
    }

    public TopicNode[] getTopics() {
        return this.topics;
    }

}
