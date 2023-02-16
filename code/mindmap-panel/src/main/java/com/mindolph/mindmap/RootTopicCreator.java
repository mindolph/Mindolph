package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.RootCreate;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.TopicNode;

public class RootTopicCreator implements RootCreate<TopicNode> {

    public static RootCreate<TopicNode> defaultCreator = new RootTopicCreator();

    @Override
    public TopicNode createRoot(MindMap<TopicNode> map) {
        return new TopicNode(map, null, "");
    }

    @Override
    public TopicNode createRoot(MindMap<TopicNode> map, String text) {
        return new TopicNode(map, null, text);
    }

    @Override
    public TopicNode createRoot(MindMap<TopicNode> map, String text, Extra<?>... extras) {
        return new TopicNode(map, null, text, extras);
    }

//    @Override
//    default TopicNode createRoot(MindMap<TopicNode> map) {
//        return new TopicNode(map, null, "");
//    }
//
//    @Override
//    default TopicNode createRoot(MindMap<TopicNode> map, String text) {
//        return new TopicNode(map, null, text);
//    }
//
//    @Override
//    default TopicNode createRoot(MindMap<TopicNode> map, String text, Extra<?>... extras) {
//        return new TopicNode(map, null, text, extras);
//    }
}
