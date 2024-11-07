package com.mindolph.plantuml.snippet;

/**
 * @author mindolph.com@gmail.com
 */
public class CustomSnippetGroup extends BasePlantUmlSnippetGroup {

    @Override
    public void init() {
        super.alwaysShow = true;
    }

    @Override
    public String getTitle() {
        return "Custom";
    }
}
