package com.mindolph.core.constant;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface GenAiConstants {

    int MAX_GENERATION_TOKENS = 2000; // TODO to be configurable
    int MAX_SUMMARIZE_TOKENS = 256;

    MultiValuedMap<String, ModelMeta> PROVIDER_MODELS = new HashSetValuedHashMap<>() {
        {
            // https://platform.openai.com/docs/models
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5-turbo", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5-turbo-instruct", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5-turbo-16k", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4-32k", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4-turbo", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4o", 1024));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4o-mini", 1024));

            // https://ai.google.dev/gemini-api/docs/models/gemini
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-2.0-flash-exp", 1024));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-pro-latest", 1024));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-pro", 1024));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-flash", 1024));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-flash-8b", 1024));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-pro", 1024));

            // https://help.aliyun.com/zh/model-studio/developer-reference/what-is-qwen-llm#267c7b3691v9k
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-72b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-32b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-14b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-7b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-3b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-1.5b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-0.5b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-72b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-57b-a14b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-7b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-1.5b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-0.5b-instruct", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-110b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-72b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-32b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-14b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-7b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-1.8b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-0.5b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-72b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-14b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-7b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-1.8b-longcontext-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-1.8b-chat", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-max", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-max-latest", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-plus", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-plus-latest", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-turbo", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-turbo-latest", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-plus", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-plus-latest", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-turbo", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-turbo-latest", 1024));
//            put(GenAiModelProvider.ALI_Q_WEN.getName(),new  ModelMeta("qwen-long", 1024));


            // https://www.bigmodel.cn/console/modelcenter/square
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-zero-preview", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-plus", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-airx", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-air", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-long", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-flashx", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-flash", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-9b", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4", 1024));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-3-turbo", 1024));
        }
    };

    enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        DISCARD, // discard the generated text
        STOP, // Stop the generating
        ABORT // Abort the summarizing
    }

    enum OutputAdjust {
        SHORTER,
        LONGER
    }

    enum OutputFormat {
        TEXT("Text"),
        MARKDOWN("Markdown"),
        JSON("JSON"),
        MINDMAP("indented text"),
        PLANTUML("PLANTUML");

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
            put(SupportFileTypes.TYPE_PLANTUML, OutputFormat.PLANTUML);
        }
    };

    record ProviderProps(String apiKey, String baseUrl, String aiModel, boolean useProxy, List<String> customModels) {
        public ProviderProps(String apiKey, String baseUrl, String aiModel, boolean useProxy) {
            this(apiKey, baseUrl, aiModel, useProxy, List.of());
        }
    }

    record ModelMeta(String name, int maxTokens){

    }

    record ProviderInfo(String name, String model) {
    }
}
