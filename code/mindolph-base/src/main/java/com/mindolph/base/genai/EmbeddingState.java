package com.mindolph.base.genai;

public enum EmbeddingState {
    INIT,  // initial state, not ready to do embedding
    PREPARING,
    READY, // ready to do embedding
    EMBEDDING, // start embedding or on progress
    UNEMBEDDING, // start unembedding or on progress
    DONE // embedding/unembedding is done
}
