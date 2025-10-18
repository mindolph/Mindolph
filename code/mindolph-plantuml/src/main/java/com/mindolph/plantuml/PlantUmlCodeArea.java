package com.mindolph.plantuml;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.control.HighlightCodeArea;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.util.DebugUtils;
import com.mindolph.plantuml.constant.ShortcutConstants;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;
import static com.mindolph.plantuml.constant.PlantUmlConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlCodeArea extends HighlightCodeArea {
    /**
     * Pattern for highlighting.
     */
    protected static Pattern patternMajor;

    public PlantUmlCodeArea() {
        super();
        if (patternMajor == null) {
            patternMajor = Pattern.compile("(?<COMMENT>" + COMMENT_PATTERN + ")"
                            + "|(?<ACTIVITY>" + ACTIVITY + ")"
                            + "|(?<QUOTE>" + QUOTE_BLOCK + ")"
                            + "|(?<CONNECTOR>" + CONNECTOR + ")"
                            + "|(?<BLOCKCOMMENT>" + BLOCK_COMMENT_PATTERN + ")"
                            + "|(?<SWIMLANE>" + SWIMLANE_PATTERN + ")"
                            + "|(?<DIAGRAMKEYWORDS>" + DIAGRAM_PATTERN + ")"
                            + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                            + "|(?<CONTAININGKEYWORDS>" + CONTAINING_PATTERN + ")"
                            + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                            + "|(?<JSON>" + JSON_PATTERN + ")"
                            + "|(?<YAML>" + YAML_PATTERN + ")"
                    , Pattern.MULTILINE);
        }
        super.addFeatures(HELPER, TAB_INDENT, QUOTE, DOUBLE_QUOTE, AUTO_INDENT);

        // comment or uncomment for plantuml.
        InputMap<KeyEvent> comment = InputMap.consume(EventPattern.keyPressed(ShortcutManager.getIns().getKeyCombination(ShortcutConstants.KEY_PUML_COMMENT)), keyEvent -> {
            super.addOrTrimHeadToParagraphsIfAdded(new Replacement("' "));
        });
        Nodes.addInputMap(this, comment);
    }

    @Override
    protected StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcherMajor = patternMajor.matcher(text);
        styleRanges.clear();
        while (matcherMajor.find()) {
            String styleClass =
                    matcherMajor.group("ACTIVITY") != null ? "activity" :
                            matcherMajor.group("QUOTE") != null ? "quote" :
                                    matcherMajor.group("CONNECTOR") != null ? "connector" :
                                            matcherMajor.group("SWIMLANE") != null ? "swimlane" :
                                            matcherMajor.group("DIAGRAMKEYWORDS") != null ? "diagramkeyword" :
                                                    matcherMajor.group("DIRECTIVE") != null ? "directive" :
                                                            matcherMajor.group("CONTAININGKEYWORDS") != null ? "containing" :
                                                                    matcherMajor.group("COMMENT") != null ? "comment" :
                                                                            matcherMajor.group("BLOCKCOMMENT") != null ? "comment" :
                                                                                    matcherMajor.group("KEYWORD") != null ? "keyword" :
                                                                                            matcherMajor.group("JSON") != null ? "json" :
                                                                                                    matcherMajor.group("YAML") != null ? "yaml" :
                                                                                            null; /* never happens */
            assert styleClass != null;
            System.out.printf("matched %s: (pos: %d-%d) - %s %n", styleClass, matcherMajor.start(), matcherMajor.end(), DebugUtils.visible(StringUtils.substring(text, matcherMajor.start(), matcherMajor.end())));
            super.append(styleClass, matcherMajor.start(), matcherMajor.end());
        }

//        Pattern patternMinor = Pattern.compile("(?<JSON>" + JSON_PATTERN + ")"
//                        + "|(?<YAML>" + YAML_PATTERN + ")"
//                , Pattern.MULTILINE);
//        Matcher matcherMinor = patternMinor.matcher(text);
//        while (matcherMinor.find()) {
//            String styleClass =
//                    matcherMinor.group("JSON") != null ? "json" :
//                            matcherMinor.group("YAML") != null ? "yaml" :
//                            null; /* never happens */
//            assert styleClass != null;
//            System.out.printf("matched %s: (pos: %d-%d, count: %d) %n", styleClass, matcherMinor.start(), matcherMinor.end(), matcherMinor.groupCount());
//            System.out.println("`%s`".formatted(StringUtils.substring(text, matcherMinor.start(), matcherMinor.end())));
//            super.cutInNewStyle(styleClass, matcherMinor.start(), matcherMinor.end());
//        }
        return super.buildStyleSpans(text);
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_PLANTUML;
    }
}
