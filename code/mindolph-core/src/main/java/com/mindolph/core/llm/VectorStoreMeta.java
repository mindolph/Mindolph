package com.mindolph.core.llm;

import com.mindolph.core.constant.VectorStoreProvider;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @since 1.13.0
 */
public class VectorStoreMeta {

    private VectorStoreProvider provider;

    private String host;

    private Integer port;

    private String database;

    private String username;

    private String password;

    public boolean isAllSetup() {
        return !(provider == null || port == null || StringUtils.isAnyBlank(host, database, username, password));
    }

    public VectorStoreProvider getProvider() {
        return provider;
    }

    public void setProvider(VectorStoreProvider provider) {
        this.provider = provider;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "VectorStoreMeta{" +
                "provider=" + provider +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
