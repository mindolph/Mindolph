package com.mindolph.fx.util;

import com.mindolph.mfx.preference.FxPreferences;
import org.junit.jupiter.api.Test;

class MetricsUtilsTest {

    @Test
    public void launch() {
        FxPreferences.getInstance().init(MetricsUtilsTest.class); // for saving the metrics id
        MetricsUtils.launch();
    }
}