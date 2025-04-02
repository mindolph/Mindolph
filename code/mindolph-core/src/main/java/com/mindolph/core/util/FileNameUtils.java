package com.mindolph.core.util;

import com.mindolph.core.constant.SupportFileTypes;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;

import java.io.File;

/**
 * @author mindolph.com@gmail.com
 */
public class FileNameUtils {

    /**
     * Extract all dir names in file path.
     *
     * @param path absolute or relative path
     * @return
     */
    public static String[] parseDirNames(String path) {
        return StringUtils.split(path, File.separatorChar);
    }

    /**
     * Append extension to a file name if it doesn't exist.
     *
     * @param fileName
     * @param extension
     * @return
     */
    public static String appendFileExtensionIfAbsent(String fileName, String extension) {
        if (!StringUtils.equals(extension, FilenameUtils.getExtension(fileName))) {
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

    public static boolean isImageFile(File file) {
        return isImagePath(file.getPath());
    }

    public static boolean isImagePath(String path) {
        return FilenameUtils.isExtension(path,
                SupportFileTypes.TYPE_PLAIN_JPG,
                SupportFileTypes.TYPE_PLAIN_JPEG,
                SupportFileTypes.TYPE_PLAIN_PNG,
                SupportFileTypes.TYPE_PLAIN_GIF);
    }

    public static boolean containsSeparator(String fileName) {
        return fileName.contains(SystemProperties.getFileSeparator());
    }

}
