package com.mindolph.desktop;

import com.mindolph.base.Env;
import com.mindolph.fx.Main;
import com.mindolph.fx.data.DataMigrator;
import com.mindolph.mfx.preference.FxPreferences;
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
        Application.launch(Main.class, args);
    }
}
