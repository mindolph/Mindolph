package com.mindolph.base.util;

import com.mindolph.base.constant.FontConstants;
import com.mindolph.core.util.TemplateUtils;
import com.mindolph.core.util.UriUtils;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.FontUtils;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.mindolph.mfx.util.FontUtils.fontPosture;

/**
 * @since 1.8.1
 */
public class CssUtils {

    private static final Logger log = LoggerFactory.getLogger(CssUtils.class);

    public static void applyFontCss(Parent root, String fontKey) {
        Font defFont = FontConstants.DEFAULT_FONTS.get(fontKey);
        Font font = FxPreferences.getInstance().getPreference(fontKey, Font.class, defFont);
        root.setStyle(FontUtils.fontToCssStyle(font));
    }

    public static void applyFontCss(Parent root, String templateUri, String sansFontKey, String monoFontKey) {
        FxPreferences fxPreferences = FxPreferences.getInstance();
        try {
            String s = IoUtils.readAllToString(root.getClass().getResource(templateUri).openStream());
            Font defFont = FontConstants.DEFAULT_FONTS.get(sansFontKey);
            Font defFontMono = FontConstants.DEFAULT_FONTS.get(monoFontKey);
            Font fontSans = fxPreferences.getPreference(sansFontKey, Font.class, defFont);
            Font fontMono = fxPreferences.getPreference(monoFontKey, Font.class, defFontMono);
            log.debug("Apply sans font {} and Mono font {} ", fontSans, fontMono);
            String formatted = fontCss(s, fontSans, fontMono);
            String name = "editor_%d.css".formatted(root.hashCode());
            File file = new File(SystemUtils.getJavaIoTmpDir(), name);
            FileUtils.writeStringToFile(file, formatted, StandardCharsets.UTF_8);
            String resRui = UriUtils.filePathToResourceUriStr(file.getAbsolutePath());
            log.debug("load font css file: {}", resRui);
            root.getStylesheets().clear();
            root.getStylesheets().add(resRui);
            root.layout();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param cssTemplate
     * @param sansFont
     * @param monoFont
     * @return
     */
    public static String fontCss(String cssTemplate, Font sansFont, Font monoFont) {
        String formatted = TemplateUtils.format(cssTemplate,
                new String[]{"sans-font-family", "sans-font-weight", "sans-font-posture", "sans-font-size"},
                new String[]{sansFont.getFamily(), FontUtils.fontWeight(sansFont).name(), fontPosture(sansFont).name(), String.valueOf(sansFont.getSize())});


        formatted = TemplateUtils.format(formatted,
                new String[]{"mono-font-family", "mono-font-weight", "mono-font-posture", "mono-font-size"},
                new String[]{monoFont.getFamily(), FontUtils.fontWeight(monoFont).name(), fontPosture(monoFont).name(), String.valueOf(monoFont.getSize())});

        return formatted;
    }
}
