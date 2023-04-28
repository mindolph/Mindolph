package com.mindolph.mindmap.search;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.core.search.BaseSearchMatcher;
import com.mindolph.core.search.SearchParams;
import com.mindolph.mindmap.RootTopicCreator;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.PatternUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 */
public class MindMapTextMatcher extends BaseSearchMatcher {

    private Set<Extra.ExtraType> extras;
    private Set<TopicFinder<TopicNode>> TOPIC_FINDERS;

    public MindMapTextMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
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
            Pattern pattern = PatternUtils.string2pattern(searchParams.getKeywords(), searchParams.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
            TopicNode next = mindMap.findNext(file.getParentFile(), mindMap.getRoot(), pattern, true, extras, TOPIC_FINDERS);
            if (next != null) {
                if (returnContextEnabled) {
                    List<TopicNode> pathNodes = next.getPath();
                    pathNodes.remove(pathNodes.size() - 1);
                    String path = pathNodes.stream().map(topicNode -> StringUtils.abbreviate(topicNode.getText(), 16))
                            .collect(Collectors.joining(" → "));

                    if (next.getText().contains(searchParams.getKeywords())) {
                        super.matchContext = path + " → " + next.getText();
                    }
                    else {
                        super.matchContext = path + " → " + StringUtils.abbreviate(next.getText(), 64);;
                        for (Extra<?> extra : next.getExtras().values()) {
                            if (extra.containsPattern(file.getParentFile(), pattern)) {
                                super.matchContext += " → " + StringUtils.abbreviate(extra.getAsString(), 64);
                                break;
                            }
                        }
                    }
                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
