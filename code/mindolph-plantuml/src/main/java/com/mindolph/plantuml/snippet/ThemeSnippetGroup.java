package com.mindolph.plantuml.snippet;

import com.mindolph.core.model.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class ThemeSnippetGroup extends BasePlantUmlSnippetGroup {
    @Override
    public String getTitle() {
        return "Theme";
    }

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("_none_").code("""
                                !theme _none_
                                """),
                        new Snippet().title("amiga").code("""
                                !theme amiga
                                """),
                        new Snippet().title("aws-orange").code("""
                                !theme aws-orange
                                """),
                        new Snippet().title("black-knight").code("""
                                !theme black-knight
                                """),
                        new Snippet().title("bluegray").code("""
                                !theme bluegray
                                """),
                        new Snippet().title("blueprint").code("""
                                !theme blueprint
                                """),
                        new Snippet().title("cerulean").code("""
                                !theme cerulean
                                """),
                        new Snippet().title("cerulean-outline").code("""
                                !theme cerulean-outline
                                """),
                        new Snippet().title("crt-amber").code("""
                                !theme crt-amber
                                """),
                        new Snippet().title("crt-green").code("""
                                !theme crt-green
                                """),
                        new Snippet().title("cyborg").code("""
                                !theme cyborg
                                """),
                        new Snippet().title("cyborg-outline").code("""
                                !theme cyborg-outline
                                """),
                        new Snippet().title("hacker").code("""
                                !theme hacker
                                """),
                        new Snippet().title("lightgray").code("""
                                !theme lightgray
                                """),
                        new Snippet().title("materia").code("""
                                !theme materia
                                """),
                        new Snippet().title("materia-outline").code("""
                                !theme materia-outline
                                """),
                        new Snippet().title("metal").code("""
                                !theme metal
                                """),
                        new Snippet().title("mimeograph").code("""
                                !theme mimeograph
                                """),
                        new Snippet().title("minty").code("""
                                !theme minty
                                """),
                        new Snippet().title("plain").code("""
                                !theme plain
                                """),
                        new Snippet().title("reddress-darkblue").code("""
                                !theme reddress-darkblue
                                """),
                        new Snippet().title("reddress-darkveen").code("""
                                !theme reddress-darkveen
                                """),
                        new Snippet().title("reddress-darkorange").code("""
                                !theme reddress-darkorange
                                """),
                        new Snippet().title("reddress-darkred").code("""
                                !theme reddress-darkred
                                """),
                        new Snippet().title("reddress-lightblue").code("""
                                !theme reddress-lightblue
                                """),
                        new Snippet().title("reddress-lightgreen").code("""
                                !theme reddress-lightgreen
                                """),
                        new Snippet().title("reddress-lightorange").code("""
                                !theme reddress-lightorange
                                """),
                        new Snippet().title("sandstone").code("""
                                !theme sandstone
                                """),
                        new Snippet().title("silver").code("""
                                !theme silver
                                """),
                        new Snippet().title("sketchy").code("""
                                !theme sketchy
                                """),
                        new Snippet().title("sketchy-outline").code("""
                                !theme sketchy-outline
                                """),
                        new Snippet().title("spacelab").code("""
                                !theme spacelab
                                """),
                        new Snippet().title("spacelab-white").code("""
                                !theme spacelab-white
                                """),
                        new Snippet().title("superhero").code("""
                                !theme superhero
                                """),
                        new Snippet().title("superhero-outline").code("""
                                !theme superhero-outline
                                """),
                        new Snippet().title("toy").code("""
                                !theme toy
                                """),
                        new Snippet().title("united").code("""
                                !theme united
                                """),
                        new Snippet().title("vibrant").code("""
                                !theme vibrant
                                """)
                )
        );
    }
}
