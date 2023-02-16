package com.mindolph.core.search;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 */
public class SearchService {

    private static final SearchService ins = new SearchService();

    private final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final Map<String, SearchMatcher> matchers = new HashMap<>();

    public static SearchService getIns() {
        return ins;
    }

    private SearchService() {
        SearchMatcher pureFileMatcher = (file, searchParams) -> {
            try {
                // TODO to be optimized via some algorithm which doesn't need to read all of a file.
                String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                if (searchParams.isCaseSensitive()) {
                    if (StringUtils.contains(s, searchParams.getKeywords())) {
                        return true;
                    }
                }
                else {
                    if (StringUtils.containsAnyIgnoreCase(s, searchParams.getKeywords())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        };
        this.matchers.put(TYPE_PLAIN_TEXT, pureFileMatcher);
        this.matchers.put(TYPE_MARKDOWN, pureFileMatcher);
        this.matchers.put(TYPE_PLANTUML, pureFileMatcher);
    }

    /**
     *
     * @param dir
     * @param fileFilter
     * @param searchParams
     * @return
     */
    public List<File> searchInFilesIn(File dir, IOFileFilter fileFilter, SearchParams searchParams) {
        log.debug("Find in files by '%s' with file filters %s".formatted(searchParams, fileFilter));
        IOFileFilter newFileFilter = fileFilter;
        if (!"all".equals(searchParams.getFileTypeName())) {
            newFileFilter = FileFilterUtils.and(fileFilter, new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return FilenameUtils.isExtension(file.getName(), searchParams.getFileTypeName());
                }

                @Override
                public boolean accept(File dir, String name) {
                    return false;
                }
            });
        }

        Collection<File> files = FileUtils.listFilesAndDirs(dir, newFileFilter, TrueFileFilter.INSTANCE);
        log.debug("Search in %d files".formatted(files.size()));
        if (searchParams != null && StringUtils.isNotBlank(searchParams.getKeywords())) {
            List<File> foundList = new ArrayList<>();
            for (File file : files) {
                SearchMatcher searchMatch = this.matchers.get(FilenameUtils.getExtension(file.getPath()));
                if (searchMatch != null && searchMatch.matches(file, searchParams)) {
                    foundList.add(file);
                }
            }
            for (File file : foundList) {
                log.debug(file.getPath());
            }
            log.debug("%d files matches".formatted(foundList.size()));
            return foundList;
        }
        return null;
    }

    public void registerMatcher(String fileType, SearchMatcher matcher) {
        this.matchers.put(fileType, matcher);
    }
}
