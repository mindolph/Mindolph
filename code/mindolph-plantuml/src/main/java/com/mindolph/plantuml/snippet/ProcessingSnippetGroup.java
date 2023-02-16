package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class ProcessingSnippetGroup extends BaseSnippetGroup {

    @Override
    public String getTitle() {
        return "Processing";
    }

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("Variable Integer").code("""
                                !$a = 42
                                """),
                        new Snippet().title("Variable String").code("""
                                !$a = 'foo'
                                """),
                        new Snippet().title("Variable Map").code("""
                                !$a = { "name": "Mindolph", "age": 1 }
                                """),
                        new Snippet().title("Conditions").code("""
                                !if ($a + 10 > 30) && ($b == "foo")
                                ⨁
                                !elseif ($c == "bar")
                                !else
                                !endif
                                """),
                        new Snippet().title("While Loop").code("""
                                !while $a != 0
                                ⨁
                                !endwhile
                                """),
                        new Snippet().title("Procedure").code("""
                                !procedure $name($from, $to)
                                    $from --> $to⨁
                                !endprocedure
                                """).description("variables will be destroyed when the function is exited"),
                        new Snippet().title("Function").code("""
                                !function $name($a, $b=1)
                                    !return $a + $b⨁
                                !endfunction
                                """).description("Function can access global variables; only last variable can have default value"),
                        new Snippet().title("Inline Function").code("""
                                !function $name($a) !return $a + 1
                                """).description("Function can access global variables"),
                        new Snippet().title("Dynamic Invocation: Procedure").code("""
                                %invoke_procedure(⨁)
                                """).description("""
                                You can dynamically invoke a procedure using the special %invoke_procedure() procedure.
                                This procedure takes as first argument the name of the actual procedure to be called. 
                                The optional following arguments are copied to the called procedure.
                                """),
                        new Snippet().title("Dynamic Invocation: Function").code("""
                                %call_user_func(⨁)
                                """).description("""
                                                                
                                """),
                        new Snippet().title("Including iuml file").code("""
                                !include foo.txt!0
                                """).description("""
                                The file *.iuml can be included in many diagrams, and any modification in this file will change all diagrams that include it.
                                You can also put several @startuml/@enduml text block in an included file and then specify which block you want to include 
                                adding !0 where 0 is the block number.
                                The !0 notation denotes the first diagram.
                                You can also put an id to some @startuml/@enduml text block in an included file using @startuml(id=MY_OWN_ID) syntax and then 
                                include the block adding !MY_OWN_ID when including the file, so using something like !include foo.txt!MY_OWN_ID.
                                By default, a file can only be included once. You can use !include_many instead of !include if you want to include some file 
                                several times. Note that there is also a !include_once directive that raises an error if a file is included several times.
                                """),
                        new Snippet().title("Including subpart: subpart").code("""
                                !startsub SUB_PART_NAME
                                A -> B: foo bar
                                !endsub
                                """),
                        new Snippet().title("Including subpart: include").code("""
                                !includesub file.puml!SUB_PART_NAME
                                """),
                        new Snippet().title("Load JSON file").code("""
                                !$foo = %loadJSON("myDir/localFile.json")
                                """)
                )
        );
    }
}
