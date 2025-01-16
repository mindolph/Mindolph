package com.mindolph.genai;

import com.mindolph.core.constant.GenAiConstants.ModelMeta;
import javafx.util.Pair;

import java.util.Comparator;

/**
 * @since 1.11
 */
public interface GenaiUiConstants {

    //     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    Comparator<Pair<String, ModelMeta>> MODEL_COMPARATOR =
            (o1, o2) -> o1.getValue().name().compareTo(o2.getValue().name());
    Pair<String, ModelMeta> MODEL_CUSTOM_ITEM = new Pair<>("Custom", new ModelMeta("Custom", 0));
}
