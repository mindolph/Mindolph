package com.mindolph.base.genai.rag;

public class EmbeddingContext {

    /**
     * @since 1.13.2
     */
    private boolean isEmbedded = false;

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean embedded) {
        isEmbedded = embedded;
    }
}
