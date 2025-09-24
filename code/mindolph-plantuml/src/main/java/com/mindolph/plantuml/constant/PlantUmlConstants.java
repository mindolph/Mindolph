package com.mindolph.plantuml.constant;

import com.mindolph.core.constant.SyntaxConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public interface PlantUmlConstants extends SyntaxConstants {

    String[] DIAGRAM_KEYWORDS_START = new String[]{
            "startuml", "startsalt", "startgantt", "startlatex", "startmath", "startdot",
            "startmindmap", "startwbs", "startyaml", "startjson", "startregex", "startebnf"
    };

    String[] DIAGRAM_KEYWORDS_END = new String[]{
            "enduml", "endsalt", "endgantt", "endlatex", "endmath", "enddot",
            "endmindmap", "endwbs", "endyaml", "endjson", "endregex", "endebnf"
    };

    String DIAGRAM_PATTERN = "@(" + String.join("|", ArrayUtils.addAll(DIAGRAM_KEYWORDS_START, DIAGRAM_KEYWORDS_END)) + ")";
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
    String CONTAINING_PATTERN = "(\\b(%s)\\b)|(<style>)|(<\\/style>)".formatted(String.join("|", CONTAINING_KEYWORDS));
    String[] KEYWORDS = new String[]{
            "scale", "skinparam", "title", "usecase", "boundary", "caption", "control", "collections",
            "entity", "database", "detach", "participant", "order", "as", "actor", "autonumber", "resume", "newpage", "is",
            "break", "critical",
            "over", "top", "bottom", "right", "left", "of", "ref", "create", "box", "hide", "footbox",
            "skinparam", "sequence", "start", "state", "stop", "file", "folder", "frame", "fork", "interface", "class", "object", "abstract",
            "namespace", "page", "node", "package", "queue", "stack", "rectangle", "storage", "card", "cloud", "component", "agent", "artifact",
            "center", "footer", "return",
            // C4
            "Person", "Person_Ext", "System", "System_Ext", "SystemDb", "SystemQueue", "SystemDb_Ext", "SystemQueue_Ext", "Boundary", "Enterprise_Boundary", "System_Boundary", "Container", "ContainerDb", "ContainerQueue", "Container_Ext", "ContainerDb_Ext", "ContainerQueue_Ext", "Container_Boundary", "Component", "ComponentDb", "ComponentQueue", "Component_Ext", "ComponentDb_Ext", "ComponentQueue_Ext", "Deployment_Node", "Node", "Node_L", "Node_R", "Rel", "BiRel", "Rel_U", "Rel_Up", "Rel_D", "Rel_Down", "Rel_L", "Rel_Left", "Rel_R", "Rel_Right", "Lay_U", "Lay_Up", "Lay_D", "Lay_Down", "Lay_L", "Lay_Left", "Lay_R", "Lay_Right", "AddElementTag", "AddRelTag", "AddBoundaryTag", "AddPersonTag", "AddExternalPersonTag", "AddSystemTag", "AddExternalSystemTag", "AddComponentTag", "AddExternalComponentTag", "AddContainerTag", "AddExternalContainerTag", "AddNodeTag", "UpdateElementStyle", "UpdateRelStyle", "UpdateBoundaryStyle", "UpdateContainerBoundaryStyle", "UpdateSystemBoundaryStyle", "UpdateEnterpriseBoundaryStyle", "UpdateLegendTitle", "RoundedBoxShape", "EightSidedShape", "DashedLine", "DottedLine", "BoldLine", "SolidLine", "SetPropertyHeader", "WithoutPropertyHeader", "AddProperty", "LAYOUT_TOP_DOWN","LAYOUT_LEFT_RIGHT","LAYOUT_LANDSCAPE","LAYOUT_WITH_LEGEND","SHOW_LEGEND","SHOW_FLOATING_LEGEND","LEGEND","LAYOUT_AS_SKETCH","SET_SKETCH_STYLE","HIDE_STEREOTYPE","HIDE_PERSON_SPRITE","SHOW_PERSON_SPRITE","SHOW_PERSON_PORTRAIT","SHOW_PERSON_OUTLINE",
            // CSS styles
            "FontName","FontColor","FontSize","FontStyle","BackGroundColor","HyperLinkColor","RoundCorner","DiagonalCorner","LineColor","LineThickness","LineStyle","Padding","Margin","MaximumWidth","Shadowing","HyperlinkUnderlineStyle","HyperlinkUnderlineThickness", "HorizontalAlignment"
    };
    String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    // include EBNF note
    String COMMENT_PATTERN = "(?<=^%s*)('.*)|(\\(\\*[.\\s\\S]+?\\*\\))".formatted(BLANK_CHAR);
    String BLOCK_COMMENT_PATTERN = "\\/'[.\\s\\S]+?'\\/";

    String ARROW1 = "[<>ox#\\*\\{\\}\\+\\^]";
    String ARROW2 = "([\\|\\}\\<][\\|o])|([\\|o][\\|\\{\\>])";
    String BAR = "(--)|(\\.\\.)|-|\\.";
    // like connectors between entities
    String CONNECTOR = "((%s)|(%s))*(%s)((%s)|(%s))*".formatted(ARROW2, ARROW1, BAR, ARROW2, ARROW1);
    // include EBNF quote
    String QUOTE_BLOCK = "(\"[^\"]*?\")|(\\[[^\\]]*?\\])|(\\?[.\\s\\S]+?\\?)";

    String ACTIVITY = "%s%s*:[^;\n]*?;%s".formatted(LINE_START, BLANK_CHAR, LINE_END);

    String SWIMLANE_PATTERN = "\\|.*?\\|";

    static void main(String[] args) {
        System.out.println(CONNECTOR);
        String text = """
                :activity;
                u-->m: a
                m-->s: b
                s-->b: c;
                """;
        Pattern patternMajor = Pattern.compile("(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<ACTIVITY>" + ACTIVITY + ")"
                        + "|(?<QUOTE>" + QUOTE_BLOCK + ")"
                        + "|(?<CONNECTOR>" + CONNECTOR + ")"
                        + "|(?<BLOCKCOMMENT>" + BLOCK_COMMENT_PATTERN + ")"
                        + "|(?<SWIMLANE>" + SWIMLANE_PATTERN + ")"
                        + "|(?<DIAGRAMKEYWORDS>" + DIAGRAM_PATTERN + ")"
                        + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                        + "|(?<CONTAININGKEYWORDS>" + CONTAINING_PATTERN + ")"
                        + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                , Pattern.MULTILINE);
        Matcher matcher = patternMajor.matcher(text);
        while (matcher.find()) {
            String styleClass =
                    matcher.group("ACTIVITY") != null ? "activity" :
                            matcher.group("QUOTE") != null ? "quote" :
                                    matcher.group("CONNECTOR") != null ? "connector" :
                                            matcher.group("SWIMLANE") != null ? "swimlane" :
                                                    matcher.group("DIAGRAMKEYWORDS") != null ? "diagramkeyword" :
                                                            matcher.group("DIRECTIVE") != null ? "directive" :
                                                                    matcher.group("CONTAININGKEYWORDS") != null ? "containing" :
                                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                                    matcher.group("BLOCKCOMMENT") != null ? "comment" :
                                                                                            matcher.group("KEYWORD") != null ? "keyword" :
                                                                                                    null; /* never happens */
            assert styleClass != null;
            System.out.printf("matched %s: (pos: %d-%d, count: %d) %n", styleClass, matcher.start(), matcher.end(), matcher.groupCount());
        }
    }
}
