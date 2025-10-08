package com.mindolph.base.editor;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.ShortcutConstants;
import com.mindolph.base.control.HighlightCodeArea;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.util.DebugUtils;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.constant.MarkdownConstants.*;
import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;

/**
 * @author mindolph.com@gmail.com
 */
public class MarkdownCodeArea extends HighlightCodeArea {

    private static final Logger log = LoggerFactory.getLogger(MarkdownCodeArea.class);
    /**
     * Pattern for highlighting.
     */
    protected static Pattern patternMajor;
    protected static Pattern patternMinor;

    public MarkdownCodeArea() {
        if (patternMajor == null) {
            patternMajor = Pattern.compile(
                    "(?<HEADING>" + HEADING_PATTERN + ")"
                            + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                            + "|(?<LIST>" + LIST_PATTERN + ")"
                            + "|(?<TABLE>" + TABLE_PATTERN + ")"
                            + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
            );
        }

        if (patternMinor == null) {
            patternMinor = Pattern.compile(
                    "(?<CODE>" + CODE_PATTERN + ")"
                            + "|(?<HRULE>" + HORIZONTAL_RULE_PATTERN + ")"
                            + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                            + "|(?<BOLD>" + BOLD_PATTERN + ")"
                            + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                            + "|(?<URL>" + URL_PATTERN + ")"
            );
        }


        super.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, BACK_QUOTE, AUTO_INDENT);
        InputMap<KeyEvent> comment = InputMap.consume(EventPattern.keyPressed(ShortcutManager.getIns().getKeyCombination(ShortcutConstants.KEY_MD_COMMENT)), keyEvent -> {
            super.addOrTrimHeadToParagraphsIfAdded(new Replacement("> ", "  ")); // TODO add tail with trimming
        });
        Nodes.addInputMap(this, comment);
    }

    @Override
    public void refresh() {
        this.setStyleSpans(0, computeHighlighting(this.getText()));
    }

    @Override
    protected StyleSpans<Collection<String>> computeHighlighting(String text) {
        // do the major matching
        Matcher majorMatcher = patternMajor.matcher(text);
        styleRanges.clear();
        while (majorMatcher.find()) {
            String styleClass =
                    majorMatcher.group("HEADING") != null ? "heading" :
                            majorMatcher.group("LIST") != null ? "list" :
                                    majorMatcher.group("TABLE") != null ? "table" :
                                            majorMatcher.group("CODEBLOCK") != null ? "code-block" :
                                                    majorMatcher.group("QUOTE") != null ? "md-quote" :
                                                            null; /* never happens */
            assert styleClass != null;
            if (StringUtils.isNotBlank(styleClass)) {
                if (log.isTraceEnabled())
                    log.trace("major matched %s: (%d-%d) - %s".formatted(styleClass, majorMatcher.start(), majorMatcher.end(), DebugUtils.visible(StringUtils.substring(text, majorMatcher.start(), majorMatcher.end()))));
                super.append(styleClass, majorMatcher.start(), majorMatcher.end());
            }
        }
        // do the minor matching
        Matcher minorMatcher = patternMinor.matcher(text);
        while (minorMatcher.find()) {
            String styleClass = minorMatcher.group("CODE") != null ? "code" :
                    minorMatcher.group("HRULE") != null ? "bold" :
                            minorMatcher.group("BOLD") != null ? "bold" :
                                    minorMatcher.group("ITALIC") != null ? "italic" :
                                            minorMatcher.group("BOLDITALIC") != null ? "bold-italic" :
                                                    minorMatcher.group("URL") != null ? "url" :
                                                            null;
            if (log.isTraceEnabled())
                log.trace("minor matched %s: (%d-%d) - %s".formatted(styleClass, minorMatcher.start(), minorMatcher.end(), DebugUtils.visible(StringUtils.substring(text, minorMatcher.start(), minorMatcher.end()))));
            super.cutInNewStyle(styleClass, minorMatcher.start(), minorMatcher.end());
        }

        return super.buildStyleSpans(text);
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_MARKDOWN;
    }
}
