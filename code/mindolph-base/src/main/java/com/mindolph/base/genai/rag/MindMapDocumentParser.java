package com.mindolph.base.genai.rag;

import com.igormaznitsa.mindmap.model.*;
import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

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
            String txtOriginal = new String(inputStream.readAllBytes(), charset);
            if (txtOriginal.isBlank()) {
                throw new BlankDocumentException();
            }

            MindMap<StubTopic> mindMap = new MindMap<>(new InputStreamReader(new ByteArrayInputStream(txtOriginal.getBytes())), new RootCreate<StubTopic>() {
                @Override
                public StubTopic createRoot(MindMap<StubTopic> map) {
                    return new StubTopic(map, null, null);
                }

                @Override
                public StubTopic createRoot(MindMap<StubTopic> map, String text) {
                    return new StubTopic(map, null, text);
                }

                @Override
                public StubTopic createRoot(MindMap<StubTopic> map, String text, Extra<?>... extras) {
                    return new StubTopic(map, null, text, extras);
                }
            });

            String txtMarkdown = ConvertUtils.convertTopics(mindMap, List.of(mindMap.getRoot()), true);

            Metadata metadata = new Metadata();
            return new MindMapDocument(txtMarkdown, metadata);
        } catch (BlankDocumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is a workaround ro using the ConvertUtils.
     */
    private class StubTopic extends Topic<StubTopic> {

        public StubTopic(MindMap<StubTopic> mindMap, StubTopic base, boolean copyChildren) {
            super(mindMap, base, copyChildren);
        }

        public StubTopic(MindMap<StubTopic> map, StubTopic parent, String text, Extra<?>... extras) {
            super(map, parent, text, extras);
        }
    }
}
