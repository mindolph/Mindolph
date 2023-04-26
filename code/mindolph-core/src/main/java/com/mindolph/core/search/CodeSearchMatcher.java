package com.mindolph.core.search;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author mindolph.com@gmail.com
 */
public class CodeSearchMatcher extends BaseSearchMatcher {

    public CodeSearchMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        try {
            // TODO to be optimized via some algorithm which doesn't need to read all of a file.
            String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            BiFunction<CharSequence, CharSequence, Boolean> contains =
                    searchParams.isCaseSensitive() ? StringUtils::contains : StringUtils::containsIgnoreCase;
            if (contains.apply(s, searchParams.getKeywords())) {
                if (returnContextEnabled) {
                    BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::indexOf : StringUtils::indexOfIgnoreCase;
                    Integer where = indexOf.apply(s, searchParams.getKeywords());
                    super.matchContext = strip(substring(s, Math.max(0, where - 64), where + 64));
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
