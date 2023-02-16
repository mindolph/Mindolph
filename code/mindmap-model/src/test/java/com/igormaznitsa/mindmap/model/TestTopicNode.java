package com.igormaznitsa.mindmap.model;

public class TestTopicNode extends Topic<TestTopicNode> {
    public TestTopicNode(MindMap<TestTopicNode> mindMap, TestTopicNode base, boolean copyChildren) {
        super(mindMap, base, copyChildren);
    }

    public TestTopicNode(MindMap<TestTopicNode> map, TestTopicNode parent, String text, Extra<?>... extras) {
        super(map, parent, text, extras);
    }

    @Override
    public TestTopicNode createChild(String text, Extra<?>... extras) {
        return new TestTopicNode(getMap(), this, text, extras);
    }

    @Override
    public TestTopicNode cloneTopic(boolean copyChildren) {
        return new TestTopicNode(getMap(), this, copyChildren);
    }

    public static RootCreate<TestTopicNode> testTopicCreator = new RootCreate<>() {
        @Override
        public TestTopicNode createRoot(MindMap<TestTopicNode> map) {
            return new TestTopicNode(map, null, "");
        }

        @Override
        public TestTopicNode createRoot(MindMap<TestTopicNode> map, String text) {
            return new TestTopicNode(map, null, text);
        }

        @Override
        public TestTopicNode createRoot(MindMap<TestTopicNode> map, String text, Extra<?>... extras) {
            return new TestTopicNode(map, null, text, extras);
        }
    };

}
