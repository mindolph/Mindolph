package com.mindolph.plantuml;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.control.HighlightCodeArea;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.plantuml.constant.ShortcutConstants;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;
import static com.mindolph.plantuml.constant.PlantUmlConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantUmlCodeArea extends HighlightCodeArea {

    public PlantUmlCodeArea() {
        pattern = Pattern.compile("(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<ACTIVITY>" + ACTIVITY + ")"
                        + "|(?<QUOTE>" + QUOTE_BLOCK + ")"
                        + "|(?<CONNECTOR>" + CONNECTOR + ")"
                        + "|(?<BLOCKCOMMENT>" + BLOCK_COMMENT_PATTERN + ")"
                        + "|(?<DIAGRAMKEYWORDS>" + DIAGRAM_PATTERN + ")"
                        + "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
                        + "|(?<CONTAININGKEYWORDS>" + CONTAINING_PATTERN + ")"
                        + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                , Pattern.MULTILINE);

        super.addFeatures(HELPER, TAB_INDENT, QUOTE, DOUBLE_QUOTE, AUTO_INDENT);

        // comment or uncomment for plantuml.
        InputMap<KeyEvent> comment = InputMap.consume(EventPattern.keyPressed(ShortcutManager.getIns().getKeyCombination(ShortcutConstants.KEY_PUML_COMMENT)), keyEvent -> {
            super.addOrTrimHeadToParagraphsIfAdded(new Replacement("' "));
        });
        Nodes.addInputMap(this, comment);
    }

    @Override
    public void refresh() {
        this.setStyleSpans(0, computeHighlighting(this.getText()));
    }

    @Override
    protected StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("ACTIVITY") != null ? "activity" :
                            matcher.group("QUOTE") != null ? "quote" :
                                    matcher.group("CONNECTOR") != null ? "connector" :
                                            matcher.group("DIAGRAMKEYWORDS") != null ? "diagramkeyword" :
                                                    matcher.group("DIRECTIVE") != null ? "directive" :
                                                            matcher.group("CONTAININGKEYWORDS") != null ? "containing" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            matcher.group("BLOCKCOMMENT") != null ? "comment" :
                                                                                    matcher.group("KEYWORD") != null ? "keyword" :
                                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_PLANTUML;
    }
}
