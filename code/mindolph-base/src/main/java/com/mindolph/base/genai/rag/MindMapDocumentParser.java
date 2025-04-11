package com.mindolph.base.genai.rag;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.RootCreate;
import com.igormaznitsa.mindmap.model.Topic;
import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @since unknown
 */
public class MindMapDocumentParser extends TextDocumentParser {

    private final Charset charset;

    public MindMapDocumentParser() {
        this(UTF_8);
    }

    public MindMapDocumentParser(Charset charset) {
        this.charset = ensureNotNull(charset, "charset");
    }

    @Override
    public Document parse(InputStream inputStream) {
        try {
            String text = new String(inputStream.readAllBytes(), charset);
            if (text.isBlank()) {
                throw new BlankDocumentException();
            }
//            MindMap  mindMap = new MindMap(new InputStreamReader(inputStream), new RootCreate() {
//                @Override
//                public Topic createRoot(MindMap map) {
//                    return new Topic(map, null, "");
//                }
//
//                @Override
//                public Topic createRoot(MindMap map, String text) {
//                    return new Topic(map, null, text);
//                }
//
//                @Override
//                public Topic createRoot(MindMap map, String text, Extra[] extras) {
//                    return new Topic(map, null, text, extras);
//                }
//            });
//            StringBuilder buf = new StringBuilder();
//            mindMap.traverseTopicTree(o -> {
//                buf.append(o);
//            });
//            String string = buf.toString();
            Metadata metadata = new Metadata();
            return new MindMapDocument(text, metadata);
        } catch (BlankDocumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
