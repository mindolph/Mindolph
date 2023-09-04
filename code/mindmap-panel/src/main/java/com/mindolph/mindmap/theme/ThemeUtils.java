package com.mindolph.mindmap.theme;

import static com.mindolph.mindmap.theme.ThemeType.*;

/**
 * @author allen
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

}
