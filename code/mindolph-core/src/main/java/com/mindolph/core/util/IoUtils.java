package com.mindolph.core.util;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class IoUtils {

    public static String readAll(InputStreamReader in) throws IOException {
        StringBuilder buf = new StringBuilder();
        while (in.ready()) {
            buf.append((char) in.read());
        }
        return buf.toString();
    }

}
