package com.mindolph.mindmap.constant;

import com.mindolph.base.util.ColorUtils;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;

/**
 * @author mindolph.com@gmail.com
 */
public interface MindMapConstants {

    String CFG_PREFIX = "mmd";

    int TOPIC_GAP = 30;
    int MIN_DISTANCE_FOR_DRAGGING_START = 8;

    Color COLOR_MOUSE_DRAG_SELECTION = ColorUtils.makeTransparentColor(Color.DARKGRAY, 0.4); // 0x80000000
    int DRAG_POSITION_UNKNOWN = -1;
    int DRAG_POSITION_LEFT = 1;
    int DRAG_POSITION_TOP = 2;
    int DRAG_POSITION_BOTTOM = 3;
    int DRAG_POSITION_RIGHT = 4;

    String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem";
    String FILELINK_ATTR_LINE = "line";
    /**
     * Session key to keep last selected folder for added file into mind map node.
     * Object is File.
     */
    String SESSIONKEY_ADD_FILE_LAST_FOLDER = "file.add.last.folder";
    /**
     * Session key to keep last selected flag to open in system viewer for added
     * file into mind map node. Object is Boolean
     */
    String SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM = "file.add.open_in_system";

    /**
     *
     */
    DataFormat MMD_DATA_FORMAT  = new DataFormat("application/mindolph-topic-list");
    String MODEL_ATTR_SHOW_JUMPS = "showJumps";

    enum VisualStrategy{
        CENTER, FIT
    }
}
