package com.mindolph.base.event;

import com.mindolph.core.search.SearchParams;

/**
 * Search results.
 *
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface SearchEventHandler {

    void onSearch(SearchParams searchParams);
}
