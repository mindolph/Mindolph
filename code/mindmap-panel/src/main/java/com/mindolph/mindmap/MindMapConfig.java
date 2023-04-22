package com.mindolph.mindmap;

import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.core.config.EditorConfig;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Config object for Mind Map.
 */
public final class MindMapConfig implements EditorConfig, Serializable {

    private final Logger log = LoggerFactory.getLogger(MindMapConfig.class);

    private final String CFG_PREFIX = "mmd";

    //    private transient final Map<String, KeyShortcut> mapShortCut = new HashMap<>();
    private int textMargins = 10;
    private int otherLevelVerticalInset = 16;
    private int otherLevelHorizontalInset = 32;
    private int firstLevelVerticalInset = 32;
    private int firstLevelHorizontalInset = 48;
    private int paperMargins = 20;
    private int selectLineGap = 5;
    private int horizontalBlockGap = 5;
    private boolean drawBackground = true;
    private Color paperColor = Color.web("0x617B94");
    private Color gridColor = paperColor.darker();
    private boolean showGrid = true;
    private int gridStep = 32;
    private Color rootBackgroundColor = Color.web("0x031A31");
    private Color firstLevelBackgroundColor = Color.web("0xB1BFCC");
    private Color otherLevelBackgroundColor = Color.web("0xFDFDFD");
    private Color rootTextColor = Color.WHITE;
    private Color firstLevelTextColor = Color.BLACK;
    private Color otherLevelTextColor = Color.BLACK;
    private Color elementBorderColor = Color.BLACK;
    private Color connectorColor = Color.WHITE;
    private Color shadowColor = ColorUtils.makeTransparentColor(Color.BLACK, 0.188);
    private Color collapsatorBorderColor = Color.DARKGRAY;
    private Color collapsatorBackgroundColor = Color.WHITE;
    private Color selectLineColor = Color.ORANGE;
    private Color jumpLinkColor = Color.CYAN;
    private float shadowOffset = 5.0f;
    private float elementBorderWidth = 1.0f;
    private boolean showCollapsatorOnMouseHover = true;
    private float collapsatorBorderWidth = 1.0f;
    private float collapsatorSize = 16;
    private float connectorWidth = 1.5f;
    private float selectLineWidth = 3.0f;
    private float jumpLinkWidth = 1.5f;
    private float roundRadius = 8f;
    private boolean trimTopicText = true;
    private boolean unfoldCollapsedTarget = false;
    private boolean copyColorInfoToNewChild = false;
    private boolean smartTextPaste = false;
    private Font topicFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_TOPIC_FONT);
    private Font noteFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_NOTE_FONT);
    private boolean dropShadow = true;
    private int maxRedoUndo = 20;

    public MindMapConfig(MindMapConfig cfg) {
        this();
        this.makeFullCopyOf(cfg);
    }

    public MindMapConfig() {

    }

    public void saveToPreferences() {
        for (Field f : MindMapConfig.class.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            String key = CFG_PREFIX + '.' + f.getName();

            try {
                FxPreferences.getInstance().savePreference(key, f.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        FxPreferences.getInstance().flush();
    }

    @Override
    public void loadFromPreferences() {
        log.debug("Load from preferences");
        MindMapConfig cfg = new MindMapConfig();
        for (Field f : MindMapConfig.class.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            String key = CFG_PREFIX + '.' + f.getName();
            try {
                Object val = FxPreferences.getInstance().getPreference(key, f, cfg);
                if (val == null) {
                    continue;
                }
                f.set(this, val);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void makeFullCopyOf(MindMapConfig src) {
        if (src != null) {
            for (Field f : MindMapConfig.class.getDeclaredFields()) {
                if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                    try {
                        f.set(this, f.get(src));
                    } catch (Exception ex) {
                        throw new Error("Unexpected state during cloning field " + f, ex);
                    }
                }
            }
        }
    }

    public int getHorizontalBlockGap() {
        return this.horizontalBlockGap;
    }

    public void setHorizontalBlockGap(int gap) {
        this.horizontalBlockGap = gap;
    }

    public float getSelectLineWidth() {
        return this.selectLineWidth;
    }

    public void setSelectLineWidth(float f) {
        this.selectLineWidth = f;
    }

    public float getJumpLinkWidth() {
        return this.jumpLinkWidth;
    }

    public void setJumpLinkWidth(float f) {
        this.jumpLinkWidth = f;
    }

    public float getRoundRadius() {
        return roundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        this.roundRadius = roundRadius;
    }

    public Color getJumpLinkColor() {
        return this.jumpLinkColor;
    }

    public void setJumpLinkColor(Color color) {
        this.jumpLinkColor = color;
    }

    public Color getSelectLineColor() {
        return this.selectLineColor;
    }

    public void setSelectLineColor(Color color) {
        this.selectLineColor = color;
    }

    public int getPaperMargins() {
        return this.paperMargins;
    }

    public void setPaperMargins(int size) {
        this.paperMargins = size;
    }

    public boolean isSmartTextPaste() {
        return this.smartTextPaste;
    }

    public void setSmartTextPaste(boolean flag) {
        this.smartTextPaste = flag;
    }

    public boolean isDrawBackground() {
        return this.drawBackground;
    }

    public void setDrawBackground(boolean flag) {
        this.drawBackground = flag;
    }

    public int getOtherLevelVerticalInset() {
        return this.otherLevelVerticalInset;
    }

    public void setOtherLevelVerticalInset(int value) {
        this.otherLevelVerticalInset = value;
    }

    public int getOtherLevelHorizontalInset() {
        return this.otherLevelHorizontalInset;
    }

    public void setOtherLevelHorizontalInset(int value) {
        this.otherLevelHorizontalInset = value;
    }

    public int getFirstLevelVerticalInset() {
        return this.firstLevelVerticalInset;
    }

    public void setFirstLevelVerticalInset(int value) {
        this.firstLevelVerticalInset = value;
    }

    public int getFirstLevelHorizontalInset() {
        return this.firstLevelHorizontalInset;
    }

    public void setFirstLevelHorizontalInset(int value) {
        this.firstLevelHorizontalInset = value;
    }


    public Color getPaperColor() {
        return this.paperColor;
    }

    public void setPaperColor(Color color) {
        this.paperColor = color;
    }


    public Color getGridColor() {
        return this.gridColor;
    }

    public void setGridColor(Color color) {
        this.gridColor = color;
    }

    public boolean isShowGrid() {
        return this.showGrid;
    }

    public void setShowGrid(boolean flag) {
        this.showGrid = flag;
    }

    public int getGridStep() {
        return this.gridStep;
    }

    public void setGridStep(int step) {
        this.gridStep = step;
    }


    public Color getRootBackgroundColor() {
        return this.rootBackgroundColor;
    }

    public void setRootBackgroundColor(Color color) {
        this.rootBackgroundColor = color;
    }


    public Color getFirstLevelBackgroundColor() {
        return this.firstLevelBackgroundColor;
    }

    public void setFirstLevelBackgroundColor(Color color) {
        this.firstLevelBackgroundColor = color;
    }


    public Color getOtherLevelBackgroundColor() {
        return this.otherLevelBackgroundColor;
    }

    public void setOtherLevelBackgroundColor(Color color) {
        this.otherLevelBackgroundColor = color;
    }


    public Color getRootTextColor() {
        return this.rootTextColor;
    }

    public void setRootTextColor(Color color) {
        this.rootTextColor = color;
    }


    public Color getFirstLevelTextColor() {
        return this.firstLevelTextColor;
    }

    public void setFirstLevelTextColor(Color color) {
        this.firstLevelTextColor = color;
    }


    public Color getOtherLevelTextColor() {
        return this.otherLevelTextColor;
    }

    public void setOtherLevelTextColor(Color color) {
        this.otherLevelTextColor = color;
    }


    public Color getElementBorderColor() {
        return this.elementBorderColor;
    }

    public void setElementBorderColor(Color color) {
        this.elementBorderColor = color;
    }


    public Color getConnectorColor() {
        return this.connectorColor;
    }

    public void setConnectorColor(Color color) {
        this.connectorColor = color;
    }

    public Color getShadowColor() {
        return this.shadowColor;
    }

    public void setShadowColor(Color color) {
        this.shadowColor = color;
    }


    public Color getCollapsatorBorderColor() {
        return this.collapsatorBorderColor;
    }

    public void setCollapsatorBorderColor(Color color) {
        this.collapsatorBorderColor = color;
    }


    public Color getCollapsatorBackgroundColor() {
        return this.collapsatorBackgroundColor;
    }

    public void setCollapsatorBackgroundColor(Color color) {
        this.collapsatorBackgroundColor = color;
    }

    public float getElementBorderWidth() {
        return this.elementBorderWidth;
    }

    public void setElementBorderWidth(float value) {
        this.elementBorderWidth = value;
    }

    public float getCollapsatorBorderWidth() {
        return this.collapsatorBorderWidth;
    }

    public void setCollapsatorBorderWidth(float width) {
        this.collapsatorBorderWidth = width;
    }

    public float getShadowOffset() {
        return this.shadowOffset;
    }

    public void setShadowOffset(float value) {
        this.shadowOffset = value;
    }

    public float getConnectorWidth() {
        return this.connectorWidth;
    }

    public void setConnectorWidth(float value) {
        this.connectorWidth = value;
    }


    public Font getTopicFont() {
        return this.topicFont;
    }

    public void setTopicFont(Font f) {
        this.topicFont = f;
    }

    public Font getNoteFont() {
        return noteFont;
    }

    public void setNoteFont(Font noteFont) {
        this.noteFont = noteFont;
    }

    public boolean isDropShadow() {
        return this.dropShadow;
    }

    public void setDropShadow(boolean value) {
        this.dropShadow = value;
    }

    public boolean isShowCollapsatorOnMouseHover() {
        return showCollapsatorOnMouseHover;
    }

    public void setShowCollapsatorOnMouseHover(boolean showCollapsatorOnMouseHover) {
        this.showCollapsatorOnMouseHover = showCollapsatorOnMouseHover;
    }

    public float getCollapsatorSize() {
        return this.collapsatorSize;
    }

    public void setCollapsatorSize(float size) {
        this.collapsatorSize = size;
    }

    public int getTextMargins() {
        return this.textMargins;
    }

    public void setTextMargins(int value) {
        this.textMargins = value;
    }

    public int getSelectLineGap() {
        return this.selectLineGap;
    }

    public void setSelectLineGap(int value) {
        this.selectLineGap = value;
    }

    public boolean isTrimTopicText() {
        return trimTopicText;
    }

    public void setTrimTopicText(boolean trimTopicText) {
        this.trimTopicText = trimTopicText;
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
