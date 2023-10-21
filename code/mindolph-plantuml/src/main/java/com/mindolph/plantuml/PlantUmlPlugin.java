package com.mindolph.plantuml;

import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.Collection;
import java.util.Collections;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class PlantUmlPlugin implements Plugin {

    @Override
    public Collection<String> supportedFileTypes() {
        return Collections.singletonList(SupportFileTypes.TYPE_PLANTUML);
    }

    @Override
    public InputHelper getInputHelper() {
        return new PlantUmlInputHelper();
    }

}
