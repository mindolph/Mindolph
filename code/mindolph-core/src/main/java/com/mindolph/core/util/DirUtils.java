package com.mindolph.core.util;

import com.mindolph.core.constant.FolderConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implement this dir search utils because neither JDK nor Commons-io can search
 * sub-dirs recursively when a dir is ignored during searching.
 *
 * @author mindolph.com@gmail.com
 */
public class DirUtils {

    /**
     * Find sub dirs in a specified dir by keyword.
     * The exclusion of a dir won't ignore it's sub-dirs.
     *
     * @param dirFile
     * @param keyword
     * @return
     */
    public static List<File> findDirsByKeyword(File dirFile, String keyword) {
        List<File> result = new ArrayList<>();
        AbstractFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !FolderConstants.EXCLUDE_DIRS.contains(file.getName());
            }
        };
        AbstractFileFilter dirNameFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory()
                        && Strings.CI.contains(FilenameUtils.getBaseName(file.getPath()), keyword);
            }
        };
        findRecursively(dirFile, dirFilter, dirNameFilter, result);
        return result;
    }

    private static void findRecursively(File dir, IOFileFilter dirFilter, IOFileFilter dirFileFilter, List<File> result) {
        // exclude dirs that won't go deeper
        File[] subDirs = dir.listFiles((FileFilter) dirFilter);
        if (subDirs == null) {
            return;
        }
        // search sub dirs (recursively)
        for (File subDir : subDirs) {
            findRecursively(subDir, dirFilter, dirFileFilter, result);
        }
        File[] dirs = dir.listFiles((FileFilter) dirFileFilter);
        if (dirs != null && ArrayUtils.isNotEmpty(dirs)) {
            result.addAll(List.of(dirs));
        }
    }

}
