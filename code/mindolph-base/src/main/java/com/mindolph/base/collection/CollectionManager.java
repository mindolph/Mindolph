package com.mindolph.base.collection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.mfx.preference.FxPreferences;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void saveActiveCollectionName(String activeCollectionName) {
        fxPreferences.savePreference(MINDOLPH_COLLECTION_ACTIVE, activeCollectionName);
    }

    public Map<String, List<String>> getFileCollectionMap() {
        String strCollectionMap = fxPreferences.getPreference(MINDOLPH_COLLECTION_MAP, "{}");
        Type collectionType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        Map<String, List<String>> result = new Gson().fromJson(strCollectionMap, collectionType);
        return result == null ? new HashMap<>() : result;
    }

    public List<String> getCollectionFilePaths(String collectionName) {
        Map<String, List<String>> collectionMap = getFileCollectionMap();
        return collectionMap.get(collectionName);
    }

    public void saveCollectionFilePaths(String collectionName, List<String> files) {
        Map<String, List<String>> collectionMap = getFileCollectionMap();
        collectionMap.put(collectionName, files);
        fxPreferences.savePreference(MINDOLPH_COLLECTION_MAP, new Gson().toJson(collectionMap));
    }

    public void deleteCollection(String collectionName) {
        Map<String, List<String>> collectionMap = getFileCollectionMap();
        collectionMap.remove(collectionName);
        fxPreferences.savePreference(MINDOLPH_COLLECTION_MAP, new Gson().toJson(collectionMap));
    }

}
