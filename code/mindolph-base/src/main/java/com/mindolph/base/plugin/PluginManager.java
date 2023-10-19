package com.mindolph.base.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @see Plugin
 */
public class PluginManager {

    private static PluginManager ins;

    private final Map<String, Plugin> pluginMap ;

    private PluginManager() {
        pluginMap = new HashMap<>();
    }

    public synchronized static PluginManager getIns() {
        if (ins == null) {
            ins = new PluginManager();
        }
        return ins;
    }

    public void registerPlugin(String fileType, Plugin plugin) {
        pluginMap.put(fileType, plugin);
    }

    public Plugin findPlugin(String fileType) {
        return pluginMap.get(fileType);
    }

}
