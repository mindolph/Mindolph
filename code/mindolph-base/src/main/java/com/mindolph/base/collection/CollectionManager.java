package com.mindolph.base.collection;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mindolph.mfx.preference.FxPreferences;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_COLLECTION_ACTIVE;
import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_COLLECTION_MAP;

/**
 * Manage file collections.
 * <p>
 * TODO move to module core once the FxPreferences is replaced with abstract one.
 * @since 1.9
 */
public class CollectionManager {

    private static final CollectionManager ins = new CollectionManager();

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    public static CollectionManager getIns() {
        return ins;
    }

    public String getActiveCollectionName() {
        return fxPreferences.getPreference(MINDOLPH_COLLECTION_ACTIVE, "default");
    }

    /**
     * Switch active collection.
     *
     * @param activeCollectionName
     */
    public void saveActiveCollectionName(String activeCollectionName) {
        fxPreferences.savePreference(MINDOLPH_COLLECTION_ACTIVE, activeCollectionName);
    }

    /**
     * Get all file paths from all collections
     *
     * @return
     */
    public Map<String, List<String>> getFileCollectionMap() {
        String strCollectionMap = fxPreferences.getPreference(MINDOLPH_COLLECTION_MAP, "{}");
        Type collectionType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        Map<String, List<String>> result = new Gson().fromJson(strCollectionMap, collectionType);
        return result == null ? new HashMap<>() : result;
    }

    /**
     * Get all file paths from the collection.
     *
     * @param collectionName
     * @return
     */
    public List<String> getCollectionFilePaths(String collectionName) {
        Map<String, List<String>> collectionMap = this.getFileCollectionMap();
        return collectionMap.get(collectionName);
    }

    /**
     * Save file paths to the collection.
     *
     * @param collectionName
     * @param files
     */
    public void saveCollectionFilePaths(String collectionName, List<String> files) {
        Map<String, List<String>> collectionMap = this.getFileCollectionMap();
        collectionMap.put(collectionName, files);
        fxPreferences.savePreference(MINDOLPH_COLLECTION_MAP, new Gson().toJson(collectionMap));
    }

    /**
     * Update any collection that contains the old file path with the new file path.
     *
     * @param oldFilePath
     * @param newFilePath
     */
    public void updateFilePath(String oldFilePath, String newFilePath) {
        String strCollectionMap = fxPreferences.getPreference(MINDOLPH_COLLECTION_MAP, "{}");
        JsonElement root = JsonParser.parseString(strCollectionMap);
        JsonObject collDict = root.getAsJsonObject();
        AtomicBoolean isChanged = new AtomicBoolean(false);
        collDict.keySet().forEach(collName -> {
            JsonArray paths = collDict.get(collName).getAsJsonArray();
            if (paths.remove(new JsonPrimitive(oldFilePath))) {
                paths.add(new JsonPrimitive(newFilePath));
                isChanged.set(true);
            }
        });
        if (isChanged.get()) {
            // save json object to json preferences
            String newJson = new Gson().toJson(root);
            fxPreferences.savePreference(MINDOLPH_COLLECTION_MAP, newJson);
        }
    }

    /**
     * Delete the collection.
     *
     * @param collectionName
     */
    public void deleteCollection(String collectionName) {
        Map<String, List<String>> collectionMap = this.getFileCollectionMap();
        collectionMap.remove(collectionName);
        fxPreferences.savePreference(MINDOLPH_COLLECTION_MAP, new Gson().toJson(collectionMap));
    }

}
