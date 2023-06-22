package com.mindolph.mindmap.search;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.core.search.BaseSearchMatcher;
import com.mindolph.core.search.SearchParams;
import com.mindolph.mindmap.RootTopicCreator;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.model.TopicNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 */
public class MindMapTextMatcher extends BaseSearchMatcher {

    private static final String NODE_CONNECTOR = " → ";
    private Set<Extra.ExtraType> extras;
    private Set<TopicFinder<TopicNode>> TOPIC_FINDERS;

    public MindMapTextMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        super.matches(file, searchParams);
        if (this.extras == null) {
            this.extras = EnumSet.noneOf(Extra.ExtraType.class);
            extras.add(Extra.ExtraType.TOPIC);
            extras.add(Extra.ExtraType.NOTE);
            extras.add(Extra.ExtraType.FILE);
            extras.add(Extra.ExtraType.LINK);
            this.TOPIC_FINDERS = MindMapExtensionRegistry.getInstance().findAllTopicFinders();
        }

        MindMap<TopicNode> mindMap;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            mindMap = new MindMap<>(reader, RootTopicCreator.defaultCreator);
            Pattern pattern = searchParams.getPattern();
            Map<TopicNode, String> foundMap = new HashMap<>();// store found topic and remove if it's sub-topic found.
            TopicNode next = mindMap.findNext(file.getParentFile(), mindMap.getRoot(), pattern, true, extras, TOPIC_FINDERS);
            boolean contains = false;
            while (next != null) {
                contains = true;
                if (returnContextEnabled) {
                    List<TopicNode> pathNodes = next.getPath();
                    pathNodes.remove(pathNodes.size() - 1);
                    String path = pathNodes.stream().map(topicNode -> StringUtils.abbreviate(topicNode.getText(), 16))
                            .collect(Collectors.joining(NODE_CONNECTOR));

                    if (next.getText().contains(searchParams.getKeywords())) {
                        String text = path + NODE_CONNECTOR + super.extractInText(searchParams, next.getText(), 32);
                        removeAncestor(foundMap, next);
                        foundMap.put(next, text);
                    }
                    else {
                        String text = path + NODE_CONNECTOR + StringUtils.abbreviate(next.getText(), 64);
                        for (Extra<?> extra : next.getExtras().values()) {
                            if (extra.containsPattern(file.getParentFile(), pattern)) {
                                text += NODE_CONNECTOR + super.extractInText(searchParams, extra.getAsString(), 32);
                                removeAncestor(foundMap, next);
                                foundMap.put(next, text);
                                break;
                            }
                        }
                    }
                }
                next = mindMap.findNext(file.getParentFile(), next, pattern, true, extras, TOPIC_FINDERS);
            }
            super.matched.addAll(foundMap.values());
            return contains;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * remove topicNode's ancestor;
     *
     * @param foundMap
     * @param topicNode
     */
    private void removeAncestor(Map<TopicNode, String> foundMap, TopicNode topicNode) {
        TopicNode ancestor = null;
        for (TopicNode node : foundMap.keySet()) {
            if (topicNode.isAncestor(node)) {
                ancestor = node;
                break;
            }
        }
        if (ancestor != null) foundMap.remove(ancestor);
    }
}
