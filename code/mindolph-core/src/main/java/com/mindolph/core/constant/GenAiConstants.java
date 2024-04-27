package com.mindolph.core.constant;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface GenAiConstants {

    MultiValuedMap<String, String> providerModels = new HashSetValuedHashMap<>(){
        {
            put(GenAiModelProvider.OPEN_AI.getName(), "gpt-3.5");
            put(GenAiModelProvider.OPEN_AI.getName(), "gpt-3.5-turbo");
            put(GenAiModelProvider.OPEN_AI.getName(), "gpt-3.5-turbo-instruct");
            put(GenAiModelProvider.OPEN_AI.getName(), "gpt-4");
            put(GenAiModelProvider.OPEN_AI.getName(), "gpt-4-turbo");
            put(GenAiModelProvider.GEMINI.getName(), "gemini-1.5-pro-latest");
            put(GenAiModelProvider.GEMINI.getName(), "gemini-pro");
            put(GenAiModelProvider.GEMINI.getName(), "gemini-pro-vision");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-turbo");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-plus");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-max");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-max-longcontext");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-turbo");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-72b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-32b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-14b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-7b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-1.8b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen1.5-0.5b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-72b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-14b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-7b-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-1.8b-longcontext-chat");
            put(GenAiModelProvider.ALI_Q_WEN.getName(), "qwen-1.8b-chat");
        }
    };

    enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        DISCARD, // discard the generated text
        STOP
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

    Map<String, OutputFormat> FILE_OUTPUT_MAPPING = new HashMap<>() {
        {
            // text doesn't need to indicate the output format.
//            put(SupportFileTypes.TYPE_PLAIN_TEXT, OutputFormat.TEXT);
            put(SupportFileTypes.TYPE_MARKDOWN, OutputFormat.MARKDOWN);
            put(SupportFileTypes.TYPE_MIND_MAP, OutputFormat.MINDMAP);
        }
    };

    record ProviderProps(String apiKey, String baseUrl, String aiModel) {
    }

    record ProviderInfo(String name, String model) {
    }
}
