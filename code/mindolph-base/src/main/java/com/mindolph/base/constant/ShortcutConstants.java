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
    // THIS IS USED TO DISABLE PASTE SHORTCUT IN EDITOR TO AVOID CONFLICT WITH GLOBAL.
    ShortcutKey KEY_EDITOR_PASTE = ShortcutKey.newShortcutKey("global.editor.paste", "paste");


    // modifier for dragging view
    ShortcutKey KEY_MODIFIER_DRAGGING = ShortcutKey.newShortcutKey("global.dragging", "dragging modifier");

    // internal shortcuts like undo/redo, copy/paste...
    ShortcutKey KEY_UNDO = ShortcutKey.newShortcutKey("global.undo", "undo");
    ShortcutKey KEY_REDO = ShortcutKey.newShortcutKey("global.redo", "redo");

    // markdown
    ShortcutKey KEY_MD_COMMENT = new ShortcutKey("md.comment", "comment line", "Markdown");

}
