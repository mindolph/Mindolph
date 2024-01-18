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
    protected String proxyType;
    protected String proxyHost;
    protected int proxyPort;
    protected String proxyUser;
    protected String proxyPassword;

    public BaseLlmProvider() {
        FxPreferences fxPreferences = FxPreferences.getInstance();
        proxyEnabled = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_ENABLE, false);
        if (proxyEnabled) {
            proxyType = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_TYPE, "HTTP");
            proxyHost = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_HOST, "");
            proxyPort = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PORT, 0);
            proxyUrl = "%s://%s:%s".formatted(StringUtils.lowerCase(proxyType), proxyHost, proxyPort).trim();
            proxyUser = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_USERNAME, "");
            proxyPassword = fxPreferences.getPreference(PrefConstants.GENERAL_PROXY_PASSWORD, "");
        }
    }
}
