package com.mindolph.mindmap.theme;

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
        if (CLASSIC.name().equals(themeName)) {
            return "Classic";
        }
        else if (LIGHT.name().equals(themeName)) {
            return "Light";
        }
        else if (DARK.name().equals(themeName)) {
            return "Dark";
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
            return "Bezier";
        }
        else if (ConnectorStyle.POLYLINE.name().equals(connectorTypeName)) {
            return "Polyline";
        }
        else {
            return connectorTypeName;
        }
    }

}
