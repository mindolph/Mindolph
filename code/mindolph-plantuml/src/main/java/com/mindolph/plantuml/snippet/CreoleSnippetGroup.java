package com.mindolph.plantuml.snippet;

import com.mindolph.core.model.Snippet;

import java.util.Arrays;

/**
 * https://plantuml.com/creole
 *
 * @author mindolph.com@gmail.com
 */
public class CreoleSnippetGroup extends BasePlantUmlSnippetGroup {
    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                new Snippet().title("Bold Text").code("""
                        **bold text**
                        """),
                new Snippet().title("Italic Text").code("""
                        //italics//
                        """),
                new Snippet().title("Monospaced Text").code("""
                        ""monospaced""
                        """),
                new Snippet().title("Stricken Out Text").code("""
                        --stricken out--
                        """),
                new Snippet().title("Underlined Text").code("""
                        __underlined__
                        """),
                new Snippet().title("Wave underlined Text").code("""
                        ~~wave underlined~~
                        """),
                new Snippet().title("Bullet List").code("""
                        * bullet list
                        * second item
                        ** sub item
                        """),
                new Snippet().title("Numbered List").code("""
                        # numbered list
                        # second item
                        ## sub item
                        """),
                new Snippet().title("Escape Character").code("""
                        ~__not underlined__
                        """),
                new Snippet().title("Horizontal Line").code("""
                        ====
                        """),
                new Snippet().title("Heading: Extra Large").code("""
                        = Extra Large Heading
                        """),
                new Snippet().title("Heading: Large").code("""
                        == Large Heading
                        """),
                new Snippet().title("Heading: Medium").code("""
                        === Medium Heading
                        """),
                new Snippet().title("Heading: Small").code("""
                        ==== Small Heading
                        """),
                new Snippet().title("Link").code("""
                        [[http://plantuml.com{Optional tooltip} This label is printed]]
                        """),
                new Snippet().title("Code").code("""
                        <code>
                        ⨁
                        </code>
                        """),
                new Snippet().title("Table").code("""
                        |= |= table |= header |
                        | a | table | row |
                        | b | table | row |
                        """),
                new Snippet().title("Tree").code("""
                        |_ First Line
                        |_ **Second Line**
                          |_ a
                          |_ b
                        |_ Third line
                        """)
                )

        );
    }

    @Override
    public String getTitle() {
        return "Creole";
    }
}
