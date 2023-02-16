package com.mindolph.base.dialog;

import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public interface DialogFileFilters {
    ExtensionFilter MMD_EXTENSION_FILTER =  new ExtensionFilter("Mind Map file(*.mmd)", "*.mmd");
    ExtensionFilter MARKDOWN_EXTENSION_FILTER =  new ExtensionFilter("Markdown File(*.md)", "*.md");
    ExtensionFilter PUML_EXTENSION_FILTER =  new ExtensionFilter("PlantUML file(*.puml)", "*.puml");
    ExtensionFilter TXT_EXTENSION_FILTER =  new ExtensionFilter("Text File(*.txt)", "*.txt");
    ExtensionFilter IMAGE_EXTENSION_FILTER =  new ExtensionFilter("Image File(*.jpg, *.jpeg, *.png, *.gif)", "*.jpg", "*.jpeg" , "*.png", "*.gif");
    ExtensionFilter IMAGE_JPEG_EXTENSION_FILTER =  new ExtensionFilter("JPEG File(*.jpg)", "*.jpg");

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
