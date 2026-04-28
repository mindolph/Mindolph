package com.mindolph.base.dialog;

import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.swiftboot.util.I18nHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public interface DialogFileFilters {
    ExtensionFilter MMD_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.mmd"), "*.mmd");
    ExtensionFilter MARKDOWN_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.markdown"), "*.md");
    ExtensionFilter PUML_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.puml"), "*.puml");
    ExtensionFilter TXT_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.txt"), "*.txt");
    ExtensionFilter IMAGE_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.image.with.pattern"), "*.jpg", "*.jpeg" , "*.png", "*.gif");
    ExtensionFilter IMAGE_JPEG_EXTENSION_FILTER =  new ExtensionFilter(I18nHelper.getInstance().get("file.filter.jpeg"), "*.jpg");

    List<ExtensionFilter> EXTENSION_FILTER_LIST = Arrays.asList(
            MMD_EXTENSION_FILTER,
            MARKDOWN_EXTENSION_FILTER,
            PUML_EXTENSION_FILTER,
            TXT_EXTENSION_FILTER
    );

    /**
     * Get filter by file extension.
     * @param file
     * @return
     */
    static ExtensionFilter getExtensionFilter(File file) {
        return getExtensionFilter(FilenameUtils.getExtension(file.getPath()));
    }

    /**
     * Get filter by file extension.
     *
     * @param extension
     * @return
     */
    static ExtensionFilter getExtensionFilter(String extension) {
        for (ExtensionFilter extensionFilter : EXTENSION_FILTER_LIST) {
            if (extensionFilter.getExtensions().contains(extension)){
                return extensionFilter;
            }
        }
        return null;
    }
}
