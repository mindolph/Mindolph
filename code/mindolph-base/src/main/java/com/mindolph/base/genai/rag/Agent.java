package com.mindolph.base.genai.rag;

import dev.langchain4j.service.TokenStream;

/**
 * @since unknown
 */
public interface Agent {

    TokenStream chat(String humanMessage);
}
