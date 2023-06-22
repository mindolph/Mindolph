package com.mindolph.core.search;

import java.io.File;
import java.util.List;

/**
 * Matcher for searching in file, different file type might implement different instance.
 *
 * @author mindolph.com@gmail.com
 */
public interface SearchMatcher {

    boolean matches(File file, SearchParams searchParams);

    List<String> getMatched();
}
