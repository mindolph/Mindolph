package com.mindolph.core.constant;

import com.mindolph.core.llm.ModelMeta;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface GenAiConstants {

    int MAX_GENERATION_TOKENS = 2000; // TODO to be configurable

    /**
     * Look up information for pre-defined model.
     *
     * @param providerName
     * @param modelName
     * @return
     */
    static ModelMeta lookupModelMeta(String providerName, String modelName) {
        Collection<ModelMeta> modelMetas = PROVIDER_MODELS.get(providerName);
        Optional<ModelMeta> first = modelMetas.stream().filter(m -> m.name().equals(modelName)).findFirst();
        return first.orElse(null);
    }

    MultiValuedMap<String, ModelMeta> PROVIDER_MODELS = new HashSetValuedHashMap<>() {
        {
            // https://platform.openai.com/docs/models
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5-turbo", 4096));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-3.5-turbo-instruct", 4096));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4", 8192));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4-turbo", 4096));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4o", 16384));
            put(GenAiModelProvider.OPEN_AI.getName(), new ModelMeta("gpt-4o-mini", 16384));

            // https://ai.google.dev/gemini-api/docs/models/gemini
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-2.0-flash-exp", 8192));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-pro-latest", 8192));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-pro", 8192));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-flash", 8192));
            put(GenAiModelProvider.GEMINI.getName(), new ModelMeta("gemini-1.5-flash-8b", 8192));

            // https://help.aliyun.com/zh/model-studio/developer-reference/what-is-qwen-llm#267c7b3691v9k
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-72b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-32b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-14b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-7b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-3b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-1.5b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2.5-0.5b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-72b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-57b-a14b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-7b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-1.5b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen2-0.5b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-110b-chat", 8000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-72b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-32b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-14b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-7b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-1.8b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen1.5-0.5b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-72b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-14b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-7b-chat", 1500));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-1.8b-longcontext-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-1.8b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-max", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-max-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-plus", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-plus-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-turbo", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-turbo-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-plus", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-plus-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-turbo", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.getName(), new ModelMeta("qwen-coder-turbo-latest", 8192));
//            put(GenAiModelProvider.ALI_Q_WEN.getName(),new  ModelMeta("qwen-long", 1024));


            // https://www.bigmodel.cn/console/modelcenter/square
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-zero-preview", 16000));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-plus", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-airx", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-air", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-long", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-flashx", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-flash", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4-9b", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-4", 4095));
            put(GenAiModelProvider.CHAT_GLM.getName(), new ModelMeta("glm-3-turbo", 4095));

            // DeepSeek
            put(GenAiModelProvider.DEEP_SEEK.getName(), new ModelMeta("deepseek-chat", 8192));
            put(GenAiModelProvider.DEEP_SEEK.getName(), new ModelMeta("deepseek-reasoner", 8192));
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

    record ProviderInfo(String name, String model) {
    }
}
