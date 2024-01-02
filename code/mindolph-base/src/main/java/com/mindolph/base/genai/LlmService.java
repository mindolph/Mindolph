package com.mindolph.base.genai;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.constant.PrefConstants.VendorProps;
import com.mindolph.base.genai.GenAiEvents.OutputAdjust;
import com.mindolph.base.genai.llm.DummyLlmProvider;
import com.mindolph.base.genai.llm.LlmProvider;
import com.mindolph.base.genai.llm.OpenAiProvider;
import com.mindolph.core.constant.GenAiModelVendor;
import com.mindolph.mfx.preference.FxPreferences;

import java.lang.reflect.Type;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GENERAL_AI_VENDOR_PROPS;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmService {

    private static final LlmService ins = new LlmService();
    private FxPreferences fxPreferences;
    private LlmProvider llmProvider;

    private LlmService() {
        fxPreferences = FxPreferences.getInstance();
        Map<String, VendorProps> map = this.loadGenAiVendors();
        if (Boolean.parseBoolean(System.getenv("mock-llm"))) {
            llmProvider = new DummyLlmProvider();
        }
        else {
            VendorProps props = map.get(GenAiModelVendor.OPEN_AI.getName());
            llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel());
        }
    }

    public static synchronized LlmService getIns() {
        return ins;
    }

    public String predict(String input, float temperature, OutputAdjust outputAdjust) {
        return llmProvider.predict(input, temperature, outputAdjust);
    }

    /**
     * @param vendor
     * @param vendorProps
     */
    public void saveGenAiVendors(GenAiModelVendor vendor, VendorProps vendorProps) {
        Map<String, VendorProps> vendorPropsMap = this.loadGenAiVendors();
        vendorPropsMap.put(vendor.getName(), vendorProps);
        String json = new Gson().toJson(vendorPropsMap);
        fxPreferences.savePreference(GENERAL_AI_VENDOR_PROPS, json);
    }

    /**
     * @return
     */
    public Map<String, VendorProps> loadGenAiVendors() {
        String json = fxPreferences.getPreference(PrefConstants.GENERAL_AI_VENDOR_PROPS, "{}");
        Type collectionType = new TypeToken<Map<String, VendorProps>>() {
        }.getType();
        Map<String, VendorProps> map = new Gson().fromJson(json, collectionType);
        return map;
    }
}
