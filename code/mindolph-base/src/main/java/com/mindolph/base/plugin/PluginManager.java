package com.mindolph.base.plugin;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.Collection;

/**
 * @author mindolph.com@gmail.com
 * @see Plugin
 */
public class PluginManager {

    private static PluginManager ins;

    private final MultiValuedMap<String, Plugin> pluginMap ;

    private final MultiValuedMap<Plugin, String> fileTypeMap;

    private PluginManager() {
        pluginMap = new HashSetValuedHashMap<>();
        fileTypeMap = new HashSetValuedHashMap<>();
    }

    public synchronized static PluginManager getIns() {
        if (ins == null) {
            ins = new PluginManager();
        }
        return ins;
    }

    public void registerPlugin(Plugin plugin) {
        fileTypeMap.putAll(plugin, plugin.supportedFileTypes());
        for (String fileType : plugin.supportedFileTypes()) {
            pluginMap.get(fileType).add(plugin);
        }
    }

    public Collection<Plugin> findPlugin(String fileType) {
        return pluginMap.get(fileType);
    }

    public Collection<Plugin> getPlugins() {
        return fileTypeMap.keys();
    }

}
