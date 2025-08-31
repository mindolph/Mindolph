package com.mindolph.fx.data;

import com.google.gson.Gson;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.preference.FxPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDERS;
import static com.mindolph.core.constant.SceneStatePrefs.GEN_AI_PROVIDER_ACTIVE;

/**
 * Fix gen-ai settings
 * @since 1.12.5
 */
public class MigrationV3 implements Migration {


    private static final Logger log = LoggerFactory.getLogger(MigrationV3.class);

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void doMigration() {
        Map<String, ProviderMeta> ProviderMeta = LlmConfig.getIns().loadAllProviderMetas();
        Map<String, ProviderMeta> newProviderMeta = new HashMap<>();
        for (String k : ProviderMeta.keySet()) {
            ProviderMeta p = ProviderMeta.get(k);
            GenAiModelProvider provider = GenAiModelProvider.fromName(k);
            if (provider != null) {
                log.info("Convert provider from %s to %s".formatted(k, provider.name()));
                newProviderMeta.put(provider.name(), p);
            }
            else {
                // keep the correct ones just for re-run the migration.
                log.info("Keep the provider: %s".formatted(k));
                newProviderMeta.put(k, p);
            }
        }
        String json = new Gson().toJson(newProviderMeta);
        FxPreferences.getInstance().savePreference(GEN_AI_PROVIDERS, json);

        // it's the display name
        String activeProvider = FxPreferences.getInstance().getPreference(GEN_AI_PROVIDER_ACTIVE, String.class);
        if (activeProvider != null) {
            GenAiModelProvider provider = GenAiModelProvider.fromName(activeProvider);
            if (provider != null) {
                FxPreferences.getInstance().savePreference(GEN_AI_PROVIDER_ACTIVE, provider.name());
            }
        }
    }
}
