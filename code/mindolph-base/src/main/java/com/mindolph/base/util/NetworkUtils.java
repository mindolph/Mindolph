package com.mindolph.base.util;

import com.mindolph.core.config.ProxyMeta;
import com.mindolph.mfx.preference.FxPreferences;

import static com.mindolph.base.constant.PrefConstants.*;

public class NetworkUtils {


    public static ProxyMeta getProxyMeta() {
        FxPreferences fxp = FxPreferences.getInstance();
        boolean proxyEnabled = fxp.getPreference(GENERAL_PROXY_ENABLE, false);
        if (proxyEnabled) {
            return new ProxyMeta(
                    fxp.getPreference(GENERAL_PROXY_TYPE, "HTTP"),
                    fxp.getPreference(GENERAL_PROXY_HOST, ""),
                    fxp.getPreference(GENERAL_PROXY_PORT, 0),
                    fxp.getPreference(GENERAL_PROXY_USERNAME, ""),
                    fxp.getPreference(GENERAL_PROXY_PASSWORD, "")
            );
        }
        else {
            return null;
        }
    }
}
