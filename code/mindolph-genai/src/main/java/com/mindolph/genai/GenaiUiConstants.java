package com.mindolph.genai;

import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ModelMetaBuilder;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.Map;

/**
 * @since 1.11
 */
public interface GenaiUiConstants {

    enum MessageType {
        HUMAN, AI
    }

    //     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    Comparator<Pair<String, ModelMeta>> MODEL_COMPARATOR =
            (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

    Pair<String, ModelMeta> MODEL_CUSTOM_ITEM = new Pair<>("Custom", new ModelMetaBuilder().name("Custom").maxTokens(0).build());

    Map<String, String> SUPPORTED_EMBEDDING_LANG = Map.of("en", "English", "zh_CN", "Simplified Chinese");


    StringConverter<Pair<String, DatasetMeta>> datasetConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<String, DatasetMeta> object) {
            return object == null ? "" : object.getValue().getName();
        }

        @Override
        public Pair<String, DatasetMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<String, AgentMeta>> agentConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<String, AgentMeta> object) {
            return object == null ? "" : object.getValue().getName();
        }

        @Override
        public Pair<String, AgentMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<String, ModelMeta>> modelMetaConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<String, ModelMeta> object) {
            return object == null ? "" : object.getValue().getName();
        }

        @Override
        public Pair<String, ModelMeta> fromString(String string) {
            return null;
        }
    };

    StringConverter<Pair<GenAiModelProvider, String>> modelProviderConverter = new StringConverter<>() {
        @Override
        public String toString(Pair<GenAiModelProvider, String> object) {
            return object == null ? "" : object.getValue();
        }

        @Override
        public Pair<GenAiModelProvider, String> fromString(String string) {
            return null;
        }
    };
}
