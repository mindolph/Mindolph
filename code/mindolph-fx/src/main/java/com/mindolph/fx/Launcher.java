package com.mindolph.fx;

import com.mindolph.base.Env;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.application.Application;

/**
 * @author mindolph.com@gmail.com
 */
public class Launcher {

    public static void main(String[] args) {
//        Env.isDevelopment = SystemPropertyUtils.isSystemPropValTrue("dev");
        String dev = System.getenv("dev");
        if ("false".equals(dev)) {
            Env.isDevelopment = false;
        }
        System.out.println("Mode: " + (Env.isDevelopment ? "development" : "test"));
        FxPreferences.getInstance().init(Launcher.class);
        Application.launch(Main.class, args);
    }
}
