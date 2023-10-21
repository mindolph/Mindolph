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
import java.util.*;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 */
public class SearchService {

    private static final SearchService ins = new SearchService();

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    // for searching content in files
    private final Map<String, SearchMatcher> textMatchers = new HashMap<>();
    // for searching file links in files
    private final Map<String, SearchMatcher> fileLinkMatchers = new HashMap<>();

    public static SearchService getIns() {
        return ins;
    }

    private SearchService() {
        SearchMatcher pureFileMatcher = new CodeSearchMatcher(true);
        this.textMatchers.put(TYPE_PLAIN_TEXT, pureFileMatcher);
        this.textMatchers.put(TYPE_MARKDOWN, pureFileMatcher);
        this.textMatchers.put(TYPE_PLANTUML, pureFileMatcher);
        // for file links (plantuml is not supported yet)
        FileLinkSearchMatcher fileLinkSearchMatcher = new FileLinkSearchMatcher();
        this.fileLinkMatchers.put(TYPE_PLAIN_TEXT, fileLinkSearchMatcher);
        this.fileLinkMatchers.put(TYPE_MARKDOWN, fileLinkSearchMatcher);
        this.fileLinkMatchers.put(TYPE_CSV, fileLinkSearchMatcher);
    }

    /**
     * @param dir
     * @param fileFilter
     * @param searchParams
     * @return
     */
    public List<FoundFile> searchInFilesIn(File dir, IOFileFilter fileFilter, SearchParams searchParams) {
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
            List<FoundFile> foundList = new ArrayList<>();
            for (File file : files) {
                SearchMatcher searchMatch = this.textMatchers.get(FilenameUtils.getExtension(file.getPath()));
                if (searchMatch != null && searchMatch.matches(file, searchParams)) {
                    foundList.add(new FoundFile(file, searchMatch.getMatched()));
                }
            }
            for (FoundFile foundFile : foundList) {
                log.debug(foundFile.getFile().getPath());
            }
            log.debug("%d files matches".formatted(foundList.size()));
            return foundList;
        }
        return null;
    }


    /**
     * Search files with link(s) to the target file whose path is as the keyword.
     *
     * @param dir
     * @param fileFilter
     * @param searchParams
     * @return
     */
    public List<File> searchLinksInFilesIn(File dir, IOFileFilter fileFilter, SearchParams searchParams) {
        log.debug("Find in files by '%s' with file filters %s".formatted(searchParams, fileFilter));
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        log.debug("Search in %d files".formatted(files.size()));
        if (searchParams != null && StringUtils.isNotBlank(searchParams.getKeywords())) {
            List<File> foundList = new ArrayList<>();
            for (File file : files) {
                SearchMatcher searchMatch = this.fileLinkMatchers.get(FilenameUtils.getExtension(file.getPath()));
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
        this.textMatchers.put(fileType, matcher);
    }

    public void registerFileLinkMatcher(String fileType, SearchMatcher matcher) {
        this.fileLinkMatchers.put(fileType, matcher);
    }
}
