package com.mindolph.desktop;

import com.mindolph.core.Env;
import com.mindolph.base.plugin.ContextHelperPlugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.fx.Main;
import com.mindolph.fx.data.DataMigrator;
import com.mindolph.genai.GenAiPlugin;
import com.mindolph.markdown.MarkdownPlugin;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.MindMapPlugin;
import com.mindolph.plantuml.PlantUmlPlugin;
import javafx.application.Application;

/**
 * @author mindolph.com@gmail.com
 */
public class Launcher {

    public static void main(String[] args) {
        Env.isDevelopment = false;
        FxPreferences.getInstance().init(Launcher.class);
        DataMigrator dataMigrator = new DataMigrator();
        dataMigrator.fixData();
        // register plugins TODO
        PluginManager.getIns().registerPlugin(new PlantUmlPlugin());
        PluginManager.getIns().registerPlugin(new ContextHelperPlugin());
        PluginManager.getIns().registerPlugin(new GenAiPlugin());
        PluginManager.getIns().registerPlugin(new MindMapPlugin());
        PluginManager.getIns().registerPlugin(new MarkdownPlugin());
        Application.launch(Main.class, args);
    }
}
