package com.mindolph.base.plugin;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class ContextHelperPlugin extends BasePlugin {

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
    public Optional<InputHelper> getInputHelper() {
        return Optional.of(inputHelper);
    }

    /**
     * Default context helper.
     */
    public static class ContextHelper implements InputHelper {
        // editor id -> suggestion string list TODO the element should be removed when editor is destroyed.
        private final Map<Object, List<String>> contextWordsMap = new HashMap<>();

        @Override
        public List<String> getHelpWords(Object editorId) {
            return contextWordsMap.get(editorId);
        }

        @Override
        public void updateContextText(Object editorId, String text) {
            StringTokenizer st = new StringTokenizer(extractOnlyLetters(text));
            List<String> words = contextWordsMap.computeIfAbsent(editorId, k -> new ArrayList<>());
            words.clear();
            while (st.hasNext()) {
                String token = st.nextToken();
                if (StringUtils.isBlank(token) || token.length() <= 2) {
                    continue;
                }
                words.add(token);
            }
            log.debug("%d context words updated.".formatted(words.size()));
        }

        public static String extractOnlyLetters(String text) {
            // this replacement makes more friendly for programming code.
            return RegExUtils.replaceAll(text, "[\\s\\r,.()=\"'`\\[\\]\\\\/\\-\\?:#\\!\\^&%\\$]", " ");
        }

    }
}
