package com.mindolph.markdown;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.control.HighlightCodeArea;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.markdown.constant.ShortcutConstants;
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
import static com.mindolph.markdown.constant.MarkdownConstants.*;

/**
 *
 *
 * @author mindolph.com@gmail.com
 */
public class MarkdownCodeArea extends HighlightCodeArea {

    public MarkdownCodeArea() {
        pattern = Pattern.compile(
                "(?<HEADING>" + HEADING_PATTERN + ")"
                        + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                        + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                        + "|(?<BOLD>" + BOLD_PATTERN + ")"
                        + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                        + "|(?<LIST>" + LIST_PATTERN + ")"
                        + "|(?<TABLE>" + TABLE_PATTERN + ")"
                        + "|(?<CODE>" + CODE_PATTERN + ")"
                        + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
                        + "|(?<URL>" + URL_PATTERN + ")"
        );

        super.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, BACK_QUOTE, AUTO_INDENT);
        InputMap<KeyEvent> comment = InputMap.consume(EventPattern.keyPressed(ShortcutManager.getIns().getKeyCombination(ShortcutConstants.KEY_MD_COMMENT)), keyEvent -> {
            super.addOrTrimHeadToParagraphsIfAdded(new Replacement("> ")); // TODO add tail
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
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("CODEBLOCK") != null ? "code-block" :
                                    matcher.group("LIST") != null ? "list" :
                                            matcher.group("TABLE") != null ? "table" :
                                                    matcher.group("BOLD") != null ? "bold" :
                                                            matcher.group("ITALIC") != null ? "italic" :
                                                                    matcher.group("BOLDITALIC") != null ? "bold-italic" :
                                                                            matcher.group("CODE") != null ? "code" :
                                                                                    matcher.group("QUOTE") != null ? "md-quote" :
                                                                                            matcher.group("URL") != null ? "url" :
                                                                                                    null; /* never happens */
            assert styleClass != null;
            // System.out.printf("%s(%d-%d)%n", styleClass, matcher.start(), matcher.end());
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_MARKDOWN;
    }
}
