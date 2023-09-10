package com.mindolph.fx.data;

import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapConfigLegacy;
import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.ConnectorStyle;
import com.mindolph.mindmap.theme.CustomTheme;
import com.mindolph.mindmap.theme.MindMapTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.BeanUtils;

import java.util.Map;

/**
 * @author mindolph
 */
public class MigrationV1 implements Migration {

    private static final Logger log = LoggerFactory.getLogger(MigrationV1.class);

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void doFixing() {
        log.debug("Try to do fixing(v1)");

        // use pref 'mmd.drawBackground' to determine the fixing
        boolean preferenceExist = FxPreferences.getInstance().isPreferenceExist("mmd.drawBackground");
        if (!preferenceExist) {
            log.debug("No need to process");
            return;
        }

        MindMapConfigLegacy configLegacy = new MindMapConfigLegacy();
        configLegacy.loadFromPreferences();

        MindMapConfig newConfig = new MindMapConfig();
        newConfig.loadFromPreferences();
        MindMapTheme theme = new CustomTheme("default");
        // some new configs
        theme.setBorderType(BorderType.BOX);
        theme.setConnectorStyle(ConnectorStyle.POLYLINE);
        newConfig.setThemeName("default");
        newConfig.setTheme(theme);
        if (!newConfig.getUserThemes().contains("default")) {
            newConfig.getUserThemes().add("default");
        }

        Map<String, Object> properties = BeanUtils.forceGetProperties(configLegacy);
        BeanUtils.forceSetProperties(theme, properties);
        BeanUtils.forceSetProperties(newConfig, properties);
        newConfig.saveToPreferences();
    }

}
