package com.mindolph.mindmap.theme;

import org.swiftboot.util.I18nHelper;

import static com.mindolph.mindmap.theme.ThemeType.*;

/**
 * @author mindolph
 */
public class ThemeUtils {

    public static MindMapTheme createTheme(String themeName) {
        if (CLASSIC.name().equals(themeName)) {
            return new ClassicTheme();
        }
        else if (LIGHT.name().equals(themeName)) {
            return new LightTheme();
        }
        else if (DARK.name().equals(themeName)) {
            return new DarkTheme();
        }
        else {
            return new CustomTheme(themeName);
        }
    }


    public static String themeLabel(String themeName) {
        I18nHelper i18n = I18nHelper.getInstance();
        if (CLASSIC.name().equals(themeName)) {
            return i18n.get("mindmap.theme.label.classic");
        }
        else if (LIGHT.name().equals(themeName)) {
            return i18n.get("mindmap.theme.label.light");
        }
        else if (DARK.name().equals(themeName)) {
            return i18n.get("mindmap.theme.label.dark");
        }
        else {
            return themeName;
        }
    }

    public static String connectorTypeLabel(ConnectorStyle connectorType) {
        if (connectorType == null) {
            connectorType = ConnectorStyle.BEZIER;
        }
        return connectorTypeLabel(connectorType.name());
    }

    public static String connectorTypeLabel(String connectorTypeName) {
        if (ConnectorStyle.BEZIER.name().equals(connectorTypeName)) {
            return I18nHelper.getInstance().get("mindmap.connector.style.bezier");
        }
        else if (ConnectorStyle.POLYLINE.name().equals(connectorTypeName)) {
            return I18nHelper.getInstance().get("mindmap.connector.style.polyline");
        }
        else {
            return connectorTypeName;
        }
    }

}
