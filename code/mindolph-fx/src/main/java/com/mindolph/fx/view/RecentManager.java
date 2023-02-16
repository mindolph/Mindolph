package com.mindolph.fx.view;

import com.mindolph.core.constant.SceneStatePrefs;
import com.mindolph.mfx.preference.FxPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage the recent opened file list which is stored in system preferences provider.
 *
 * @author mindolph.com@gmail.com
 */
public class RecentManager {

    private static final RecentManager ins = new RecentManager();

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    // max size to store recent files, TODO to be configurable
    public static final int MAX_SIZE = 25;

    public static RecentManager getInstance() {
        return ins;
    }

    private RecentManager() {
    }

    public List<File> loadRecent() {
        List<String> openedFiles = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_RECENT_FILE_LIST, new ArrayList<>());
        return openedFiles.stream().map(File::new).collect(Collectors.toList());
    }

    /**
     * Add to recent history, if exists, bring it to latest.
     * Only save MAX_SIZE files to history.
     *
     * @param file
     */
    public void addToRecent(File file) {
        List<String> recentFiles = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_RECENT_FILE_LIST, new ArrayList<>());
        recentFiles.remove(file.getPath());
        recentFiles.add(0, file.getPath());
        List<String> toBeSaved = recentFiles.size() > MAX_SIZE ? recentFiles.subList(0, MAX_SIZE) : recentFiles;
        fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_RECENT_FILE_LIST, toBeSaved);
    }

    public void removeFromRecent(File file) {
        List<String> recentFiles = fxPreferences.getPreference(SceneStatePrefs.MINDOLPH_RECENT_FILE_LIST, new ArrayList<>());
        recentFiles.remove(file.getPath());
        fxPreferences.savePreference(SceneStatePrefs.MINDOLPH_RECENT_FILE_LIST, recentFiles);
    }
}
