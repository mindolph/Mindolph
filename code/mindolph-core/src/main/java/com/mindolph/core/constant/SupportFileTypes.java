package com.mindolph.core.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mindolph.com@gmail.com
 */
public interface SupportFileTypes {
    String TYPE_FOLDER = "folder"; // special file type

    String TYPE_MIND_MAP = "mmd";
    String TYPE_PLANTUML = "puml";
    String TYPE_MARKDOWN = "md";
    String TYPE_PLAIN_TEXT = "txt";
    String TYPE_PLAIN_JPG = "jpg";
    String TYPE_PLAIN_PNG = "png";
    String TYPE_CSV = "csv";

    Set<String> EDITABLE_TYPES = new HashSet<>() {
        {
            add(TYPE_MIND_MAP);
            add(TYPE_MARKDOWN);
            add(TYPE_PLANTUML);
            add(TYPE_PLAIN_TEXT);
            add(TYPE_CSV);
        }
    };
}
