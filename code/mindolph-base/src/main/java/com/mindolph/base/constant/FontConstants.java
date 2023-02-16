package com.mindolph.base.constant;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public interface FontConstants {

    String KEY_MMD_TOPIC_FONT = "mmd.topicFont";
    String KEY_MMD_NOTE_FONT = "mmd.noteFont";
    String KEY_PUML_EDITOR = "puml.editorFont";
    String KEY_MD_EDITOR = "md.editorFont";
    String KEY_TXT_EDITOR = "txt.editorFont";

    Map<String, Font> DEFAULT_FONTS = new HashMap<>() {{
        put(KEY_MMD_TOPIC_FONT, Font.font("serif", FontWeight.BOLD, 16));
        put(KEY_MMD_NOTE_FONT, Font.font("serif", FontWeight.NORMAL, 15));
        put(KEY_PUML_EDITOR, Font.font("monospaced", FontWeight.NORMAL, 15));
        put(KEY_MD_EDITOR, Font.font("monospaced", FontWeight.NORMAL, 15));
        put(KEY_TXT_EDITOR, Font.font("monospaced", FontWeight.NORMAL, 15));
    }};

}
