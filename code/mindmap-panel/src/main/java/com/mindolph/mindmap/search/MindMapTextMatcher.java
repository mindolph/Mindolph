package com.mindolph.mindmap.search;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.core.search.BaseSearchMatcher;
import com.mindolph.core.search.MatchedItem;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.util.FunctionUtils;
import com.mindolph.mindmap.RootTopicCreator;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.model.TopicNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(MindMapTextMatcher.class);

    private static final String NODE_CONNECTOR = " → ";
    private Set<Extra.ExtraType> extras;
    private Set<TopicFinder<TopicNode>> TOPIC_FINDERS;

    public MindMapTextMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        super.matches(file, searchParams);
        log.debug("try match in file: " + file);
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
            boolean contained = false;
            while (next != null) {
                contained = true;
                log.debug("found topic: " + StringUtils.abbreviate(next.getText(), 100));
                if (returnContextEnabled) {
                    List<TopicNode> pathNodes = next.getPath();
                    pathNodes.remove(pathNodes.size() - 1);
                    String path = pathNodes.stream().map(topicNode -> StringUtils.abbreviate(topicNode.getText(), 16))
                            .collect(Collectors.joining(NODE_CONNECTOR));

                    if (FunctionUtils.textContains(searchParams.isCaseSensitive()).apply(next.getText(), searchParams.getKeywords())) {
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
            if (returnContextEnabled) {
                List<MatchedItem> matched = foundMap.keySet().stream().map(t -> new MatchedItem(foundMap.get(t), createAnchor(t))).toList();
                super.matched.addAll(matched);
            }
            return contained;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private MindMapAnchor createAnchor(TopicNode topicNode) {
        MindMapAnchor anchor = new MindMapAnchor();
        anchor.setText(topicNode.getText());
        anchor.setParentText(topicNode.getParent().getText());
        return anchor;
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
