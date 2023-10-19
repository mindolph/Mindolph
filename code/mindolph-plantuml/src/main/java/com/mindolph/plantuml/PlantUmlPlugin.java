package com.mindolph.plantuml;

import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.Plugin;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class PlantUmlPlugin implements Plugin {

    @Override
    public InputHelper getInputHelper() {
        return new PlantUmlInputHelper();
    }

}
