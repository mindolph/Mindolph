package com.mindolph.core.constant;

import org.apache.commons.lang3.SystemUtils;

/**
 * @author mindolph.com@gmail.com
 */
public interface TextConstants {

    /**
     * To collaborating with Windows and *ix like systems, use this separator.
     */
    String LINE_SEPARATOR = "\n"; // SystemUtils.IS_OS_WINDOWS ? "\r\n" : "\n";

    String LINE_SEPARATOR2 = LINE_SEPARATOR + LINE_SEPARATOR;

    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

}
