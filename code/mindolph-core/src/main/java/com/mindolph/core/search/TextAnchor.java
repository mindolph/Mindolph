package com.mindolph.core.search;

/**
 *
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class TextAnchor implements Anchor {

//    /**
//     * Level in hierarchy, starts from 1.
//     */
//    private int level;
//
//    /**
//     * Index in same level siblings
//     */
//    private int levelIndex;

    private TextLocation textLocation;

    public TextAnchor(TextLocation textLocation) {
        this.textLocation = textLocation;
    }

//    public int getLevel() {
//        return level;
//    }
//
//    public void setLevel(int level) {
//        this.level = level;
//    }
//
//    public int getLevelIndex() {
//        return levelIndex;
//    }
//
//    public void setLevelIndex(int levelIndex) {
//        this.levelIndex = levelIndex;
//    }

    public TextLocation getTextLocation() {
        return textLocation;
    }

    public void setTextLocation(TextLocation textLocation) {
        this.textLocation = textLocation;
    }

    @Override
    public String toString() {
        return "TextAnchor{" +
                "textLocation=" + textLocation +
                '}';
    }
}
