package com.mindolph.base.genai;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.GenAiEvents.OutputAdjust;
import com.mindolph.core.constant.GenAiModelVendor;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GENERAL_AI_VENDOR_PROPS;

/**
 * @author mindolph.com@gmail.com
 */
public class LlmService {

    private static final LlmService ins = new LlmService();
    private FxPreferences fxPreferences;
    private LlmService() {
         fxPreferences = FxPreferences.getInstance();
    }

    public static synchronized LlmService getIns() {
        return ins;
    }

    public String predict(String input, float temperature, OutputAdjust outputAdjust) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String chatId = RandomStringUtils.randomAlphabetic(10);
        String generated = """
                [%s](%.1f)
                Hi, I'm AI assistant,
                ask me anything you want.
                I can do more.""".formatted(chatId, temperature);
        if (outputAdjust == OutputAdjust.SHORTER) {
            return StringUtils.substring(generated, 0, StringUtils.lastIndexOf(generated, '\n') + 1);
        }
        else if (outputAdjust == OutputAdjust.LONGER) {
            return generated + "\nI can do more.";
        }
        else {
            return generated;
        }
    }

    public void saveGenAiVendors(GenAiModelVendor vendor, PrefConstants.VendorProps vendorProps) {
        Map<String, PrefConstants.VendorProps> vendorPropsMap = this.loadGenAiVendors();
        vendorPropsMap.put(vendor.getName(), vendorProps);
        String json = new Gson().toJson(vendorPropsMap);
        fxPreferences.savePreference(GENERAL_AI_VENDOR_PROPS, json);
    }

    public Map<String, PrefConstants.VendorProps> loadGenAiVendors() {
        String json = fxPreferences.getPreference(PrefConstants.GENERAL_AI_VENDOR_PROPS, "{}");
        Type collectionType = new TypeToken<Map<String, PrefConstants.VendorProps>>(){}.getType();
        Map<String, PrefConstants.VendorProps> map = new Gson().fromJson(json, collectionType);
        return map;
    }
}
