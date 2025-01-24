package com.mindolph.base;

import com.mindolph.base.constant.IconKey;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.model.NodeData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import de.jensd.fx.glyphs.icons525.Icons525;
import de.jensd.fx.glyphs.icons525.utils.Icon525Factory;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.utils.MaterialDesignIconFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.octicons.utils.OctIconFactory;
import javafx.scene.text.Text;
import org.apache.commons.lang3.SystemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author mindolph.com@gmail.com
 */
public class FontIconManager {
    public static final String DEFAULT_ICON_SIZE = "16";
    private static final FontIconManager ins = new FontIconManager();
    //    private final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

    // icons with default size.
    private final Map<IconKey, Supplier<Text>> icons = new HashMap<>();

    public static FontIconManager getIns() {
        return ins;
    }

    public FontIconManager() {
        // general
        icons.put(IconKey.FILE, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FILE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PLUS, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLUS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.RENAME, () -> MaterialIconFactory.get().createIcon(MaterialIcon.EDIT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CLONE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.CONTENT_COPY, DEFAULT_ICON_SIZE));
        icons.put(IconKey.DELETE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.DELETE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.COLLAPSE_ALL, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.MINUS_CIRCLE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.COLLAPSE_FOLDERS, () -> Icon525Factory.get().createIcon(Icons525.SHRINK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.OK, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHECK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.YES, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHECK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FINISH, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHECK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CLOSE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CLOSE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.REFRESH, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.REFRESH, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SEARCH, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SEARCH, DEFAULT_ICON_SIZE));
        icons.put(IconKey.REPLACE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SEARCH_PLUS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SORT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.SORT_BY_ALPHA, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CODE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CODE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PREVIEW, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.IMAGE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SWITCH_HORIZONTAL, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_LEFT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SWITCH_VERTICAL, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_RIGHT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CASE_SENSITIVITY, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.CASE_SENSITIVE_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.NEXT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.NAVIGATE_NEXT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PREVIOUS, () -> MaterialIconFactory.get().createIcon(MaterialIcon.NAVIGATE_BEFORE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PRINTER, () -> MaterialIconFactory.get().createIcon(MaterialIcon.PRINT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PAGE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FILE_TEXT_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PDF, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FILE_PDF_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.WORKSPACE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CUBE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.WORKSPACE_TREE, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FILE_TREE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.RECENT_LIST, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.HISTORY, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_MMD, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SNOWFLAKE_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_PUML, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FACTORY, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_MD, () -> OctIconFactory.get().createIcon(OctIcon.MARKDOWN, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_CSV, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_LARGE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_IMG, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.IMAGE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_TXT, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_TEXT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.UNKNOWN_FILE, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FILE_OUTLINE, DEFAULT_ICON_SIZE));

        icons.put(IconKey.BOLD, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_BOLD, DEFAULT_ICON_SIZE));
        icons.put(IconKey.ITALIC, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_ITALIC, DEFAULT_ICON_SIZE));
        icons.put(IconKey.INDENT_INCREASE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_INDENT_INCREASE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.INDENT_DECREASE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_INDENT_DECREASE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.BULLET_LIST, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_LIST_BULLETED, DEFAULT_ICON_SIZE));
        icons.put(IconKey.NUMBER_LIST, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_LIST_NUMBERED, DEFAULT_ICON_SIZE));
        icons.put(IconKey.TABLE, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_LARGE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CODE_TAG, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.CODE_TAGS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.QUOTE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_QUOTE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H1, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_1, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H2, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_2, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H3, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_3, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H4, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_4, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H5, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_5, DEFAULT_ICON_SIZE));
        icons.put(IconKey.H6, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_HEADER_6, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SEPARATOR, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.WINDOW_MINIMIZE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.COMMENT, () -> MaterialDesignIconFactory.get().createIcon(MaterialIcon.CHAT_BUBBLE_OUTLINE, DEFAULT_ICON_SIZE));

        icons.put(IconKey.OUTLINE, () -> OctIconFactory.get().createIcon(OctIcon.THREE_BARS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.OUTLINE_ITEM, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.RECORD, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SNIPPET, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.CODE_BRACES, DEFAULT_ICON_SIZE));

        if (SystemUtils.IS_OS_MAC) {
            icons.put(IconKey.SYSTEM, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.APPLE, DEFAULT_ICON_SIZE));
        }
        else if (SystemUtils.IS_OS_LINUX) {
            icons.put(IconKey.SYSTEM, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.LINUX, DEFAULT_ICON_SIZE));
        }
        else if (SystemUtils.IS_OS_WINDOWS) {
            icons.put(IconKey.SYSTEM, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.WINDOWS, DEFAULT_ICON_SIZE));
        }
        // for mmd
        icons.put(IconKey.OPEN, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SAVE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SAVE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.BROWSE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.GLOBE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CLEAR, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PAINT_BRUSH, DEFAULT_ICON_SIZE));
        icons.put(IconKey.UNDO, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_LEFT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.REDO, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_RIGHT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.LOCK, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.LOCK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FOLD, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER, DEFAULT_ICON_SIZE));
        icons.put(IconKey.UNFOLD, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, DEFAULT_ICON_SIZE));
        icons.put(IconKey.GEAR, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.GEAR, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FONT, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FONT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.UNSELECT, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.UNLINK, DEFAULT_ICON_SIZE));

        icons.put(IconKey.ALIGN, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_ALIGN_JUSTIFY, DEFAULT_ICON_SIZE));
        icons.put(IconKey.ALIGN_LEFT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_ALIGN_LEFT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.ALIGN_RIGHT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_ALIGN_RIGHT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.ALIGN_CENTER, () -> MaterialIconFactory.get().createIcon(MaterialIcon.FORMAT_ALIGN_CENTER, DEFAULT_ICON_SIZE));

        icons.put(IconKey.COPY, () -> MaterialIconFactory.get().createIcon(MaterialIcon.CONTENT_COPY, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CUT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.CONTENT_CUT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.PASTE, () -> MaterialIconFactory.get().createIcon(MaterialIcon.CONTENT_PASTE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.WRAP, () -> MaterialIconFactory.get().createIcon(MaterialIcon.WRAP_TEXT, DEFAULT_ICON_SIZE));

        icons.put(IconKey.SHORT_TEXT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.SHORT_TEXT));
        icons.put(IconKey.LONG_TEXT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.REORDER));
        icons.put(IconKey.SEND, () -> MaterialIconFactory.get().createIcon(MaterialIcon.SEND));
        icons.put(IconKey.TEMPERATURE, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.OIL_TEMPERATURE));
        icons.put(IconKey.GEN_AI, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.ROBOT));
        icons.put(IconKey.MAGIC, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.MAGIC));

        icons.put(IconKey.EDIT_TEXT, () -> MaterialIconFactory.get().createIcon(MaterialIcon.EDIT, DEFAULT_ICON_SIZE));
