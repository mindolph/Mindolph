package com.mindolph.mindmap.utils;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.MindMapUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author allen
 */
public class MindMapUtilsTest {

    @Test
    public void removeSuccessorsAndDuplications() {
        MindMap<TopicNode> map = new MindMap<>();
        map.setRoot(new TopicNode(map, null, ""));
        TopicNode son = new TopicNode(map, map.getRoot(), "son");
        TopicNode grandson = new TopicNode(map, son, "grandson");

        List<TopicNode> topicNodes = MindMapUtils.removeDuplicatedAndDescendants(Arrays.asList(map.getRoot(), son, grandson));
        Assert.assertEquals(1, topicNodes.size());
    }
}
