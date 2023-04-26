package com.mindolph.mindmap.search;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.core.search.SearchMatcher;
import com.mindolph.core.search.SearchParams;
import com.mindolph.mindmap.RootTopicCreator;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.PatternUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class FileLinkMindMapSearchMatcher implements SearchMatcher {

    private static final Logger log = LoggerFactory.getLogger(FileLinkMindMapSearchMatcher.class);
    private Set<Extra.ExtraType> extras;
    private Set<TopicFinder<TopicNode>> TOPIC_FINDERS;

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

        File absolutPathFile = new File(searchParams.getWorkspaceDir(), searchParams.getKeywords());
        MindMap<TopicNode> mindMap;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            mindMap = new MindMap<>(reader, RootTopicCreator.defaultCreator);
            Pattern pattern = PatternUtils.string2pattern(absolutPathFile.getPath(), 0);
            if (mindMap.findNext(file.getParentFile(), mindMap.getRoot(), pattern, true, extras, TOPIC_FINDERS) != null) {
                log.debug("Found");
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public String getMatchContext() {
        return null;
    }
}
