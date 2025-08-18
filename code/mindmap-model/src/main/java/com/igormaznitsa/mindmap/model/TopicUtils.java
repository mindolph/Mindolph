package com.igormaznitsa.mindmap.model;

import java.util.ArrayList;
import java.util.List;

public class TopicUtils {

    /**
     * Remove duplicated and descendant.
     *
     * @param topics array to be processed
     * @return
     */
    public static <T extends Topic<T>> List<T> removeDuplicatedAndDescendants(List<T> topics) {
        List<T> result = new ArrayList<>();
        for (T t : topics) {
            if (result.contains(t) || topics.stream().anyMatch(t2 -> t.isAncestor(t2))) {
                continue; // exclude if it has ancestor
            }
            result.add(t);
        }
        return result;
    }

    public static String getTopicUid(Topic<?> topic) {
        return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
    }

    public static Extra<?> findExtra(Topic<?> topic, Extra.ExtraType type) {
        Extra<?> result = topic.getExtras().get(type);
        return result == null ? null : (result.isExportable() ? result : null);
    }
}
