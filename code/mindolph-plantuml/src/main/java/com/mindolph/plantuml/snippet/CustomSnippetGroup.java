package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;

/**
 * @author mindolph.com@gmail.com
 */
public class CustomSnippetGroup extends BaseSnippetGroup {

    @Override
    public void init() {
        super.alwaysShow = true;
    }

    @Override
    public String getTitle() {
        return "Custom";
    }
}
