package com.mindolph.core.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class FileNameUtils {

    // base path pattern for Windows
    private static final String PATH_BASE_WIN = "[a-zA-Z]:\\\\";
    private static final Pattern winPathPattern = Pattern.compile(PATH_BASE_WIN);

    /**
     * Append extension to a file name if it doesn't exist.
     *
     * @param fileName
     * @param extension
     * @return
     */
    public static String appendFileExtensionIfAbsent(String fileName, String extension) {
        if (StringUtils.isBlank(FilenameUtils.getExtension(fileName))) {
            return fileName + "." + extension;
        }
        return fileName;
    }

    /**
     * Append extension to a file name if the extension doesn't match.
     *
     * @param fileName
     * @param extension
     * @return
     */
    public static String appendFileExtensionIfNotMach(String fileName, String extension) {
        if (!FilenameUtils.isExtension(fileName, extension)) {
            return fileName + "." + extension;
        }
        return fileName;
    }

    public static String getRelativePath(File file, File ancestorDir) {
        return getRelativePath(file.getPath(), ancestorDir.getPath());
    }

    public static boolean isParentFolder(File folder, File file) {
        return file.getPath().startsWith(folder.getPath());
    }

    public static String getRelativePath(String fullPath, String ancestorPath) {
        String finalPath = StringUtils.substringAfter(fullPath, ancestorPath);
        return StringUtils.stripStart(finalPath, File.separator);
    }

    public static boolean isAbsolutePath(String path) {
        return StringUtils.startsWith(path, "/") || isWindowsPath(path);
    }

    public static boolean isWindowsPath(String path) {
        return winPathPattern.matcher(path).find();
    }

}
