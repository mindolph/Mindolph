package com.mindolph.mindmap.theme;

import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.util.ColorUtils;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * @author mindolph
 */
public class DarkTheme extends MindMapTheme {

    public DarkTheme() {
        textMargins = 10;
        otherLevelVerticalInset = 16;
        otherLevelHorizontalInset = 32;
        firstLevelVerticalInset = 32;
        firstLevelHorizontalInset = 48;
        paperMargins = 20;
        selectLineGap = 5;
        horizontalBlockGap = 5;
        drawBackground = true;
        paperColor = Color.web("0x262F34CC");
        gridColor = paperColor.darker();
        showGrid = false;
        gridStep = 0;
        rootBackgroundColor = Color.web("0xEEEEEE");
        firstLevelBackgroundColor = Color.web("0xB1BFCC");
        otherLevelBackgroundColor = Color.web("0xFFFFFF00");
        rootTextColor = Color.BLACK;
        firstLevelTextColor = Color.BLACK;
        otherLevelTextColor = Color.WHITE;

        connectorStyle = ConnectorStyle.BEZIER;
        connectorWidth = 2.0f;
        connectorColor = Color.LIGHTGRAY;

        collapsatorBorderColor = Color.LIGHTGRAY;
        collapsatorBackgroundColor = Color.DARKGRAY;
        selectLineColor = Color.LIGHTGRAY;
        jumpLinkColor = Color.YELLOWGREEN;

        showCollapsatorOnMouseHover = true;
        collapsatorBorderWidth = 1.0f;
        collapsatorSize = 16;

        selectLineWidth = 2.5f;
        jumpLinkWidth = 2f;
        roundRadius = 8f;
        topicFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_TOPIC_FONT);
        noteFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_NOTE_FONT);

        dropShadow = false;
        shadowColor = ColorUtils.makeTransparentColor(Color.DARKGRAY, 0.188);
        shadowOffset = 5.0f;

        borderType = BorderType.LINE;
        firstLevelBorderType = BorderType.BOX;
        elementBorderWidth = 0f;
        elementBorderColor = paperColor;

        disabledSettings = new ArrayList<>() {
            {
                add("spnBorderWidth"); // elementBorderWidth
                add("cpTopicBorderColor"); // elementBorderColor
                add("ckbDropShadow");
            }
        };
    }
}
