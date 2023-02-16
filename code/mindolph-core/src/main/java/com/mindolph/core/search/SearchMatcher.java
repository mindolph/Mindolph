package com.mindolph.core.search;

import java.io.File;

/**
 * @author mindolph.com@gmail.com
 */
public interface SearchMatcher {

    boolean matches(File file, SearchParams searchParams);
}
