package com.mindolph.core.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * @since 1.8.1
 */
public class UriUtils {

    public static URI filePathToResourceUri(String filePath) {
        return URI.create(filePathToResourceUriStr(filePath));
    }

    public static String filePathToResourceUriStr(String filePath) {
        if (StringUtils.contains(filePath, "\\")) {
            return "file://" + StringUtils.replace(filePath, "\\", "/");
        }
        else {
            return "file://" + filePath;
        }
    }

    public static void main(String[] args) {
        System.out.println(filePathToResourceUriStr("C:\\windows\\folder"));
        System.out.println(filePathToResourceUri("C:\\windows\\folder"));
    }

}
