package com.mindolph.base.plugin;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.SnippetViewable;

import java.util.List;
import java.util.Optional;

/**
 * @since 1.10
 */
public interface SnippetHelper {

    /**
     * Create snippet view by snippet type.
     *
     * @param snippetGroup
     * @return
     */
    <T extends SnippetViewable> Optional<T> createView(BaseSnippetGroup snippetGroup);

    /**
     * Snippet group that contains snippets definitions.
     *
     * @param fileType
     * @return
     */
    List<BaseSnippetGroup> getSnippetGroups(String fileType);

}
