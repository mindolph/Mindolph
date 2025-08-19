package com.mindolph.base.genai.rag;

public record EmbeddingDocEntity(String id,
                                 String file_name,
                                 String file_path,
                                 int block_count,
                                 boolean embedded,
                                 String comment) {

}
