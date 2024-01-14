package com.mindolph.mindmap.theme;

import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.util.ColorUtils;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * @author mindolph
 */
public class ClassicTheme extends MindMapTheme {

    public ClassicTheme() {
        textMargins = 10;
        otherLevelVerticalInset = 16;
        otherLevelHorizontalInset = 32;
        firstLevelVerticalInset = 32;
        firstLevelHorizontalInset = 48;
        paperMargins = 200;
        selectLineGap = 5;
        horizontalBlockGap = 5;
        drawBackground = true;
        paperColor = Color.web("0x617B94");
        gridColor = paperColor.darker();
        showGrid = true;
        gridStep = 50;
        rootBackgroundColor = Color.web("0x031A31");
        firstLevelBackgroundColor = Color.web("0xB1BFCC");
        otherLevelBackgroundColor = Color.web("0xFDFDFD");
        rootTextColor = Color.WHITE;
        firstLevelTextColor = Color.BLACK;
        otherLevelTextColor = Color.BLACK;

        connectorStyle = ConnectorStyle.POLYLINE;
        connectorColor = Color.WHITE;
        connectorWidth = 1.5f;

        collapsatorBorderColor = Color.DARKGRAY;
        collapsatorBackgroundColor = Color.WHITE;
        selectLineColor = Color.ORANGE;
        jumpLinkColor = Color.CYAN;

        showCollapsatorOnMouseHover = true;
        collapsatorBorderWidth = 1.0f;
        collapsatorSize = 16;

        selectLineWidth = 3.0f;
        jumpLinkWidth = 1.5f;
        roundRadius = 8f;
//        topicFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_TOPIC_FONT);
//        noteFont = FontConstants.DEFAULT_FONTS.get(FontConstants.KEY_MMD_NOTE_FONT);

        dropShadow = true;
        shadowColor = ColorUtils.makeTransparentColor(Color.BLACK, 0.188);
        shadowOffset = 5.0f;

        borderType = BorderType.BOX;
        firstLevelBorderType = BorderType.BOX;
        elementBorderWidth = 1.0f;
        elementBorderColor = Color.BLACK;
        disabledSettings = new ArrayList<>();
    }

}
