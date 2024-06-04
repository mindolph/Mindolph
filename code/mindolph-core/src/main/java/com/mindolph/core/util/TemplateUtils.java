package com.mindolph.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Handle template with variable like '${name}'.
 *
 * @since 1.8.1
 */
public class TemplateUtils {

    /**
     *
     *
     * @param template
     * @param argName
     * @param argValue
     * @return
     */
    public static String format(String template, String argName, Object argValue) {
        return StringUtils.replace(template, "${%s}".formatted(argName), argValue.toString());
    }

    /**
     *
     * @param template
     * @param argNames
     * @param argValues
     * @return
     */
    public static String format(String template, String[] argNames, String[] argValues) {
        String formatted = template;
        for (int i = 0; i < argNames.length; i++) {
            formatted = StringUtils.replace(formatted, "${%s}".formatted(argNames[i]), argValues[i]);
        }
        return formatted;
    }
}
