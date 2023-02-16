package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class GeneralSnippetGroup extends BaseSnippetGroup {

    @Override
    public String getTitle() {
        return "General";
    }

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("Title").code("""
                                title
                                 <u>This</u> title
                                 on <i>several</i> lines and using <back:cadetblue>creole tags</back>
                                end title
                                """).generateImage(),
                        new Snippet().title("Header and footer").code("""
                                header
                                <font color=red>Header:</font>
                                This is a header
                                endheader
                                center footer "this is a center footer"
                                """).generateImage(),
                        new Snippet().title("Caption").code("""
                                "caption this is a caption"
                                """).generateImage(),
                        new Snippet().title("Legend").code("""
                                legend top left
                                  This is a legend
                                endlegend
                                """).generateImage(),
                        new Snippet().title("Block Comment").code("""
                                /'
                                many lines comments
                                here⨁
                                '/
                                """).generateImage(),
                        new Snippet().title("Style").code("""
                                <style>
                                ⨁
                                </style>
                                """),
                        new Snippet().title("Mainframe").code("""
                                mainframe This is a **mainframe**
                                """).generateImage()
                )
        );
    }
}
