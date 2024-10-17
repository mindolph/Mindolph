package com.mindolph.base.plugin;

import java.util.Collection;
import java.util.Optional;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public interface Plugin {

    /**
     * low is high priority to be processed.
     *
     * @return
     */
    Integer getOrder();

    /**
     * One plugin can support multiple file types.
     *
     * @return
     */
    Collection<String> supportedFileTypes();

    /**
     * Input helper for supported file types.
     *
     * @return
     */
    Optional<InputHelper> getInputHelper();

    /**
     * Generator for generating text.
     *
     * @return
     */
    Optional<Generator> getGenerator(Object editorId, String fileType);

    /**
     * Snippet helper to manage the snippets data and view for supported file types.
     *
     * @return
     */
    Optional<SnippetHelper> getSnippetHelper();
}
