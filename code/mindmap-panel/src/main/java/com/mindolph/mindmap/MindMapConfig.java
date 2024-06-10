package com.mindolph.mindmap;

import com.mindolph.base.constant.FontConstants;
import com.mindolph.core.config.EditorConfig;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.theme.*;
import com.mindolph.mindmap.util.PrefObjectUtils;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Config object for Mind Map.
 */
public final class MindMapConfig implements EditorConfig, Serializable {

    private static final Logger log = LoggerFactory.getLogger(MindMapConfig.class);

    // the current theme name, if user-defined, load from java preference.
    private String themeName = ThemeType.LIGHT.name();
    private List<String> userThemes = new ArrayList<>();// init for first usage.
    private MindMapTheme theme = new LightTheme();

    // fonts
    private Font topicFont;
    private Font noteFont;

    // options
    private boolean trimTopicText = true;
    private boolean unfoldCollapsedTarget = false;
    private boolean copyColorInfoToNewChild = false;
    private boolean smartTextPaste = false;
    private int maxRedoUndo = 20;

    public MindMapConfig(MindMapConfig cfg) {
        this();
        this.makeFullCopyOf(cfg);
    }

    public MindMapConfig() {
        topicFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_TOPIC_FONT);
        noteFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_NOTE_FONT);
    }

    @Override
    public void loadFromPreferences() {
        PrefObjectUtils.load(this, this, MindMapConfig.class, MindMapConstants.CFG_PREFIX);
        // theme type will be loaded in the previous process
        this.theme = ThemeUtils.createTheme(this.getThemeName());
        this.theme.loadFromPreferences();
    }

    public void saveToPreferences() {
        theme.saveToPreferences();
        PrefObjectUtils.save(this, MindMapConfig.class, MindMapConstants.CFG_PREFIX);
    }

    public void makeFullCopyOf(MindMapConfig src) {
        if (src != null) {
            for (Field f : MindMapConfig.class.getDeclaredFields()) {
                if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                    try {
                        f.set(this, f.get(src));
                    } catch (Exception ex) {
                        throw new Error("Failed to cloning field " + f, ex);
                    }
                }
            }
        }
    }

    public MindMapTheme getTheme() {
        return theme;
    }

    public void setTheme(MindMapTheme theme) {
        this.theme = theme;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public List<String> getUserThemes() {
        return userThemes;
    }

    public void setUserThemes(List<String> userThemes) {
        this.userThemes = userThemes;
    }

    public Font getTopicFont() {
        return topicFont;
    }

    public void setTopicFont(Font topicFont) {
        this.topicFont = topicFont;
    }

    /**
     * @deprecated
     * @return
     */
    public Font getNoteFont() {
        return noteFont;
    }

    /**
     * @deprecated
     * @param noteFont
     */
    public void setNoteFont(Font noteFont) {
        this.noteFont = noteFont;
    }

    public boolean isTrimTopicText() {
        return trimTopicText;
    }

    public void setTrimTopicText(boolean trimTopicText) {
        this.trimTopicText = trimTopicText;
    }

    public boolean isSmartTextPaste() {
        return this.smartTextPaste;
    }

    public void setSmartTextPaste(boolean flag) {
        this.smartTextPaste = flag;
    }

    public boolean isUnfoldCollapsedTarget() {
        return unfoldCollapsedTarget;
    }

    public void setUnfoldCollapsedTarget(boolean unfoldCollapsedTarget) {
        this.unfoldCollapsedTarget = unfoldCollapsedTarget;
    }

    public boolean isCopyColorInfoToNewChild() {
        return copyColorInfoToNewChild;
    }

    public void setCopyColorInfoToNewChild(boolean copyColorInfoToNewChild) {
        this.copyColorInfoToNewChild = copyColorInfoToNewChild;
    }

    public int getMaxRedoUndo() {
        return maxRedoUndo;
    }

    public void setMaxRedoUndo(int maxRedoUndo) {
        this.maxRedoUndo = maxRedoUndo;
    }
}
