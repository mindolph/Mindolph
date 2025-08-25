package com.mindolph.genai;

import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.*;
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

    StringConverter<Pair<VectorStoreProvider, String>> vectorStoreConverter = new StringConverter<>() {

        @Override
        public String toString(Pair<VectorStoreProvider, String> object) {
            return object == null ? "" : object.getKey().getDisplayName();
        }

        @Override
        public Pair<VectorStoreProvider, String> fromString(String s) {
            return null;
        }
    };

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

    StringConverter<Pair<String, ModelMeta>> modelMetaConverter = new ModelMetaConverter();

    StringConverter<Pair<GenAiModelProvider, String>> modelProviderConverter = new ProviderConverter();

    class ProviderConverter extends StringConverter<Pair<GenAiModelProvider, String>> {
        @Override
        public String toString(Pair<GenAiModelProvider, String> object) {
            return object == null ? "" : object.getValue();
        }

        @Override
        public Pair<GenAiModelProvider, String> fromString(String string) {
            return null;
        }
    }

    class ModelMetaConverter extends StringConverter<Pair<String, ModelMeta>> {
        @Override
        public String toString(Pair<String, ModelMeta> object) {
            return object == null ? "" : object.getValue().getName();
        }

        @Override
        public Pair<String, ModelMeta> fromString(String string) {
            return null;
        }
    }
}
