package com.mindolph.base.genai.llm;

import com.mindolph.core.constant.SupportFileTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface Constants {
    enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        DISCARD // discard the generated text
    }

    enum OutputAdjust {
        SHORTER,
        LONGER
    }

    enum OutputFormat {
        TEXT("Text"),
        MARKDOWN("Markdown"),
        JSON("JSON"),
        MINDMAP("indented text");

        private final String name;

        OutputFormat(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    Map<String, OutputFormat> FILE_OUTPUT_MAPPING = new HashMap<>(){
        {
            put(SupportFileTypes.TYPE_PLAIN_TEXT, OutputFormat.TEXT);
            put(SupportFileTypes.TYPE_MARKDOWN, OutputFormat.MARKDOWN);
            put(SupportFileTypes.TYPE_MIND_MAP, OutputFormat.MINDMAP);
        }
    };
}
