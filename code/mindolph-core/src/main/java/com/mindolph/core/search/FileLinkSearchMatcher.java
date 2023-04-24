package com.mindolph.core.search;

import com.mindolph.core.util.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author allen
 */
public class FileLinkSearchMatcher implements SearchMatcher {

    private static final Logger log = LoggerFactory.getLogger(FileLinkSearchMatcher.class);

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        try {
            // TODO to be optimized via some algorithm which doesn't need to read all of a file.
            String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String filePathInWorkspace = FileNameUtils.getRelativePath(file, searchParams.getWorkspaceDir());
            String fileParentPathInWorkspace = FilenameUtils.getPath(filePathInWorkspace);
            log.debug("file: " + filePathInWorkspace);
            String relativePath = FileNameUtils.getRelativePath(searchParams.getKeywords(), fileParentPathInWorkspace);
            log.debug("search for: " + relativePath);
            if (StringUtils.contains(s, relativePath)) {
                log.debug("Found");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
