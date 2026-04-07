package com.mindolph.base.genai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.swiftboot.util.ClasspathResourceUtils;

/**
 * @since 1.13.0
 */
public class BaseLlmTest {
    protected JsonObject props;

    @BeforeEach
    public void setup() {
        FxPreferences.getInstance().init(BaseLlmTest.class);

        FxPreferences.getInstance().savePreference("general.proxy.enable", "false"); // disable by default.
        FxPreferences.getInstance().savePreference("general.proxy.type", "HTTP");
        FxPreferences.getInstance().savePreference("general.proxy.host", "192.168.1.66");
        FxPreferences.getInstance().savePreference("general.proxy.port", "8080");

        String json = ClasspathResourceUtils.readResourceToString("api_keys.json");

        if (StringUtils.isNotBlank(json)) {
            props = JsonParser.parseString(json).getAsJsonObject();
        }
    }

    protected void enableProxy() {
        FxPreferences.getInstance().savePreference("general.proxy.enable", "true");
    }

    protected void disableProxy() {
        FxPreferences.getInstance().savePreference("general.proxy.enable", "false");
    }

    protected String loadApiKeyForTesting(String providerName) {
        if (props.has(providerName)) {
            return props.get(providerName).getAsJsonObject().get("apiKey").getAsString();
        }
        return SystemUtils.getEnvironmentVariable("API_KEY", "NONE");
    }

    protected ProviderMeta loadProviderMeta(String providerName, boolean useProxy) {
        return new ProviderMeta(loadApiKeyForTesting(providerName), null,null, useProxy, null);
    }

    protected ProviderMeta loadProviderMeta(String providerName, String baseUrl,  boolean useProxy) {
        return new ProviderMeta(loadApiKeyForTesting(providerName), baseUrl, null, useProxy, null);
    }

    protected ModelMeta loadModelMeta(String modelName) {
        return new ModelMeta(modelName, null, null);
    }

}
