package com.mindolph.base.genai.llm;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public abstract class BaseLlmProvider implements LlmProvider {

    protected boolean proxyEnabled = false;
    protected String proxyUrl;
    protected String proxyUser;
    protected String proxyPassword;

    public BaseLlmProvider() {
        FxPreferences fxPreferences = FxPreferences.getInstance();
        proxyEnabled = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_ENABLE, false);
        if (proxyEnabled) {
            String proxyType = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_TYPE, "HTTP");
            String host = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_HOST, "");
            String port = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PORT, "");
            proxyUrl = "%s://%s:%s".formatted(StringUtils.lowerCase(proxyType), host, port).trim();
            proxyUser = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_USERNAME, "");
            proxyPassword = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PASSWORD, "");
        }
    }
}
