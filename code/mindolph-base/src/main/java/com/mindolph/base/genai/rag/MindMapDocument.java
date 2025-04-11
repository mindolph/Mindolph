package com.mindolph.base.genai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;

public record MindMapDocument(String text, Metadata metadata) implements Document {

    public MindMapDocument(String text) {
        this(text, new Metadata());
    }

}
