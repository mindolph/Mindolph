package com.mindolph.genai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mindolph.core.constant.AiModelProvider;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import org.swiftboot.util.I18nHelper;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * @since 1.11
 */
public interface AiUiConstants {

    enum MessageType {
        HUMAN, AI
    }

    // be used to notify vector db config has been changed.
    String PAYLOAD_VECTOR_DB = "vector_db";

    Comparator<ModelMeta> MODEL_META_COMPARATOR = Comparator.comparing(ModelMeta::getName);

    //     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    Comparator<Pair<String, ModelMeta>> MODEL_COMPARATOR =
            (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

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
        public String toString(Pair<String, AgentMeta> pair) {
            return pair == null ? "" : pair.getValue().getName();
        }

        @Override
        public Pair<String, AgentMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<String, ModelMeta>> modelMetaConverter = new ModelMetaConverter();

    StringConverter<Pair<String, ProviderMeta>> aiProviderConverter = new AiProviderConverter();

    class AiProviderConverter extends StringConverter<Pair<String, ProviderMeta>> {
        @Override
        public String toString(Pair<String, ProviderMeta> pair) {
            return (pair == null || pair.getValue() == null) ? "" : AiModelProvider.ProviderType.INTERNAL.name().equals(pair.getValue().getType())
                    ? I18nHelper.getInstance().get("prefs.ai.provider.internal")
                    : pair.getValue().getName();
        }

        @Override
        public Pair<String, ProviderMeta> fromString(String string) {
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


    /**
     * for embedding model.
     *
     * @param langCode
     * @return
     */
    static String lookupLanguage(String langCode) {
        if (StringUtils.isBlank(langCode)) {
            return "Unknown";
        }
        if ("all".equals(langCode)) {
            return I18nHelper.getInstance().get("prefs.ai.embedding.lang.all");
        }
        JsonArray langs = new Gson().fromJson(LANGUAGES_IN_JSON, JsonArray.class);
        for (JsonElement lang : langs) {
            if (lang.getAsJsonObject().get("code").getAsString().equals(langCode)) {
                return lang.getAsJsonObject().get("name").getAsString();
            }
        }
        return langCode;
    }

    // Languages code and name mapping for embedding.
    String LANGUAGES_IN_JSON = """
            [
                {"code": "en", "name": "prefs.ai.embedding.lang.en"},
                {"code": "zh", "name": "prefs.ai.embedding.lang.zh"},
                {
                    "code": "hi",
                    "name": "prefs.ai.embedding.lang.hi"
                },
                {"code": "es", "name": "prefs.ai.embedding.lang.es"},
                {"code": "fr", "name": "prefs.ai.embedding.lang.fr"},
                {"code": "ar", "name": "prefs.ai.embedding.lang.ar"},
                {
                    "code": "bn",
                    "name": "prefs.ai.embedding.lang.bn"
                },
                {
                    "code": "ru",
                    "name": "prefs.ai.embedding.lang.ru"
                },
                {"code": "pt", "name": "prefs.ai.embedding.lang.pt"},
                {
                    "code": "id",
                    "name": "prefs.ai.embedding.lang.id"
                },
                {
                    "code": "ur",
                    "name": "prefs.ai.embedding.lang.ur"
                },
                {
                    "code": "ja",
                    "name": "prefs.ai.embedding.lang.ja"
                },
                {"code": "de", "name": "prefs.ai.embedding.lang.de"},
                {
                    "code": "mr",
                    "name": "prefs.ai.embedding.lang.mr"
                },
                {
                    "code": "te",
                    "name": "prefs.ai.embedding.lang.te"
                },
                {
                    "code": "tr",
                    "name": "prefs.ai.embedding.lang.tr"
                },
                {
                    "code": "ta",
                    "name": "prefs.ai.embedding.lang.ta"
                },
                {
                    "code": "vi",
                    "name": "prefs.ai.embedding.lang.vi"
                },
                {
                    "code": "ko",
                    "name": "prefs.ai.embedding.lang.ko"
                }
            ]
            """;

}
