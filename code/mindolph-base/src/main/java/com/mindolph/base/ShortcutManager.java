package com.mindolph.base;

import com.mindolph.base.shortcut.ShortcutKey;
import com.mindolph.core.constant.TextConstants;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.SystemUtils;
import org.swiftboot.collections.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.ShortcutConstants.*;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;

/**
 * Manage the changeable shortcuts for FX application.
 * TODO move to "shortcut/"
 *
 * @author mindolph.com@gmail.com
 */
public class ShortcutManager {

    private static final ShortcutManager ins = new ShortcutManager();

    private static final Map<ShortcutKey, KeyCodeCombination> shortcutMap = new LinkedHashMap<>();

    private static final Map<ShortcutKey, KeyCombination> modifierMap = new LinkedHashMap<>();

    private ShortcutManager() {
    }

    public static ShortcutManager getIns() {
        return ins;
    }

    static {
        // zoom
        shortcutMap.put(KEY_ZOOM_IN, new KeyCodeCombination(EQUALS, CONTROL_DOWN));
        shortcutMap.put(KEY_ZOOM_OUT, new KeyCodeCombination(MINUS, CONTROL_DOWN));
        shortcutMap.put(KEY_ZOOM_RESET, new KeyCodeCombination(DIGIT0, CONTROL_DOWN));

        // editor
        if (SystemUtils.IS_OS_MAC) {
            // mac specific
            shortcutMap.put(KEY_EDITOR_QUOTE, new KeyCodeCombination(QUOTE, META_DOWN));
            shortcutMap.put(KEY_EDITOR_DOUBLE_QUOTE, new KeyCodeCombination(QUOTE, SHIFT_DOWN, META_DOWN));
            shortcutMap.put(KEY_EDITOR_BACK_QUOTE, new KeyCodeCombination(BACK_QUOTE, META_DOWN));
            shortcutMap.put(KEY_EDITOR_DELETE_LINE, new KeyCodeCombination(BACK_SPACE, SHIFT_DOWN, CONTROL_DOWN)); // macbook has no DEL key? TODO
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_UP, new KeyCodeCombination(UP, META_DOWN, SHIFT_DOWN));
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_DOWN, new KeyCodeCombination(DOWN, META_DOWN, SHIFT_DOWN));
            shortcutMap.put(KEY_SHOW_POPUP, new KeyCodeCombination(SPACE, META_DOWN, ALT_DOWN));
            modifierMap.put(KEY_MODIFIER_DRAGGING, new KeyCodeCombination(SPACE, META_DOWN));// KeyCode is not used.
        }
        else if (SystemUtils.IS_OS_LINUX) {
            // linux specific
            shortcutMap.put(KEY_EDITOR_QUOTE, new KeyCodeCombination(QUOTE, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_DOUBLE_QUOTE, new KeyCodeCombination(QUOTE, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_BACK_QUOTE, new KeyCodeCombination(BACK_QUOTE, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_DELETE_LINE, new KeyCodeCombination(DELETE, SHIFT_DOWN));
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_UP, new KeyCodeCombination(UP, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_DOWN, new KeyCodeCombination(DOWN, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_SHOW_POPUP, new KeyCodeCombination(SPACE, CONTROL_DOWN));
            modifierMap.put(KEY_MODIFIER_DRAGGING, new KeyCodeCombination(SPACE, CONTROL_DOWN));// KeyCode is not used.
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            // windows specific
            shortcutMap.put(KEY_EDITOR_QUOTE, new KeyCodeCombination(QUOTE, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_DOUBLE_QUOTE, new KeyCodeCombination(QUOTE, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_BACK_QUOTE, new KeyCodeCombination(BACK_QUOTE, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_DELETE_LINE, new KeyCodeCombination(DELETE, SHIFT_DOWN));
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_UP, new KeyCodeCombination(UP, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_EDITOR_MOVE_LINE_DOWN, new KeyCodeCombination(DOWN, SHIFT_DOWN, CONTROL_DOWN));
            shortcutMap.put(KEY_SHOW_POPUP, new KeyCodeCombination(SPACE, CONTROL_DOWN));
            modifierMap.put(KEY_MODIFIER_DRAGGING, new KeyCodeCombination(SPACE, CONTROL_DOWN));// KeyCode is not used.
        }
    }

    public void addShortCut(ShortcutKey key, KeyCodeCombination keyCodeCombination) {
        shortcutMap.put(key, keyCodeCombination);
    }

    public KeyCodeCombination getKeyCombination(ShortcutKey key) {
        return shortcutMap.get(key);
    }

    /**
     * @param keyEvent
     * @param key      pre-defined for shortcut.
     * @return
     */
    public boolean isKeyEventMatch(KeyEvent keyEvent, ShortcutKey key) {
        KeyCodeCombination keyCodeCombination = shortcutMap.get(key);
        if (keyCodeCombination == null) return false;
        return keyCodeCombination.match(keyEvent);
    }

    /**
     * Check if any shortcut matches the key event.
     *
     * @param keyEvent
     * @param keys
     * @return
     */
    public boolean isKeyEventMatch(KeyEvent keyEvent, ShortcutKey... keys) {
        for (ShortcutKey key : keys) {
            KeyCodeCombination keyCodeCombination = shortcutMap.get(key);
            if (keyCodeCombination == null) continue;
            if (keyCodeCombination.match(keyEvent)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMouseWithModifier(MouseEvent mouseEvent, ShortcutKey key) {
        KeyCombination keyCombination = modifierMap.get(key);
        return keyCombination.getControl() == ModifierValue.DOWN && mouseEvent.isControlDown()
                || keyCombination.getAlt() == ModifierValue.DOWN && mouseEvent.isAltDown()
                || keyCombination.getMeta() == ModifierValue.DOWN && mouseEvent.isMetaDown()
                || keyCombination.getShift() == ModifierValue.DOWN && mouseEvent.isShiftDown()
                || keyCombination.getShortcut() == ModifierValue.DOWN && mouseEvent.isShortcutDown();
    }

    /**
     * Check shortcuts conflict for each category.
     * TODO should consider the Global category with other categories.
     *
     * @return
     */
    public boolean hasConflict() {
        AtomicBoolean hasConflict = new AtomicBoolean(false);
        shortcutMap.entrySet().stream().collect(
                        Collectors.groupingBy(shortcutKeyKeyCodeCombinationEntry -> shortcutKeyKeyCodeCombinationEntry.getKey().getCategory(), LinkedHashMap::new, Collectors.toList()))
                .forEach((s, entries) -> {
                    hasConflict.set(hasConflict.get() | CollectionUtils.hasDuplicate(entries.stream().map(Map.Entry::getValue).toList()));
                });
        return hasConflict.get();
    }

    public String exportToMarkdown() {
        LinkedHashMap<String, List<ShortcutKey>> categorized =
                ShortcutManager.shortcutMap.keySet().stream().collect(Collectors.groupingBy(ShortcutKey::getCategory, LinkedHashMap::new, Collectors.toList()));

        StringBuilder buf = new StringBuilder();
        for (String category : categorized.keySet()) {
            buf.append("## ").append(category).append(TextConstants.LINE_SEPARATOR);
            List<ShortcutKey> shortcutKeys = categorized.get(category);
            for (ShortcutKey shortcutKey : shortcutKeys) {
                buf.append("* ").append(shortcutKey.getName()).append("    :    ").append(shortcutMap.get(shortcutKey).getDisplayText()).append(TextConstants.LINE_SEPARATOR);
            }
        }
        return buf.toString();
    }
}
