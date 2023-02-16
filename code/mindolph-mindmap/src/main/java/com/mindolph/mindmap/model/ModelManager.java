package com.mindolph.mindmap.model;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.EditorContext;
import com.mindolph.mindmap.RootTopicCreator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * @author mindolph.com@gmail.com
 */
public class ModelManager {

    private static final Logger log = LoggerFactory.getLogger(ModelManager.class);

    public static MindMap<TopicNode> loadMmdFile(EditorContext editorContext) {
        log.debug("Load mmd file: " + editorContext.getFileData().getFile());
        File file = editorContext.getFileData().getFile();
        try (StringReader reader = new StringReader(FileUtils.readFileToString(file, StandardCharsets.UTF_8))) {
            return new MindMap<>(reader, RootTopicCreator.defaultCreator);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load mind map file " + file, e);
        }
    }

    public static void fixWrongTopics(TopicNode parent) {
        BaseElement parentEle = (BaseElement) parent.getPayload();
        for (TopicNode child : parent.getChildren()) {
            BaseElement ele = (BaseElement) child.getPayload();
            if (ele.isLeftDirection() && !child.isLeftSidedTopic()) {
                log.debug("fix one:" + child.getText());
                child.makeTopicLeftSided(true);
            }
            fixWrongTopics(child);
        }
    }
}
