package com.mindolph.mindmap.util;

import com.mindolph.mfx.dialog.FileDialogBuilder;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;

/**
 * @author mindolph.com@gmail.com
 */
public class DialogUtils {

    /**
     *
     *
     * @param title
     * @param defaultFolder
     * @param dottedFileExtension
     * @param filterDescription
     * @param defaultFileName
     * @return
     */
    public static File selectFileToSaveForFileFilter(String title, File defaultFolder,
                                                     String dottedFileExtension, String filterDescription, String defaultFileName) {
        String lcExtension = "*" + dottedFileExtension.toLowerCase(Locale.ENGLISH);
        return new FileDialogBuilder().fileDialogType(FileDialogBuilder.FileDialogType.SAVE_FILE)
                .title(title).initDir(defaultFolder)
                .initFileName(defaultFileName)
                .extensionFilters(new FileChooser.ExtensionFilter(filterDescription, lcExtension))
                .buildAndShow();
    }

    /**
     *
     *
     * @param title
     * @param defaultFolder
     * @param dottedFileExtension
     * @param filterDescription
     * @return
     */
    public static File selectFileToOpenForFileFilter(String title, File defaultFolder, String dottedFileExtension,
                                                     String filterDescription) {
        String lcExtension = "*" + dottedFileExtension.toLowerCase(Locale.ENGLISH);
        System.out.println("Select file: " + lcExtension);
        return new FileDialogBuilder().fileDialogType(FileDialogBuilder.FileDialogType.OPEN_FILE).title(title).initDir(defaultFolder)
                .extensionFilters(new FileChooser.ExtensionFilter(filterDescription, lcExtension))
                .buildAndShow();
    }
}
