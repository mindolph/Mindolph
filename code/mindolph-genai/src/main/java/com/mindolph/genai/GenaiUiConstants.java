package com.mindolph.genai;

import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.ModelMeta;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.util.Comparator;

/**
 * @since 1.11
 */
public interface GenaiUiConstants {

    enum MessageType {
        HUMAN, AI
    }

    //     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    Comparator<Pair<String, ModelMeta>> MODEL_COMPARATOR =
            (o1, o2) -> o1.getValue().name().compareTo(o2.getValue().name());

    Pair<String, ModelMeta> MODEL_CUSTOM_ITEM = new Pair<>("Custom", new ModelMeta("Custom", 0));

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
            return object == null ? "" : object.getValue().name();
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
