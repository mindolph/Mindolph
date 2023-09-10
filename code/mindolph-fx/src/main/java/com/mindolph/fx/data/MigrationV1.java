package com.mindolph.fx.data;

import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapConfigLegacy;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.ConnectorStyle;
import com.mindolph.mindmap.theme.CustomTheme;
import com.mindolph.mindmap.theme.MindMapTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mindolph
 */
public class MigrationV1 implements Migration {

    private static final Logger log = LoggerFactory.getLogger(MigrationV1.class);

    String PREFIX = "%s.default".formatted(MindMapConstants.CFG_PREFIX);

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void doFixing() {
        log.debug("Do fixing(v1)");

        // use pref 'mmd.drawBackground' to determine the fixing
        boolean preferenceExist = FxPreferences.getInstance().isPreferenceExist("mmd.drawBackground");
        if (!preferenceExist){
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

        Map<String, Object> properties = this.getProperties(configLegacy);
        setProperties(properties, theme);
        setProperties(properties, newConfig);
        newConfig.saveToPreferences();

    }

    private Map<String, Object> getProperties(Object source) {
        Collection<Field> allFields = BeanUtils.getAllFields(source.getClass());
        Map<String, Object> ret = new LinkedHashMap<>();
        for (Field f : allFields) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            Object v = BeanUtils.forceGetProperty(source, f);
            ret.put(f.getName(), v);
        }
        return ret;
    }

    private void setProperties(Map<String, Object> properties, Object target) {
        Collection<Field> allFields = BeanUtils.getAllFields(target.getClass());
        for (Field f : allFields) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            Object v = properties.get(f.getName());
            if (v == null){
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(target, v);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
