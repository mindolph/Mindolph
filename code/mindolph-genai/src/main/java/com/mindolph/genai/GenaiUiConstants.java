package com.mindolph.genai;

import javafx.util.Pair;

import java.util.Comparator;

/**
 * @since 1.11
 */
public interface GenaiUiConstants {

     Comparator<Pair<String, String>> MODEL_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
     Pair<String, String> MODEL_CUSTOM_ITEM = new Pair<>("Custom", "Custom");
}
