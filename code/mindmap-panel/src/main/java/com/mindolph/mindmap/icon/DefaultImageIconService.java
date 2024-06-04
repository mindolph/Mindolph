package com.mindolph.mindmap.icon;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * @deprecated since 1.8
 */
public class DefaultImageIconService implements ImageIconService {

    private static final Map<IconID, Image> MAP = new EnumMap<>(IconID.class);

    static {
//        MAP.put(IconID.POPUP_IMPORT_TXT2MM, loadIcon("external/import_txt2mm.png"));
//        MAP.put(IconID.POPUP_IMPORT_XMIND2MM, loadIcon("external/xmind16.png"));
//        MAP.put(IconID.POPUP_IMPORT_NOVAMIND2MM, loadIcon("external/novamind16.png"));
//        MAP.put(IconID.POPUP_IMPORT_COGGLE2MM, loadIcon("external/coggle16.png"));
//        MAP.put(IconID.POPUP_EXPORT_FREEMIND, loadIcon("external/mm16.png"));
//        MAP.put(IconID.POPUP_EXPORT_SVG, loadIcon("external/svg16.png"));
//        MAP.put(IconID.POPUP_EXPORT_MARKDOWN, loadIcon("external/md16.png"));
//        MAP.put(IconID.POPUP_EXPORT_ASCIIDOC, loadIcon("external/asciidoc16.png"));
//        MAP.put(IconID.POPUP_EXPORT_ORGMODE, loadIcon("external/orgmode16.png"));
//        MAP.put(IconID.POPUP_EXPORT_MINDMUP, loadIcon("external/mup16.png"));
//        MAP.put(IconID.POPUP_EXPORT_PNG, loadIcon("external/png16.png"));
//        MAP.put(IconID.POPUP_EXPORT_TEXT, loadIcon("external/txt16.png"));
    }

    public DefaultImageIconService() {
    }

    private static Image loadIcon(String name) {
        String resPath = "icon/" + name;
        InputStream in = DefaultImageIconService.class.getClassLoader().getResourceAsStream(resPath);
        if (in == null) {
            throw new RuntimeException(resPath + " is not found");
        }
        return new Image(in, 16, 16, true, true);
    }


    @Override
    public Image getIconForId(IconID id) {
        return MAP.get(id);
    }

}
