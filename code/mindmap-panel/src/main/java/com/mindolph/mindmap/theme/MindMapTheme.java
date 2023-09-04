package com.mindolph.mindmap.theme;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author allen
 */
public abstract class MindMapTheme {

    protected int textMargins;
    protected int otherLevelVerticalInset;
    protected int otherLevelHorizontalInset;
    protected int firstLevelVerticalInset;
    protected int firstLevelHorizontalInset;
    protected int paperMargins;
    protected int selectLineGap;
    protected int horizontalBlockGap;
    protected boolean drawBackground;
    protected Color paperColor;
    protected Color gridColor;
    protected boolean showGrid;
    protected int gridStep;
    protected Color rootBackgroundColor;
    protected Color firstLevelBackgroundColor;
    protected Color otherLevelBackgroundColor;
    protected Color rootTextColor;
    protected Color firstLevelTextColor;
    protected Color otherLevelTextColor;
    protected Color elementBorderColor;
    protected Color connectorColor;
    protected Color shadowColor;
    protected Color collapsatorBorderColor;
    protected Color collapsatorBackgroundColor;
    protected Color selectLineColor;
    protected Color jumpLinkColor;
    protected float shadowOffset;
    protected float elementBorderWidth;
    protected boolean showCollapsatorOnMouseHover;
    protected float collapsatorBorderWidth;
    protected float collapsatorSize;
    protected float connectorWidth;
    protected float selectLineWidth;
    protected float jumpLinkWidth;
    protected float roundRadius;
    protected Font topicFont;
    protected Font noteFont;
    protected boolean dropShadow;

    public MindMapTheme() {
    }

    public void loadFromPreferences(){
        // do nothing
    }

    public void saveToPreferences(){
        // do nothing
    }


    public int getTextMargins() {
        return textMargins;
    }

    public void setTextMargins(int textMargins) {
        this.textMargins = textMargins;
    }

    public int getOtherLevelVerticalInset() {
        return otherLevelVerticalInset;
    }

    public void setOtherLevelVerticalInset(int otherLevelVerticalInset) {
        this.otherLevelVerticalInset = otherLevelVerticalInset;
    }

    public int getOtherLevelHorizontalInset() {
        return otherLevelHorizontalInset;
    }

    public void setOtherLevelHorizontalInset(int otherLevelHorizontalInset) {
        this.otherLevelHorizontalInset = otherLevelHorizontalInset;
    }

    public int getFirstLevelVerticalInset() {
        return firstLevelVerticalInset;
    }

    public void setFirstLevelVerticalInset(int firstLevelVerticalInset) {
        this.firstLevelVerticalInset = firstLevelVerticalInset;
    }

    public int getFirstLevelHorizontalInset() {
        return firstLevelHorizontalInset;
    }

    public void setFirstLevelHorizontalInset(int firstLevelHorizontalInset) {
        this.firstLevelHorizontalInset = firstLevelHorizontalInset;
    }

    public int getPaperMargins() {
        return paperMargins;
    }

    public void setPaperMargins(int paperMargins) {
        this.paperMargins = paperMargins;
    }

    public int getSelectLineGap() {
        return selectLineGap;
    }

    public void setSelectLineGap(int selectLineGap) {
        this.selectLineGap = selectLineGap;
    }

    public int getHorizontalBlockGap() {
        return horizontalBlockGap;
    }

