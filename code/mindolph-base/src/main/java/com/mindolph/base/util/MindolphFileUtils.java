package com.mindolph.base.util;

import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.DesktopUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class MindolphFileUtils {

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
