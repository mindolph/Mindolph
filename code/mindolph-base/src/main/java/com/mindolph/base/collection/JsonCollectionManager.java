package com.mindolph.base.collection;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.util.ConfigUtils;
import com.mindolph.core.util.GsonUtils;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_COLLECTION_ACTIVE;

/**
 * @since 1.13.3
 */
public class JsonCollectionManager {

    private static final JsonCollectionManager ins = new JsonCollectionManager();

    private final FxPreferences fxPreferences = FxPreferences.getInstance();

    private final Type collectionType = new TypeToken<Map<String, List<String>>>() {
    }.getType();

    public static JsonCollectionManager getIns() {
        return ins;
    }

    private JsonCollectionManager() {
    }


    public String getActiveCollectionName() {
        return fxPreferences.getPreference(MINDOLPH_COLLECTION_ACTIVE, "default");
    }

    /**
     * Switch the active collection.
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ConfigUtils.collectionConfigFile()), StandardCharsets.UTF_8))) {
            Map<String, List<String>> o = GsonUtils.newGson().fromJson(reader, collectionType);
            if (o == null) {
                return new HashMap<>();
            }
            return o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * Rename a collection by new name (which does not equal old name)
     *
     * @param oldName
     * @param newName
     */
    public void renameCollection(String oldName, String newName) {
        if (Strings.CS.equals(oldName, newName)) return;
        Map<String, List<String>> collectionMap = this.getFileCollectionMap();
        collectionMap.put(newName, collectionMap.get(oldName));
        collectionMap.remove(oldName);
        this.saveToConfigFile(new Gson().toJson(collectionMap));
    }

    /**
     * Save file paths to the collection.
     * If connection name does not exist, new one will be created.
     *
     * @param collectionName
     * @param files
     */
    public void saveCollectionFilePaths(String collectionName, List<String> files) {
        Map<String, List<String>> collectionMap = this.getFileCollectionMap();
        collectionMap.put(collectionName, files);
        this.saveToConfigFile(new Gson().toJson(collectionMap));
    }

    /**
     * Update any collection that contains the old file path with the new file path.
     * If newFilePath is null or blank, just remove the file.
     *
     * @param oldFilePath
     * @param newFilePath
     */
    public void updateFilePath(String oldFilePath, String newFilePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ConfigUtils.collectionConfigFile()), StandardCharsets.UTF_8))) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonObject collDict = root.getAsJsonObject();
            AtomicBoolean isChanged = new AtomicBoolean(false);
            collDict.keySet().forEach(collName -> {
                JsonArray paths = collDict.get(collName).getAsJsonArray();
                if (paths.remove(new JsonPrimitive(oldFilePath))) {
                    if (!StringUtils.isBlank(newFilePath)) {
                        paths.add(new JsonPrimitive(newFilePath));
                    }
                    isChanged.set(true);
                }
            });
            if (isChanged.get()) {
                // save json object to json config file
                String newJson = new Gson().toJson(root);
                this.saveToConfigFile(newJson);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        this.saveToConfigFile(new Gson().toJson(collectionMap));
    }

    private void saveToConfigFile(String json) {
        try {
            IOUtils.write(json, new FileOutputStream(ConfigUtils.collectionConfigFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
