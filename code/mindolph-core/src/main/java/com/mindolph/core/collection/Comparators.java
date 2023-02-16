package com.mindolph.core.collection;

import java.io.File;
import java.util.Comparator;

/**
 * @author mindolph.com@gmail.com
 */
public class Comparators {

    public static Comparator<File> FULL_PATH_COMPARATOR = (f1, f2) -> {
        if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
            return f1.compareTo(f2);
        }
        else {
            if (f1.isDirectory()) return -1;
            return 1;
        }
    };

    public static Comparator<File> FILE_NAME_COMPARATOR = (f1, f2) -> {
        if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
            return f1.getName().compareTo(f2.getName());
        }
        else {
            if (f1.isDirectory()) return -1;
            return 1;
        }
    };

    // order: folder, last modified file, file path
    public static Comparator<File> NAVIGATION_DEFAULT_COMPARATOR =
            Comparator.comparingInt((File file) -> {
                        if (file.isDirectory()) return -1;
                        return 1;
                    })
                    .thenComparingLong(file -> Long.MAX_VALUE - file.lastModified())
                    .thenComparing(File::getPath);

    // order: folder, file name
    public static Comparator<File> NAVIGATION_FILENAME_COMPARATOR =
            Comparator.comparingInt((File file) -> {
                        if (file.isDirectory()) return -1;
                        return 1;
                    })
                    .thenComparing(File::getName);
}
