package com.mindolph.base.event;

import com.mindolph.core.search.SearchParams;

import java.io.File;

/**
 * Handle event when open a file either from tree/list or search results.
 *
 * @author mindolph.com@gmail.com
 * @deprecated
 */
@FunctionalInterface
public interface OpenFileEventHandler {

    /**
     * @param file             file to open
     * @param searchParams     can be null, if provided, the editor will locate the first match automatically.
     * @param visibleInWorkspace this flag may not good here, but it was easiest that I can think about.
     */
    void onOpenFile(File file, SearchParams searchParams, boolean visibleInWorkspace);

}