    public void setHorizontalBlockGap(int horizontalBlockGap) {
        this.horizontalBlockGap = horizontalBlockGap;
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }

    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }

    public Color getPaperColor() {
        return paperColor;
    }

    public void setPaperColor(Color paperColor) {
        this.paperColor = paperColor;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public int getGridStep() {
        return gridStep;
    }

    public void setGridStep(int gridStep) {
        this.gridStep = gridStep;
    }

    public Color getRootBackgroundColor() {
        return rootBackgroundColor;
    }

    public void setRootBackgroundColor(Color rootBackgroundColor) {
        this.rootBackgroundColor = rootBackgroundColor;
    }

    public Color getFirstLevelBackgroundColor() {
        return firstLevelBackgroundColor;
    }

    public void setFirstLevelBackgroundColor(Color firstLevelBackgroundColor) {
        this.firstLevelBackgroundColor = firstLevelBackgroundColor;
    }

    public Color getOtherLevelBackgroundColor() {
        return otherLevelBackgroundColor;
    }

    public void setOtherLevelBackgroundColor(Color otherLevelBackgroundColor) {
        this.otherLevelBackgroundColor = otherLevelBackgroundColor;
    }

    public Color getRootTextColor() {
        return rootTextColor;
    }

    public void setRootTextColor(Color rootTextColor) {
        this.rootTextColor = rootTextColor;
    }

    public Color getFirstLevelTextColor() {
        return firstLevelTextColor;
    }

    public void setFirstLevelTextColor(Color firstLevelTextColor) {
        this.firstLevelTextColor = firstLevelTextColor;
    }

    public Color getOtherLevelTextColor() {
        return otherLevelTextColor;
    }

    public void setOtherLevelTextColor(Color otherLevelTextColor) {
        this.otherLevelTextColor = otherLevelTextColor;
    }

    public Color getElementBorderColor() {
        return elementBorderColor;
    }

    public void setElementBorderColor(Color elementBorderColor) {
        this.elementBorderColor = elementBorderColor;
    }

    public Color getConnectorColor() {
        return connectorColor;
    }

    public void setConnectorColor(Color connectorColor) {
        this.connectorColor = connectorColor;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    public Color getCollapsatorBorderColor() {
        return collapsatorBorderColor;
    }

    public void setCollapsatorBorderColor(Color collapsatorBorderColor) {
        this.collapsatorBorderColor = collapsatorBorderColor;
    }

    public Color getCollapsatorBackgroundColor() {
        return collapsatorBackgroundColor;
    }

    public void setCollapsatorBackgroundColor(Color collapsatorBackgroundColor) {
        this.collapsatorBackgroundColor = collapsatorBackgroundColor;
    }

    public Color getSelectLineColor() {
        return selectLineColor;
    }

    public void setSelectLineColor(Color selectLineColor) {
        this.selectLineColor = selectLineColor;
    }

    public Color getJumpLinkColor() {
        return jumpLinkColor;
    }

    public void setJumpLinkColor(Color jumpLinkColor) {
        this.jumpLinkColor = jumpLinkColor;
    }

    public float getShadowOffset() {
        return shadowOffset;
    }

    public void setShadowOffset(float shadowOffset) {
        this.shadowOffset = shadowOffset;
    }

    public float getElementBorderWidth() {
        return elementBorderWidth;
    }

    public void setElementBorderWidth(float elementBorderWidth) {
        this.elementBorderWidth = elementBorderWidth;
    }

    public boolean isShowCollapsatorOnMouseHover() {
        return showCollapsatorOnMouseHover;
    }

    public void setShowCollapsatorOnMouseHover(boolean showCollapsatorOnMouseHover) {
        this.showCollapsatorOnMouseHover = showCollapsatorOnMouseHover;
    }

    public float getCollapsatorBorderWidth() {
        return collapsatorBorderWidth;
    }

    public void setCollapsatorBorderWidth(float collapsatorBorderWidth) {
        this.collapsatorBorderWidth = collapsatorBorderWidth;
    }

    public float getCollapsatorSize() {
        return collapsatorSize;
    }

    public void setCollapsatorSize(float collapsatorSize) {
        this.collapsatorSize = collapsatorSize;
    }

    public float getConnectorWidth() {
        return connectorWidth;
    }

    public void setConnectorWidth(float connectorWidth) {
        this.connectorWidth = connectorWidth;
    }

    public float getSelectLineWidth() {
        return selectLineWidth;
    }

    public void setSelectLineWidth(float selectLineWidth) {
        this.selectLineWidth = selectLineWidth;
    }

    public float getJumpLinkWidth() {
        return jumpLinkWidth;
    }

    public void setJumpLinkWidth(float jumpLinkWidth) {
        this.jumpLinkWidth = jumpLinkWidth;
    }

    public float getRoundRadius() {
        return roundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        this.roundRadius = roundRadius;
    }

    public Font getTopicFont() {
        return topicFont;
    }

    public void setTopicFont(Font topicFont) {
        this.topicFont = topicFont;
    }

    public Font getNoteFont() {
        return noteFont;
    }

    public void setNoteFont(Font noteFont) {
        this.noteFont = noteFont;
    }

    public boolean isDropShadow() {
        return dropShadow;
    }

    public void setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
    }

}
