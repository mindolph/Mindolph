package com.mindolph.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class TimeUtils {

    /**
     *
     * @return
     */
    public static String createTimestamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    }

}
