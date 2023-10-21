package com.mindolph.core.util;

import com.mindolph.core.constant.SupportFileTypes;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
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

    public static boolean isParentFolder(File folder, File file) {
        return StringUtils.startsWith(file.getParentFile().getPath(), folder.getPath());
    }

    /**
     * Get real relative path even not in the save directory.
     *
     * @param file
     * @param ancestorDir
     * @return like "a/b/c" or "../a/b/c"
     * @deprecated
     */
    public static String getRelativePath(File file, File ancestorDir) {
        return ancestorDir.toPath().relativize(file.toPath()).toString();
    }

    /**
     * @deprecated
     * @param path
     * @param ancestorPath
     * @return
     */
    public static String getRelativePath(String path, String ancestorPath){
        return Path.of(ancestorPath).relativize(Path.of(path)).toString();
    }

//    public static String getRelativePath(String fullPath, String ancestorPath) {
//        String finalPath = StringUtils.substringAfter(fullPath, ancestorPath);
//        return StringUtils.stripStart(finalPath, File.separator);
//    }

    public static boolean isAbsolutePath(String path) {
        return StringUtils.startsWith(path, "/") || isWindowsPath(path);
    }

    public static boolean isWindowsPath(String path) {
        return winPathPattern.matcher(path).find();
    }

    public static boolean isImageFile(File file) {
        return isImagePath(file.getPath());
    }

    public static boolean isImagePath(String path) {
        return FilenameUtils.isExtension(path, SupportFileTypes.TYPE_PLAIN_JPG, SupportFileTypes.TYPE_PLAIN_PNG);
    }

}
