package com.mindolph.mindmap;

import com.mindolph.base.ShortcutManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.apache.commons.lang3.SystemUtils;

import static com.mindolph.mindmap.constant.ShortcutConstants.*;
import static javafx.scene.input.KeyCombination.*;

/**
 * @author mindolph.com@gmail.com
 */
public class Shortcuts {

    public static void init() {
        // mmd
        ShortcutManager sm = ShortcutManager.getIns();
        sm.addShortCut(KEY_MMD_ADD_CHILD_AND_START_EDIT, new KeyCodeCombination(KeyCode.TAB));
        sm.addShortCut(KEY_MMD_ADD_SIBLING_AND_START_EDIT, new KeyCodeCombination(KeyCode.ENTER));
        sm.addShortCut(KEY_MMD_ADD_PREV_SIBLING_AND_START_EDIT, new KeyCodeCombination(KeyCode.ENTER, SHIFT_DOWN));
        if (SystemUtils.IS_OS_MAC) {
            sm.addShortCut(KEY_MMD_FOCUS_ROOT_OR_START_EDIT, new KeyCodeCombination(KeyCode.SPACE, ALT_DOWN));
        }
        else if (SystemUtils.IS_OS_LINUX) {
            sm.addShortCut(KEY_MMD_FOCUS_ROOT_OR_START_EDIT, new KeyCodeCombination(KeyCode.SPACE, SHIFT_DOWN));
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            sm.addShortCut(KEY_MMD_FOCUS_ROOT_OR_START_EDIT, new KeyCodeCombination(KeyCode.SPACE, ALT_DOWN));
        }
        sm.addShortCut(KEY_MMD_TOPIC_FOLD, new KeyCodeCombination(KeyCode.MINUS));
        sm.addShortCut(KEY_MMD_TOPIC_UNFOLD, new KeyCodeCombination(KeyCode.EQUALS));
        sm.addShortCut(KEY_MMD_TOPIC_FOLD_ALL, new KeyCodeCombination(KeyCode.MINUS, ALT_DOWN));
        sm.addShortCut(KEY_MMD_TOPIC_UNFOLD_ALL, new KeyCodeCombination(KeyCode.EQUALS, ALT_DOWN));
        sm.addShortCut(KEY_MMD_DELETE_TOPIC, new KeyCodeCombination(KeyCode.DELETE));
        sm.addShortCut(KEY_FOCUS_MOVE_UP_ADD_FOCUSED, new KeyCodeCombination(KeyCode.UP, SHIFT_DOWN));
        sm.addShortCut(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED, new KeyCodeCombination(KeyCode.DOWN, SHIFT_DOWN));
        sm.addShortCut(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, new KeyCodeCombination(KeyCode.LEFT, SHIFT_DOWN));
        sm.addShortCut(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED, new KeyCodeCombination(KeyCode.RIGHT, SHIFT_DOWN));
        if (SystemUtils.IS_OS_MAC) {
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_UP, new KeyCodeCombination(KeyCode.UP, META_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_DOWN, new KeyCodeCombination(KeyCode.DOWN, META_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_LEFT, new KeyCodeCombination(KeyCode.LEFT, META_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_RIGHT, new KeyCodeCombination(KeyCode.RIGHT, META_DOWN));
        }
        else if (SystemUtils.IS_OS_LINUX) {
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_UP, new KeyCodeCombination(KeyCode.UP, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_DOWN, new KeyCodeCombination(KeyCode.DOWN, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_LEFT, new KeyCodeCombination(KeyCode.LEFT, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_RIGHT, new KeyCodeCombination(KeyCode.RIGHT, ALT_DOWN));
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_UP, new KeyCodeCombination(KeyCode.UP, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_DOWN, new KeyCodeCombination(KeyCode.DOWN, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_LEFT, new KeyCodeCombination(KeyCode.LEFT, ALT_DOWN));
            sm.addShortCut(KEY_MMD_TOPIC_MOVE_RIGHT, new KeyCodeCombination(KeyCode.RIGHT, ALT_DOWN));
        }
    }
}
