package com.mindolph.plantuml.snippet;

import com.mindolph.core.model.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class SkinparamSnippetGroup extends BasePlantUmlSnippetGroup {

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("Shadowing").code("""
                                skinparam shadowing true
                                """),
                new Snippet().title("Reverse Color").code("""
                        skinparam monochrome reverse
                        """),
                new Snippet().title("Change Default Font Name").code("""
                        skinparam defaultFontName ⨁
                        """),
                new Snippet().title("Font Name").code("""
                        skinparam classFontName ⨁
                        """),
                new Snippet().title("Font Color").code("""
                        skinparam classFontColor ⨁
                        """),
                new Snippet().title("Font Size").code("""
                        skinparam classFontSize ⨁
                        """),
                new Snippet().title("Text Alignment: Message").code("""
                        skinparam sequenceMessageAlign ⨁
                        """).description("""
                        Text alignment can be set to left, right or center in skinparam sequenceMessageAlign.
                        You can also use direction or reverseDirection values to align text depending on arrow direction.
                        """),
                new Snippet().title("Text Alignment: Reference").code("""
                        skinparam sequenceReferenceAlign ⨁
                        """).description("""
                        Text alignment can be set to left, right or center in skinparam sequenceMessageAlign.
                        You can also use direction or reverseDirection values to align text depending on arrow direction.
                        """)
                )
        );
    }

    @Override
    public String getTitle() {
        return "Skinparam";
    }
}
