package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.Snippet;

import java.util.Arrays;

/**
 * https://plantuml.com/sequence-diagram
 *
 * @author mindolph.com@gmail.com
 */
public class DiagramSnippetGroup extends BaseSnippetGroup {

    @Override
    public String getTitle() {
        return "Diagram";
    }

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet("alt").code("""
                                alt successful case
                                ⨁
                                else some kind of failure
                                                                
                                else Another type of failure
                                                                
                                end
                                """).generateImage(),
                        new Snippet("group").code("""
                                group Main Label [Second Label]
                                ⨁
                                end
                                """).generateImage(),
                        new Snippet("loop").code("""
                                loop 1000 times
                                ⨁
                                end
                                """).generateImage(),
                        new Snippet("activate").code("""
                                activate ⨁
                                                                
                                deactivate ⨁
                                """).generateImage(),
                        new Snippet("activate return").code("""
                                activate ⨁
                                                                
                                return
                                """).generateImage(),
                        new Snippet("if").code("""
                                if (⨁) then (Y)
                                                                
                                endif
                                """).generateImage(),
                        new Snippet("if else").code("""
                                if (⨁) then (Y)

                                else (n)
                                                                
                                endif
                                """).generateImage(),
                        new Snippet("if elseif").code("""
                                if (⨁) then (Y)

                                elseif (⨁) then (Y)
                                                                
                                else (n)
                                                     
                                endif
                                """).generateImage(),
                        new Snippet("vertical mode(if elseif)").code("""
                                !pragma useVerticalIf on
                                """).generateImage(),
                        new Snippet().title("while").code("""
                                while (⨁) is (N)
                                                                
                                endwhile (Y)
                                """).generateImage(),
                        new Snippet().title("repeat").code("""
                                repeat
                                                                
                                backward: this is backward
                                repeat while (⨁) is (Y)
                                """).generateImage(),
                        new Snippet("switch").code("""
                                switch (⨁?)
                                case ( condition A )
                                                                
                                case ( condition B )
                                                                
                                endswitch
                                """).generateImage(),
                        new Snippet("fork").code("""
                                fork
                                ⨁
                                fork again
                                      
                                end fork
                                """).generateImage(),
                        new Snippet("split").code("""
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
