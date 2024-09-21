package com.mindolph.plantuml.constant;

import com.mindolph.core.constant.SyntaxConstants;

/**
 * @author mindolph.com@gmail.com
 */
public interface PlantUmlConstants extends SyntaxConstants {

    String[] DIAGRAM_KEYWORDS = new String[]{
            "startsalt", "endsalt", "startgantt", "endgantt", "startlatex", "endlatex", "startmath", "endmath", "startdot", "enddot",
            "startuml", "enduml", "startmindmap", "endmindmap", "startwbs", "endwbs", "startyaml", "endyaml", "startjson", "endjson"
    };
    String DIAGRAM_PATTERN = "@(" + String.join("|", DIAGRAM_KEYWORDS) + ")";
    String[] DIRECTIVE = new String[]{
            "include", "function", "endfunction", "procedure", "endprocedure", "\\$"
    };
    String DIRECTIVE_PATTERN = "!(" + String.join("|", DIRECTIVE) + ")";
    String[] CONTAINING_KEYWORDS = new String[]{
            "end",
            "if", "then", "endif", "elseif", "repeat", "while", "endwhile", "loop", "switch", "case", "endswitch",
            "header", "endheader", "legend", "endlegend",
            "split again", "split", "end split",
            "note", "endnote", "rnote", "endrnote", "hnote", "endhnote",
            "activate", "deactivate",
            "alt", "else", "opt", "group", "par"
    };
    String CONTAINING_PATTERN = "\\b(" + String.join("|", CONTAINING_KEYWORDS) + ")\\b";
    String[] KEYWORDS = new String[]{
            "scale", "skinparam", "title", "usecase", "boundary", "caption", "control", "collections",
            "entity", "database", "detach", "participant", "order", "as", "actor", "autonumber", "resume", "newpage", "is",
            "break", "critical",
            "over", "top", "bottom", "right", "left", "of", "ref", "create", "box", "hide", "footbox",
            "skinparam", "sequence", "start", "state", "stop", "file", "folder", "frame", "fork", "interface", "class", "abstract",
            "namespace", "page", "node", "package", "queue", "stack", "rectangle", "storage", "card", "cloud", "component", "agent", "artifact",
            "center", "footer", "return"
    };
    String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    String COMMENT_PATTERN = "(" + BLANK_CHAR + "*'.*)";
    String BLOCK_COMMENT_PATTERN = "\\/'[.\\s\\S]+?'\\/";

    String ARROW1 = "[<>ox#\\*\\{\\}\\+\\^]";
    String ARROW2 = "([\\|\\}\\<][\\|o])|([\\|o][\\|\\{\\>])";
    String BAR = "-|\\.|(--)|(\\.\\.)";
    String CONNECTOR = "((%s)|(%s))*(%s)((%s)|(%s))*".formatted(ARROW2, ARROW1, BAR, ARROW2, ARROW1);

    String QUOTE_BLOCK = "(\"[^\"]*?\")|(\\[[^\\]]*?\\])";

    String ACTIVITY = ":[^;]*?;";
}
