package com.mindolph.genai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ModelMetaBuilder;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * @since 1.11
 */
public interface GenaiUiConstants {

    enum MessageType {
        HUMAN, AI
    }

    // be used to notify vector db config has been changed.
    String PAYLOAD_VECTOR_DB = "vector_db";

    Comparator<ModelMeta> MODEL_META_COMPARATOR = Comparator.comparing(ModelMeta::getName);

    //     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    Comparator<Pair<String, ModelMeta>> MODEL_COMPARATOR =
            (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

    Pair<String, ModelMeta> MODEL_CUSTOM_ITEM = new Pair<>(GenAiConstants.CUSTOM_MODEL_KEY, new ModelMetaBuilder().name(GenAiConstants.CUSTOM_MODEL_KEY).maxTokens(0).build());

    StringConverter<Pair<VectorStoreProvider, String>> vectorStoreConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<VectorStoreProvider, String> pair) {
            return pair == null ? "" : pair.getKey().getDisplayName();
        }

        @Override
        public Pair<VectorStoreProvider, String> fromString(String s) {
            return null;
        }
    };

    StringConverter<Pair<String, DatasetMeta>> datasetConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<String, DatasetMeta> pair) {
            return pair == null ? "" : pair.getValue().getName();
        }

        @Override
        public Pair<String, DatasetMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<String, AgentMeta>> agentConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<String, AgentMeta> paire) {
            return paire == null ? "" : paire.getValue().getName();
        }

        @Override
        public Pair<String, AgentMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<String, ModelMeta>> modelMetaConverter = new ModelMetaConverter();

    StringConverter<Pair<GenAiModelProvider, String>> providerConverter = new ProviderConverter();

    class ProviderConverter extends StringConverter<Pair<GenAiModelProvider, String>> {
        @Override
        public String toString(Pair<GenAiModelProvider, String> pair) {
            return pair == null ? "" : pair.getValue();
        }

        @Override
        public Pair<GenAiModelProvider, String> fromString(String string) {
            return null;
        }
    }

    class ModelMetaConverter extends StringConverter<Pair<String, ModelMeta>> {
        @Override
        public String toString(Pair<String, ModelMeta> pair) {
            return pair == null ? "" : pair.getValue().getName();
        }

        @Override
        public Pair<String, ModelMeta> fromString(String string) {
            return null;
        }
    }

    static String lookupLanguage(String languageCode) {
        if (StringUtils.isBlank(languageCode)) {
            return "Unknown";
        }
        JsonArray langs = new Gson().fromJson(LANGUAGES_IN_JSON, JsonArray.class);
        for (JsonElement lang : langs) {
            if (lang.getAsJsonObject().get("code").getAsString().equals(languageCode)) {
                return lang.getAsJsonObject().get("name").getAsString();
            }
        }
        return languageCode;
    }

    // Languages code and name mapping for embedding.
    String LANGUAGES_IN_JSON = """
            [
                {"code": "en", "name": "English"},
                {"code": "zh", "name": "Chinese"},
                {
                    "code": "hi",
                    "name": "Hindi"
                },
                {"code": "es", "name": "Spanish"},
                {"code": "fr", "name": "French"},
                {"code": "ar", "name": "Arabic"},
                {
                    "code": "bn",
                    "name": "Bengali"
                },
                {
                    "code": "ru",
                    "name": "Russian"
                },
                {"code": "pt", "name": "Portuguese"},
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
                {"code": "de", "name": "German"},
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

}
