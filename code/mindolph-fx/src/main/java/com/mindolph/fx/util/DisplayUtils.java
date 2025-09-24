package com.mindolph.fx.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.File;

/**
 * @author mindolph.com@gmail.com
 */
public class DisplayUtils {

    /**
     * Display a file with workspace.
     *
     * @param workspaceDir
     * @param file
     * @return
     */
    public static String displayFileWithWorkspace(File workspaceDir, File file) {
        String workspaceName = FilenameUtils.getName(workspaceDir.getPath());
        return "%s(%s)".formatted(StringUtils.substringAfter(file.getPath(), workspaceDir.getPath()), workspaceName);
    }

    /**
     * Display a file in workspace.
     *
     * @param workspaceDir
     * @param file
     * @return
     */
    public static String displayFile(File workspaceDir, File file) {
        return StringUtils.substringAfter(file.getPath(), Strings.CS.appendIfMissing(workspaceDir.getPath(), "/"));
    }
}
