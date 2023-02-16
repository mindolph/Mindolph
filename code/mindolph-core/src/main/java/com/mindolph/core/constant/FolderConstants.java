package com.mindolph.core.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public interface FolderConstants {

    /**
     * Exclude folders and all theirs sub folders.
     */
    List<String> EXCLUDE_DIRS = Arrays.asList(".git", ".svn");

    static void main(String[] args) {
        System.out.println(EXCLUDE_DIRS.contains(".git"));
        System.out.println(EXCLUDE_DIRS.contains("xxx"));
    }
}
