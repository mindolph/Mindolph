package com.mindolph.mindmap.constant;

import com.mindolph.base.shortcut.ShortcutKey;

/**
 * @author mindolph.com@gmail.com
 */
public interface ShortcutConstants {

    // mmd
    ShortcutKey KEY_MMD_ADD_CHILD_AND_START_EDIT = new ShortcutKey("mmd.addChildAndStartEdit", "add child topic and start edit", "Mind Map");
    ShortcutKey KEY_MMD_ADD_SIBLING_AND_START_EDIT = new ShortcutKey("mmd.addSiblingAndStartEdit", "add next topic and start edit", "Mind Map");
    ShortcutKey KEY_MMD_ADD_PREV_SIBLING_AND_START_EDIT = new ShortcutKey("mmd.addPrevSiblingAndStartEdit", "add previous topic and start edit", "Mind Map");
    ShortcutKey KEY_MMD_FOCUS_ROOT_OR_START_EDIT = new ShortcutKey("mmd.focusToRootOrStartEdit", "focus root or start edit selected", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_FOLD = new ShortcutKey("mmd.topicFold", "fold selected topic", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_FOLD_ALL = new ShortcutKey("mmd.topicFoldAll", "fold all topics of selected", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_UNFOLD = new ShortcutKey("mmd.topicUnfold", "unfold selected topic", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_UNFOLD_ALL = new ShortcutKey("mmd.topicUnfoldAll", "unfold all topics of selected", "Mind Map");
    ShortcutKey KEY_MMD_DELETE_TOPIC = new ShortcutKey("mmd.deleteSelectedTopic", "delete selected topics", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_MOVE_UP = new ShortcutKey("mmd.moveTopicUp", "move topic up", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_MOVE_DOWN = new ShortcutKey("mmd.moveTopicDown", "move topic down", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_MOVE_LEFT = new ShortcutKey("mmd.moveTopicLeft", "move topic left", "Mind Map");
    ShortcutKey KEY_MMD_TOPIC_MOVE_RIGHT = new ShortcutKey("mmd.moveTopicRight", "move topic right", "Mind Map");
    ShortcutKey KEY_FOCUS_MOVE_UP_ADD_FOCUSED = new ShortcutKey("mmd.moveFocusUpAddFocused", "move up", "Mind Map");
    ShortcutKey KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED = new ShortcutKey("mmd.moveFocusDownAddFocused", "move down", "Mind Map");
    ShortcutKey KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED = new ShortcutKey("mmd.moveFocusLeftAddFocused", "move left", "Mind Map");
    ShortcutKey KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED = new ShortcutKey("mmd.moveFocusRightAddFocused", "move right", "Mind Map");
}