//        icons.put(IconKey.ADD_CHILD, () -> MaterialIconFactory.get().createIcon(MaterialIcon.PLUS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.EDIT_COLORS, () -> MaterialIconFactory.get().createIcon(MaterialIcon.COLOR_LENS, DEFAULT_ICON_SIZE));
        icons.put(IconKey.SHOW_JUMPS, () -> MaterialIconFactory.get().createIcon(MaterialIcon.LINK, DEFAULT_ICON_SIZE));
        icons.put(IconKey.EXPAND_ALL, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.PLUS_CIRCLE_OUTLINE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.COLLAPSE_ALL, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.MINUS_CIRCLE_OUTLINE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.CONVERT, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TRANSFER, DEFAULT_ICON_SIZE));
        icons.put(IconKey.NOTE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.STICKY_NOTE_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FILE_LINK, () -> OctIconFactory.get().createIcon(OctIcon.LINK_EXTERNAL, DEFAULT_ICON_SIZE));
        icons.put(IconKey.URI, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.LINK_VARIANT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.JUMP, () -> OctIconFactory.get().createIcon(OctIcon.FILE_SYMLINK_FILE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.IMAGE, () -> FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FILE_IMAGE_ALT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.EMOTICONS, () -> MaterialIconFactory.get().createIcon(MaterialIcon.INSERT_EMOTICON, DEFAULT_ICON_SIZE));
        icons.put(IconKey.EXPORT, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.EXPORT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.IMPORT, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.IMPORT, DEFAULT_ICON_SIZE));
        icons.put(IconKey.TOPIC, () -> MaterialIconFactory.get().createIcon(MaterialIcon.CHAT_BUBBLE_OUTLINE, DEFAULT_ICON_SIZE));
        icons.put(IconKey.FOLDER, () -> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FOLDER_OUTLINE));
        icons.put(IconKey.MOVE_FOLDER, ()-> MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FOLDER_MOVE));
    }

    public Text getIcon(IconKey iconKey) {
        Supplier<Text> textSupplier = icons.get(iconKey);
        if (textSupplier == null) {
            return null;
        }
        return textSupplier.get();
    }

    /**
     * @param fileType
     * @param size
     * @return
     * @since 1.3.2
     */
    public Text getIconForFile(String fileType, double size) {
        String iconSize = String.valueOf(size);
        if (SupportFileTypes.TYPE_WORKSPACE.equals(fileType)) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.CUBE, iconSize);
        }
        else if (SupportFileTypes.TYPE_FOLDER.equals(fileType)) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FOLDER_OUTLINE, iconSize);
        }
        else if (SupportFileTypes.TYPE_MIND_MAP.equals(fileType)) {
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SNOWFLAKE_ALT, iconSize);
        }
        else if (SupportFileTypes.TYPE_PLANTUML.equals(fileType)) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FACTORY, iconSize);
        }
        else if (SupportFileTypes.TYPE_PLAIN_TEXT.equals(fileType)) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_TEXT, iconSize);
        }
        else if (SupportFileTypes.TYPE_MARKDOWN.equals(fileType)) {
            return OctIconFactory.get().createIcon(OctIcon.MARKDOWN, iconSize);
        }
        else if (SupportFileTypes.TYPE_CSV.equals(fileType)) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_LARGE, iconSize);
        }
        else if (SupportFileTypes.TYPE_PLAIN_JPG.equals(fileType) || SupportFileTypes.TYPE_PLAIN_PNG.equals(fileType)) {
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.IMAGE, iconSize);
        }
        else {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FILE_OUTLINE, iconSize); // as default icon
        }
    }

    /**
     * @param fileData
     * @param size
     * @return
     * @since 1.3.2
     */
    public Text getIconForFile(NodeData fileData, double size) {
        String iconSize = String.valueOf(size);
        if (fileData.isFolder()) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FOLDER_OUTLINE, iconSize);
        }
        else if (fileData.isMindMap()) {
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SNOWFLAKE_ALT, iconSize);
        }
        else if (fileData.isPlantUml()) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FACTORY, iconSize);
        }
        else if (fileData.isPlainText()) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FORMAT_TEXT, iconSize);
        }
        else if (fileData.isMarkdown()) {
            return OctIconFactory.get().createIcon(OctIcon.MARKDOWN, iconSize);
        }
        else if (fileData.isCsv()) {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.TABLE_LARGE, iconSize);
        }
        else if (fileData.isImage()) {
            return FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.IMAGE, iconSize);
        }
        else {
            return MaterialDesignIconFactory.get().createIcon(MaterialDesignIcon.FILE_OUTLINE, iconSize); // as default icon
        }
    }

    /**
     * @param fileData
     * @return
     * @since 1.3.2
     */
    public Text getIconForFile(NodeData fileData) {
        if (fileData.isFolder()) {
            return getIcon(IconKey.FOLDER);
        }
        else if (fileData.isMindMap()) {
            return getIcon(IconKey.FILE_MMD);
        }
        else if (fileData.isPlantUml()) {
            return getIcon(IconKey.FILE_PUML);
        }
        else if (fileData.isPlainText()) {
            return getIcon(IconKey.FILE_TXT);
        }
        else if (fileData.isMarkdown()) {
            return getIcon(IconKey.FILE_MD);
        }
        else if (fileData.isCsv()) {
            return getIcon(IconKey.FILE_CSV);
        }
        else if (fileData.isImage()) {
            return getIcon(IconKey.FILE_IMG);
        }
        else {
            return getIcon(IconKey.UNKNOWN_FILE); // as default icon
        }
    }
}
