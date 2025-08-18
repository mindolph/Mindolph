package com.mindolph.base.genai.rag;

import com.igormaznitsa.mindmap.model.*;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.HierarchicalDocumentSplitter;
import dev.langchain4j.model.TokenCountEstimator;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.function.Consumer;

/**
 * TBD
 */
public class MindMapDocumentSplitter extends HierarchicalDocumentSplitter {

    protected MindMapDocumentSplitter(int maxSegmentSizeInChars, int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars);
    }

    protected MindMapDocumentSplitter(int maxSegmentSizeInChars, int maxOverlapSizeInChars, HierarchicalDocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, subSplitter);
    }

    protected MindMapDocumentSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator);
    }

    protected MindMapDocumentSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, TokenCountEstimator tokenCountEstimator, DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
    }

    @Override
    protected String[] split(String text) {
        try {
            MindMap mindMap = new MindMap(new StringReader(text), new RootCreate() {
                @Override
                public Topic createRoot(MindMap map) {
                    return new Topic(map, null, "");
                }

                @Override
                public Topic createRoot(MindMap map, String text) {
                    return new Topic(map, null, text);
                }

                @Override
                public Topic createRoot(MindMap map, String text, Extra[] extras) {
                    return new Topic(map, null, text, extras);
                }
            });
            List<Topic> firstLevelNodes = mindMap.getRoot().getChildren();
            return firstLevelNodes.stream().map(t -> {
                StringBuilder buf = new StringBuilder();
                mindMap.traverseTopicTree(t, (Consumer<Topic>) topic -> buf.append(topic.getText()).append(Constants.NEXT_LINE));
                return buf.toString();
            }).toList().toArray(new String[]{});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String joinDelimiter() {
        return Constants.NEXT_LINE;
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentByLineSplitter(maxSegmentSize, maxOverlapSize, tokenCountEstimator);
    }
}
