package com.mindolph.base.genai.rag;

import dev.langchain4j.service.TokenStream;

/**
 * Define Agent interface as langchain4j.
 *
 * @since 1.13.0
 */
public interface Agent {

    TokenStream chat(String humanMessage);
}
