package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.ImageSnippet;

import java.util.Arrays;

/**
 * <a href="https://plantuml.com/sequence-diagram">sequence diagram</a>
 *
 * @author mindolph.com@gmail.com
 */
public class DiagramSnippetGroup extends BasePlantUmlSnippetGroup {

    @Override
    public String getTitle() {
        return "Diagram";
    }

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new ImageSnippet("alt").code("""
                                alt successful case
                                ⨁
                                else some kind of failure
                                
                                else Another type of failure
                                
                                end
                                """).generateImage(),
                        new ImageSnippet("group").code("""
                                group Main Label [Second Label]
                                ⨁
                                end
                                """).generateImage(),
                        new ImageSnippet("loop").code("""
                                loop 1000 times
                                ⨁
                                end
                                """).generateImage(),
                        new ImageSnippet("activate").code("""
                                activate ⨁
                                
                                deactivate ⨁
                                """).generateImage(),
                        new ImageSnippet("activate return").code("""
                                activate ⨁
                                
                                return
                                """).generateImage(),
                        new ImageSnippet("if").code("""
                                if (⨁) then (Y)
                                
                                endif
                                """).generateImage(),
                        new ImageSnippet("if else").code("""
                                if (⨁) then (Y)

                                else (n)
                                
                                endif
                                """).generateImage(),
                        new ImageSnippet("if elseif").code("""
                                if (⨁) then (Y)

                                elseif (⨁) then (Y)
                                
                                else (n)
                                
                                endif
                                """).generateImage(),
                        new ImageSnippet("vertical mode(if elseif)").code("""
                                !pragma useVerticalIf on
                                """).generateImage(),
                        new ImageSnippet().title("while").code("""
                                while (⨁) is (N)
                                
                                endwhile (Y)
                                """).generateImage(),
                        new ImageSnippet().title("repeat").code("""
                                repeat
                                
                                backward: this is backward
                                repeat while (⨁) is (Y)
                                """).generateImage(),
                        new ImageSnippet("switch").code("""
                                switch (⨁?)
                                case ( condition A )
                                
                                case ( condition B )
                                
                                endswitch
                                """).generateImage(),
                        new ImageSnippet("fork").code("""
                                fork
                                ⨁
                                fork again
                                
                                end fork
                                """).generateImage(),
                        new ImageSnippet("split").code("""
                                split
                                ⨁
                                split again
                                
                                split again
                                
                                end split
                                """).generateImage()
                )
        );
    }
}
