package com.mindolph.base.util;

import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.DesktopUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class MindolphFileUtils {

    /**
     * Ignore the .DS_Store file from macOS.
     *
     * @param folder
     * @return
     * @throws IOException
     */
    public static boolean isFolderEmpty(File folder) throws IOException {
        if (folder == null || !folder.exists()) {
            return true;
        }
        if (FileUtils.isEmptyDirectory(folder)) {
            return true;
        }
        else {
            String[] list = folder.list((dir, name) -> ".DS_Store".equals(name));
            return list == null || list.length == 1;
        }
    }

    /**
     * Delete the .DS_Store file from macOS in folder.
     *
     * @param folder
     */
    public static void deleteMacFile(File folder) {
        // do not check current OS because the mac .DS_Store file might be copied to other OS.
        File[] files = folder.listFiles((dir, name) -> ".DS_Store".equals(name));
        if (files != null) {
            for (File file : files) {
                if (file.isHidden()) {
                    file.delete();
                }
            }
        }
    }

    public static File getTempDir() {
        return new File(SystemUtils.getUserHome(), "/Temp/mindolph");
    }

    public static File getTempFile(String fileName) {
        File f = new File(MindolphFileUtils.getTempDir(), fileName);
        if (!f.getParentFile().exists()) {
            if (f.mkdirs()) {
                return f;
            }
            else {
                return null;
            }
        }
        return f;
    }

    /**
     * TODO to be tested
     *
     * @param filePath
     * @return
     */
    public static boolean isOpenInternally(String filePath) {
        for (String suffix : Arrays.asList(SupportFileTypes.TYPE_MIND_MAP, SupportFileTypes.TYPE_PLANTUML, SupportFileTypes.TYPE_MARKDOWN)) {
            if (filePath.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static void openFileInSystem(File file) {
        try {
            DesktopUtils.openInSystem(file, false);
        } catch (Exception e) {
            DialogFactory.warnDialog("Can't open this file in system");
        }
    }
}
