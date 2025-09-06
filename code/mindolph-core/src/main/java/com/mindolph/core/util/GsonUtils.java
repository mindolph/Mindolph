package com.mindolph.core.util;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

/**
 * @since 1.13.0
 */
public class GsonUtils {

    public static Gson newGson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(File.class, new FileSerializer());
        return builder.create();
    }


    static class FileSerializer implements JsonSerializer<File>, JsonDeserializer<File> {

        @Override
        public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonNull() ? null : new File(json.getAsString());
        }

        @Override
        public JsonElement serialize(File json, Type typeOfSrc, JsonSerializationContext context) {
            return json == null ? JsonNull.INSTANCE : new JsonPrimitive(json.getPath());
        }
    }
}
