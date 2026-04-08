package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.util.ConfigUtils;
import com.mindolph.core.constant.AiConstants;
import com.mindolph.core.constant.AiModelProvider;
import com.mindolph.core.constant.AiModelProvider.ProviderType;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.*;
import com.mindolph.core.util.GsonUtils;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.JsonUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDERS;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_VECTOR_STORE_PROVIDER_ACTIVE;
import static com.mindolph.core.constant.AiConstants.CUSTOM_MODEL_KEY;

/**
 * Manage llm config in java preference.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    private static LlmConfig ins;
    private final FxPreferences fxPreferences;

    private final Type collectionType = new TypeToken<Map<String, DatasetMeta>>() {
    }.getType();

    private final Type vectorStoreMetaType = new TypeToken<Map<String, VectorStoreMeta>>() {
    }.getType();

    public LlmConfig() {
        fxPreferences = FxPreferences.getInstance();
    }

    public static synchronized LlmConfig getIns() {
        if (ins == null) ins = new LlmConfig();
        return ins;
    }


    /**
     * all provider including pre-set ones and custom ones.
     * @return
     */
    public Map<String, ProviderMeta> loadAllProviderMetas() {
        List<AiModelProvider> preDefinedProviders = EnumUtils.getEnumList(AiModelProvider.class);
        Map<String, ProviderMeta> providerKeyMetaMap = this.loadConfiguredProviderMetas();
        // fill missing provides
        preDefinedProviders.forEach(p -> {
            // the name is the key of provider definition.
            if (providerKeyMetaMap.containsKey(p.name())) {
                // configured pre-defined providers
                ProviderMeta pm = providerKeyMetaMap.get(p.name());
                // fill missing name for old data??
                pm.setId(p.name());// workaround for no id is stored.
                pm.setName(p.getDisplayName()); // workaround for no name is stored(maybe).
                pm.setType(p.getType().name());
            }
            else {
                // custom providers
                ProviderMeta pm = new ProviderMeta(p.name(), p.getDisplayName(), ProviderType.CUSTOM.name());
                providerKeyMetaMap.put(p.name(), pm);
            }
        });

        for (String k : providerKeyMetaMap.keySet()) {
            if (preDefinedProviders.stream().noneMatch(p -> p.name().equals(k))) {
                providerKeyMetaMap.get(k).setType(ProviderType.CUSTOM.name());
            }
        }
        return providerKeyMetaMap;
    }

    /**
     * Load all configured LLM providers meta, no data if never setup.
     *
     * @return Provider name -> Provider meta
     */
    public Map<String, ProviderMeta> loadConfiguredProviderMetas() {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderMeta>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }

    public ProviderMeta loadProviderMeta(String providerId) {
        Map<String, ProviderMeta> providers = loadConfiguredProviderMetas();
        return providers.get(providerId);
    }

    public void removeCustomProvider(String providerId) {
        Map<String, ProviderMeta> providers = loadConfiguredProviderMetas();
        ProviderMeta pm = providers.get(providerId);
        if (pm.isCustom()) {
            providers.remove(providerId);
        }
        fxPreferences.savePreference(PrefConstants.GEN_AI_PROVIDERS, new Gson().toJson(providers));
    }

    /**
     * Save provider meta, if the provider already exists, it will be overwritten.
     *
     * @param providerId
     * @param providerMeta
     */
    public void saveProviderMeta(String providerId, ProviderMeta providerMeta) {
        Map<String, ProviderMeta> providerPropsMap = this.loadConfiguredProviderMetas();
        providerPropsMap.put(providerId, providerMeta);
        String json = new Gson().toJson(providerPropsMap);
        fxPreferences.savePreference(GEN_AI_PROVIDERS, json);
    }

    /**
     * Get filtered custom models of the provider for the type.
     *
     * @param providerName
     * @param modelType
     * @return
     */
    public Collection<ModelMeta> getFilteredCustomModels(String providerName, int modelType) {
        ProviderMeta providerMeta = this.loadProviderMeta(providerName);
        if (providerMeta != null) {
            List<ModelMeta> customModels = providerMeta.customModels();
            if (customModels != null && !customModels.isEmpty()) {
                customModels = customModels.stream().filter(mm -> mm.getType() == modelType).toList();
                return customModels;
            }
        }
        return null;
    }

    // Whether there are any specific type of models for the provider
    public boolean hasModelsForType(String providerName, int modelType) {
        return CollectionUtils.isNotEmpty(AiConstants.getFilteredPreDefinedModels(providerName, modelType))
                || CollectionUtils.isNotEmpty(this.getFilteredCustomModels(providerName, modelType));
    }


    public ModelMeta lookupModel(String providerName, String modelName) {
        ModelMeta modelMeta = AiConstants.lookupModelMeta(providerName, modelName);
        if (modelMeta == null) {
            modelMeta = this.lookupCustomModel(providerName, modelName);
        }
        return modelMeta;
    }

    public ModelMeta lookupCustomModel(String providerId, String modelName) {
        ProviderMeta providerMeta = this.loadProviderMeta(providerId);
        if (providerMeta != null) {
            for (ModelMeta modelMeta : providerMeta.customModels()) {
                if (modelMeta.getName().equals(modelName)) {
                    return modelMeta;
                }
            }
        }
        return null;
    }


    public AgentMeta loadAgent(String id) {
        return loadAgents().get(id);
    }

    public Map<String, AgentMeta> loadAgents() {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_AGENTS, "{}");
        Type collectionType = new TypeToken<Map<String, AgentMeta>>() {
        }.getType();
        return GsonUtils.newGson().fromJson(json, collectionType);
    }

    public boolean saveAgent(AgentMeta agentMeta) {
        Map<String, AgentMeta> agentMap = loadAgents();
        if (agentMap.values().stream().anyMatch(a -> a.getName().equals(agentMeta.getName()))) {
            return false;
        }
        agentMap.put(String.valueOf(UUID.randomUUID()), agentMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, new Gson().toJson(agentMap));
        return true;
    }

    public void saveAgent(String agentId, AgentMeta agentMeta) {
        if (StringUtils.isBlank(agentId) || agentMeta == null) {
            throw new IllegalStateException("Agent id or agent is null");
        }
        Map<String, AgentMeta> agentMap = loadAgents();
        agentMeta.setId(agentId);
        agentMap.put(agentId, agentMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, GsonUtils.newGson().toJson(agentMap));
    }

    public void removeAgent(String agentId) {
        Map<String, AgentMeta> agentMap = loadAgents();
        agentMap.remove(agentId);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, new Gson().toJson(agentMap));
    }

    public List<DatasetMeta> getDatasetsFromAgentId(String agentId) {
        AgentMeta agentMeta = this.loadAgent(agentId);
        return this.getDatasetsFromIds(agentMeta.getDatasetIds());
    }

    public List<DatasetMeta> getDatasetsFromIds(List<String> datasetIds) {
        Map<String, DatasetMeta> datasetMap = loadAllDatasets();
        // the ids and the dataset metas might don't match in development.
        return datasetIds.stream().map(datasetMap::get).filter(Objects::nonNull).toList();
    }

    public Map<String, DatasetMeta> loadAllDatasets() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(ConfigUtils.datasetConfigFile()), StandardCharsets.UTF_8))) {
            Object o = GsonUtils.newGson().fromJson(reader, collectionType);
            if (o == null) {
                return new HashMap<>();
            }
            return (Map<String, DatasetMeta>) o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDataset(String datasetId, DatasetMeta datasetMeta) {
        if (StringUtils.isBlank(datasetId) || datasetMeta == null) {
            throw new IllegalStateException("Dataset id or dataset is null");
        }
        log.debug("save dataset {}", JsonUtils.object2PrettyJson(datasetMeta));
        Map<String, DatasetMeta> datasetMap = this.loadAllDatasets();
        datasetMeta.setId(datasetId);
        datasetMap.put(datasetId, datasetMeta);
        String json = JsonUtils.object2PrettyJson(datasetMap);
//        String json = GsonUtils.newGson().toJson(datasetMap, collectionType);
        try {
            IOUtils.write(json, new FileOutputStream(ConfigUtils.datasetConfigFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeDataset(DatasetMeta datasetMeta) {
        log.debug("remove dataset {}", datasetMeta.getId());
        Map<String, DatasetMeta> datasetMap = this.loadAllDatasets();
        datasetMap.remove(datasetMeta.getId());
        String json = GsonUtils.newGson().toJson(datasetMap, collectionType);
        try {
            IOUtils.write(json, new FileOutputStream(ConfigUtils.datasetConfigFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VectorStoreMeta loadActiveVectorStorePrefs() {
        String activeVectorStoreProvider = fxPreferences.getPreference(GEN_AI_VECTOR_STORE_PROVIDER_ACTIVE, String.class);
        if (StringUtils.isBlank(activeVectorStoreProvider)) {
            return null;
        }
        return loadVectorStorePrefs(VectorStoreProvider.valueOf(activeVectorStoreProvider));
    }

    public VectorStoreMeta loadVectorStorePrefs(VectorStoreProvider provider) {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, "{}");
        Map<String, VectorStoreMeta> map = GsonUtils.newGson().fromJson(json, vectorStoreMetaType);
        VectorStoreMeta vectorStoreMeta = map.get(provider.name());
        if (vectorStoreMeta != null) {
            vectorStoreMeta.setProvider(provider);
        }
        return vectorStoreMeta;
    }

    public void saveVectorStorePrefs(VectorStoreProvider provider, VectorStoreMeta vectorStoreMeta) {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, "{}");
        Map<String, VectorStoreMeta> map = GsonUtils.newGson().fromJson(json, vectorStoreMetaType);
        map.put(provider.name(), vectorStoreMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, GsonUtils.newGson().toJson(map));
    }


    /**
     * Used for migration only.
     *
     * @return
     */
    @Deprecated(since = "1.13")
    public Tuple2<AiModelProvider, String> getActiveProviderMeta() {
        String providerName = fxPreferences.getPreference(PrefConstants.GEN_AI_PROVIDER_ACTIVE, String.class);
        if (StringUtils.isBlank(providerName)) {
            return null;
        }
        ProviderMeta providerMeta = this.loadProviderMeta(providerName);
        if (providerMeta != null) {
            if (StringUtils.isBlank(providerMeta.aiModel()) || CUSTOM_MODEL_KEY.equals(providerMeta.aiModel())) {
                Optional<ModelMeta> opt = providerMeta.customModels().stream().filter(ModelMeta::active).findFirst();
                return opt.map(modelMeta -> new Tuple2<>(AiModelProvider.valueOf(providerName), modelMeta.getName())).orElse(null);
            }
            else {
                return new Tuple2<>(AiModelProvider.valueOf(providerName), providerMeta.aiModel());
            }
        }
        return null;
    }

    /**
     * Activate a custom model directly for active provider.
     *
     * @param provider
     * @param modelMeta
     * @since 1.11
     * @deprecated
     */
    @Deprecated(since = "1.13")
    public void activateCustomModel(AiModelProvider provider, ModelMeta modelMeta) {
        ProviderMeta providerMeta = this.loadProviderMeta(provider.name());
        for (ModelMeta customModel : providerMeta.customModels()) {
            customModel.setActive(customModel.getName().equals(modelMeta.getName()));
        }
        saveProviderMeta(provider.name(), providerMeta);
    }
}
