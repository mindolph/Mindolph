package com.mindolph.base.plugin;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author mindolph.com@gmail.com
 * @see Plugin
 */
public class PluginManager {

    private static PluginManager ins;

    private final MultiValuedMap<String, Plugin> pluginMap;

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

    public Collection<Plugin> findPlugins(String fileType) {
        return pluginMap.get(fileType);
    }

    public Collection<Plugin> findPlugins(Predicate<Plugin> filter) {
        return fileTypeMap.keys().stream().filter(filter).toList();
    }

    public Optional<Plugin> findFirstPlugin(String fileType) {
        Collection<Plugin> plugins = pluginMap.get(fileType);
        if (plugins.isEmpty()) {
            return Optional.empty();
        }
        return plugins.stream().findFirst();
    }

    public Optional<Plugin> findFirstPlugin(Predicate<Plugin> filter) {
        return fileTypeMap.keys().stream().filter(filter).findFirst();
    }

    public Collection<Plugin> getPlugins() {
        return fileTypeMap.keys();
    }

}
