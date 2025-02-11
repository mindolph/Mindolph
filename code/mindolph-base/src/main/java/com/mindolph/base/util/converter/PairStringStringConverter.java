package com.mindolph.base.util.converter;

import javafx.util.Pair;
import javafx.util.StringConverter;

/**
 * @since 1.11.1
 */
public class PairStringStringConverter extends StringConverter<Pair<String, String>> {

    @Override
    public String toString(Pair<String, String> object) {
        return object == null ? "" : object.getValue();
    }

    @Override
    public Pair<String, String> fromString(String string) {
        return null;
    }
}
