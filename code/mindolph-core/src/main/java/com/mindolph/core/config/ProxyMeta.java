package com.mindolph.core.config;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @param type
 * @param host
 * @param port
 * @param username
 * @param password
 * @since unknown
 */
public record ProxyMeta(String type, String host, int port, String username, String password) {
    public String url() {
        return "%s://%s:%s".formatted(StringUtils.lowerCase(type), host, port).trim();
    }
}
