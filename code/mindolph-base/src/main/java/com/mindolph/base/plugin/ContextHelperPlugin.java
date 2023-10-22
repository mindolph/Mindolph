package com.mindolph.base.plugin;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class ContextHelperPlugin implements Plugin {

    private static final Logger log = LoggerFactory.getLogger(ContextHelperPlugin.class);

    private final InputHelper inputHelper = new ContextHelper();

    @Override
    public Integer getOrder() {
        return 1;
    }

    @Override
    public Collection<String> supportedFileTypes() {
        return Arrays.asList(TYPE_PLANTUML, TYPE_MARKDOWN, TYPE_PLAIN_TEXT);
    }

    @Override
    public InputHelper getInputHelper() {
        return inputHelper;
    }

    public static class ContextHelper implements InputHelper {
        private final List<String> contextWords = new ArrayList<>();

        @Override
        public List<String> getHelpWords() {
            return contextWords;
        }

        @Override
        public void updateContextText(String text) {
            StringTokenizer st = new StringTokenizer(this.extractOnlyLetters(text));
            contextWords.clear();
            while (st.hasNext()) {
                String token = st.nextToken();
                if (StringUtils.isBlank(token) || token.length() < 2) {
                    continue;
                }
                contextWords.add(token);
            }
            log.debug("%d context words updated.".formatted(contextWords.size()));
        }

        private String extractOnlyLetters(String text) {
            String cleanText = RegExUtils.replaceAll(text, "[^a-zA-Z]", " ");
            return cleanText;
        }

    }
}
