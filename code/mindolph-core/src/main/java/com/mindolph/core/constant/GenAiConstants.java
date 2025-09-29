package com.mindolph.core.constant;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ModelMetaBuilder;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface GenAiConstants {

    int MODEL_TYPE_CHAT = 1;
    int MODEL_TYPE_EMBEDDING = 2;

    // Be used to indicate that the model is a custom model
    // This will be removed in the future.
    @Deprecated(since = "1.13")
    String CUSTOM_MODEL_KEY = "Custom";

    /**
     * Look up information for a pre-defined model.
     *
     * @param providerName
     * @param modelName
     * @return
     */
    static ModelMeta lookupModelMeta(String providerName, String modelName) {
        Collection<ModelMeta> modelMetas = PROVIDER_MODELS.get(providerName);
        Optional<ModelMeta> first = modelMetas.stream().filter(m -> m.getName().equals(modelName)).findFirst();
        return first.orElse(null);
    }

    /**
     * get filtered predefined models of the provider for the type.
     *
     * @param providerName
     * @param modelType
     * @return
     */
    static Collection<ModelMeta> getFilteredPreDefinedModels(String providerName, int modelType) {
        return PROVIDER_MODELS.get(providerName)
                .stream().filter(mm -> mm.getType() == modelType).toList();
    }

    MultiValuedMap<String, ModelMeta> PROVIDER_MODELS = new HashSetValuedHashMap<>() {
        {
            // GPT https://platform.openai.com/docs/models
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-5", 128000));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-5-mini", 128000));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-5-nano", 128000));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-5-chat-latest", 16384));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4.1", 32768));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4.1-mini", 32768));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4.1-nano", 32768));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4", 8192));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4-turbo", 4096));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4o", 16384));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-4o-mini", 16384));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-3.5-turbo", 4096));
            put(GenAiModelProvider.OPEN_AI.name(), new ModelMeta("gpt-3.5-turbo-instruct", 4096));

            // Gemini https://ai.google.dev/gemini-api/docs/models/gemini
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-2.5-pro", 65536));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-2.5-flash", 65536));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-2.5-flash-lite", 65536));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-2.0-flash", 8192));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-2.0-flash-lite", 8192));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-1.5-pro", 8192));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-1.5-flash", 8192));
            put(GenAiModelProvider.GEMINI.name(), new ModelMeta("gemini-1.5-flash-8b", 8192));

            // Qwen https://help.aliyun.com/zh/model-studio/developer-reference/what-is-qwen-llm#267c7b3691v9k
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen3-max", 65536));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-14b-instruct-1m", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-7b-instruct-1m", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-72b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-32b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-14b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-7b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-3b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-1.5b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2.5-0.5b-instruct", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2-72b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2-57b-a14b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2-7b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2-1.5b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen2-0.5b-instruct", 6144));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-110b-chat", 8000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-72b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-32b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-14b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-7b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-1.8b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen1.5-0.5b-chat", 2000));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-72b-chat", 2000));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-14b-chat", 2000));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-7b-chat", 1500));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-1.8b-longcontext-chat", 2000));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-1.8b-chat", 2000));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-flash", 32768));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-max", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-max-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-plus", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-plus-latest", 32768));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-turbo", 16384));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-turbo-latest", 16384));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-omni-turbo", 2048));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-omni-turbo-latest", 2048));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-math-plus", 3072));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-math-plus-latest", 3072));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-math-turbo", 3072));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-math-turbo-latest", 3072));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-mt-plus", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-mt-turbo", 1024));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-coder-plus", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-coder-plus-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-coder-turbo", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-coder-turbo-latest", 8192));
            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMeta("qwen-omni-turbo", 2048));
            put(GenAiModelProvider.ALI_Q_WEN.name(),new  ModelMeta("qwen-long", 8192));
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMetaBuilder().name("text-embedding-v3").type(MODEL_TYPE_EMBEDDING).dimension(1024).langCode("zh_CN").build());
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMetaBuilder().name("text-embedding-v2").type(MODEL_TYPE_EMBEDDING).dimension(1536).langCode("zh_CN").build());
//            put(GenAiModelProvider.ALI_Q_WEN.name(), new ModelMetaBuilder().name("text-embedding-v1").type(MODEL_TYPE_EMBEDDING).dimension(1536).langCode("zh_CN").build());


            // ChatGLM https://www.bigmodel.cn/console/modelcenter/square
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4.5-airx", 98304));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4.5-air", 98304));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4.5-x", 98304));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4.5", 98304));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-z1-air", 32768));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-z1-airx", 32768));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-z1-flash", 32768));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-plus", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-airx", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-air", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-long", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-flashx", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-flash", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4-9b", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-4", 4095));
            put(GenAiModelProvider.CHAT_GLM.name(), new ModelMeta("glm-3-turbo", 4095));

            // DeepSeek https://api-docs.deepseek.com/zh-cn/quick_start/pricing
            put(GenAiModelProvider.DEEP_SEEK.name(), new ModelMeta("deepseek-chat", 8192));
            put(GenAiModelProvider.DEEP_SEEK.name(), new ModelMeta("deepseek-reasoner", 65536));

            // Moonshot https://platform.moonshot.cn/docs/introduction
            put(GenAiModelProvider.MOONSHOT.name(), new ModelMeta("kimi-latest", 131072)); // the max output tokens is dynamic, use the largest.
            put(GenAiModelProvider.MOONSHOT.name(), new ModelMeta("moonshot-v1-8k", 8192));
            put(GenAiModelProvider.MOONSHOT.name(), new ModelMeta("moonshot-v1-32k", 32768));
            put(GenAiModelProvider.MOONSHOT.name(), new ModelMeta("moonshot-v1-128k", 131072));

            // Internal
            put(GenAiModelProvider.INTERNAL.name(), new ModelMetaBuilder().name("BAAI/bge-small-en-v1.5").type(MODEL_TYPE_EMBEDDING).langCode("en").internal(true).downloadUrl("https://huggingface.co/Xenova/bge-small-en-v1.5/resolve/main").build());
            put(GenAiModelProvider.INTERNAL.name(), new ModelMetaBuilder().name("BAAI/bge-small-zh-v1.5").type(MODEL_TYPE_EMBEDDING).langCode("zh_CN").internal(true).downloadUrl("https://huggingface.co/Xenova/bge-small-zh-v1.5/resolve/main").build());
        }
    };

    // TBD
    MultiValuedMap<String, ModelMeta> INTERNAL_EMBEDDING_MODELS = new HashSetValuedHashMap<>() {
        {
            put("en", new ModelMetaBuilder().name("BAAI/bge-small-en-v1.5").langCode("en").dimension(384).downloadUrl("https://huggingface.co/Xenova/bge-small-en-v1.5/tree/main").build());
            put("zh_CN", new ModelMetaBuilder().name("BAAI/bge-small-zh-v1.5").langCode("zh_CN").dimension(512).downloadUrl("https://huggingface.co/Xenova/bge-small-zh-v1.5/tree/main").build());
        }
    };

    String[] SUPPORTED_EMBEDDING_FILE_TYPES = new String[]{"mmd", "md", "txt"};

    static Collection<ModelMeta> lookupModel(String langCode) {
        return INTERNAL_EMBEDDING_MODELS.get(langCode);
    }

    /**
     * Lookup language by language coed, eg: zh-CN returns "Simplified Chinese (China)"
     *
     * @param langCode
     * @return
     */
    static String lookupLanguage(String langCode) {
        Map<String, String> mapped = new Gson().fromJson(LANGS_JSON, JsonArray.class).asList()
                .stream().map(je -> (JsonObject) je).toList()
                .stream().collect(Collectors.toMap(jo -> jo.get("code").getAsString(), jo -> jo.get("name").getAsString()));
        return mapped.getOrDefault(langCode, "the same language that I just said to you.");
    }

    String LANGS_JSON = """
            [
                {"code": "as-is", "name": "the same language that I just said to you"},
                {"code": "en-US", "name": "American English"},
                {"code": "en-GB", "name": "British English"},
                {"code": "en-AU", "name": "Australian English"},
                {"code": "zh-CN", "name": "Simplified Chinese (China)"},
                {"code": "zh-TW", "name": "Traditional Chinese (Taiwan)"},
                {"code": "zh-HK", "name": "Traditional Chinese (Hong Kong)"},
                {
                    "code": "hi",
                    "name": "Hindi"
                },
                {"code": "es-ES", "name": "European Spanish"},
                {"code": "es-MX", "name": "Mexican Spanish"},
                {"code": "es-AR", "name": "Argentinian Spanish"},
                {"code": "fr-FR", "name": "European French"},
                {"code": "fr-CA", "name": "Canadian French"},
                {"code": "fr-BE", "name": "Belgian French"},
                {"code": "ar-SA", "name": "Saudi Arabic"},
                {"code": "ar-EG", "name": "Egyptian Arabic"},
                {"code": "ar-MA", "name": "Moroccan Arabic"},
                {
                    "code": "bn",
                    "name": "Bengali"
                },
                {
                    "code": "ru",
                    "name": "Russian"
                },
                {"code": "pt-PT", "name": "European Portuguese"},
                {"code": "pt-BR", "name": "Brazilian Portuguese"},
                {
                    "code": "id",
                    "name": "Indonesian"
                },
                {
                    "code": "ur",
                    "name": "Urdu"
                },
                {
                    "code": "ja",
                    "name": "Japanese"
                },
                {"code": "de-DE", "name": "Standard German"},
                {"code": "de-AT", "name": "Austrian German"},
                {"code": "de-CH", "name": "Swiss German"},
                {
                    "code": "sw",
                    "name": "Swahili"
                },
                {
                    "code": "mr",
                    "name": "Marathi"
                },
                {
                    "code": "te",
                    "name": "Telugu"
                },
                {
                    "code": "tr",
                    "name": "Turkish"
                },
                {
                    "code": "ta",
                    "name": "Tamil"
                },
                {
                    "code": "vi",
                    "name": "Vietnamese"
                },
                {
                    "code": "ko",
                    "name": "Korean"
                }
            ]
            """;

    enum ActionType {
        CANCEL, // cancel the generation
        KEEP, // keep the generated text
        DISCARD, // discard the generated text
        STOP, // Stop the generating
//        ABORT // Abort the summarizing
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

//    record ProviderInfo(String name, String model) {
//    }
}
