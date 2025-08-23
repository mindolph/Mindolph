package com.mindolph.base.genai.rag;

import dev.langchain4j.service.TokenStream;

/**
 * Define Agent interface as langchain4j.
 *
 * @since unknown
 */
public interface Agent {

    TokenStream chat(String humanMessage);
}
