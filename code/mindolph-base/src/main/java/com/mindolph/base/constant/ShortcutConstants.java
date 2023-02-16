package com.mindolph.base.constant;

import com.mindolph.base.shortcut.ShortcutKey;

/**
 * @author mindolph.com@gmail.com
 */
public interface ShortcutConstants {

    // general
    ShortcutKey KEY_SHOW_POPUP = ShortcutKey.newShortcutKey("global.showPopupMenu", "show popup");

    // zoom
    ShortcutKey KEY_ZOOM_IN = ShortcutKey.newShortcutKey("global.zoomIn", "zoom in");
    ShortcutKey KEY_ZOOM_OUT = ShortcutKey.newShortcutKey("global.zoomOut", "zoom out");
    ShortcutKey KEY_ZOOM_RESET = ShortcutKey.newShortcutKey("global.zoomReset", "reset zoom");

    // editor
    ShortcutKey KEY_EDITOR_QUOTE = ShortcutKey.newShortcutKey("global.editor.quote", "quote selected");
    ShortcutKey KEY_EDITOR_DOUBLE_QUOTE = ShortcutKey.newShortcutKey("global.editor.doubleQuote", "double quote selected");
    ShortcutKey KEY_EDITOR_BACK_QUOTE = ShortcutKey.newShortcutKey("global.editor.backQuote", "back quote selected");
    ShortcutKey KEY_EDITOR_DELETE_LINE = ShortcutKey.newShortcutKey("global.editor.deleteLine", "delete line");
    ShortcutKey KEY_EDITOR_MOVE_LINE_UP = ShortcutKey.newShortcutKey("global.editor.moveLineUp", "move line up");
    ShortcutKey KEY_EDITOR_MOVE_LINE_DOWN = ShortcutKey.newShortcutKey("global.editor.moveLineDown", "move line down");


    // modifier for dragging view
    ShortcutKey KEY_MODIFIER_DRAGGING = ShortcutKey.newShortcutKey("global.dragging", "dragging modifier");
}
