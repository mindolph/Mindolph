package com.mindolph.core.search;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class FileLinkSearchMatcher implements SearchMatcher {

    private static final Logger log = LoggerFactory.getLogger(FileLinkSearchMatcher.class);

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        try {
            // TODO to be optimized via some algorithm which doesn't need to read all of a file.
            String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String filePathInWorkspace = PathUtils.getRelativePath(file, searchParams.getWorkspaceDir());
            String fileParentPathInWorkspace = FilenameUtils.getPath(filePathInWorkspace);
            log.debug("file: %s".formatted(filePathInWorkspace));
            String relativePath = PathUtils.getRelativePath(searchParams.getKeywords(), fileParentPathInWorkspace);
            log.debug("search for: %s".formatted(relativePath));
            if (Strings.CS.contains(s, relativePath)) {
                log.debug("Found");
                return true;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<MatchedItem> getMatched() {
        return null;
    }
}
