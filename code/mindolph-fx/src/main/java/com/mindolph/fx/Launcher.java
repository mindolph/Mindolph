package com.mindolph.fx;

import com.mindolph.base.Env;
import com.mindolph.genai.GenAiPlugin;
import com.mindolph.base.plugin.ContextHelperPlugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.fx.data.DataMigrator;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.plantuml.PlantUmlPlugin;
import javafx.application.Application;

/**
 * Launch with possible environment variables:
 * dev=True|false
 * disable-reopen=true|false
 * disable-window-resize=true|false
 * mock-llm=true|false
 *
 * If more log output required, add -Dlog4j2.configurationFile=log4j2-debug.xml to application arguments.
 * Add -Djdk.gtk.version=2 to JVM options if CJK input method is used on Linux.
 *
 * @author mindolph.com@gmail.com
 */
public class Launcher {

    public static void main(String[] args) {
//        Env.isDevelopment = SysUtils.isSystemPropValTrue("dev");
        String dev = System.getenv("dev");
        if ("false".equals(dev)) {
            Env.isDevelopment = false;
        }
        System.out.println("Mode: " + (Env.isDevelopment ? "development" : "test"));
        FxPreferences.getInstance().init(Launcher.class);
        DataMigrator dataMigrator = new DataMigrator();
        dataMigrator.fixData();

        // register plugins TODO
        PluginManager.getIns().registerPlugin(new PlantUmlPlugin());
        PluginManager.getIns().registerPlugin(new ContextHelperPlugin());
        PluginManager.getIns().registerPlugin(new GenAiPlugin());

        Application.launch(Main.class, args);
    }
}
