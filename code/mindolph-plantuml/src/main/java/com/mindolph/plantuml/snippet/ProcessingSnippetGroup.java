package com.mindolph.plantuml.snippet;

import com.mindolph.core.model.Snippet;

import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class ProcessingSnippetGroup extends BasePlantUmlSnippetGroup {

    private static final List<Snippet> processing = Arrays.asList(
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
    );

    private static final List<Snippet> builtinFunctions = Arrays.asList(
            new Snippet().title("Function: %chr").code("%chr(⨁)").description("""
                    Return a character from a give Unicode value.
                    example: %chr(65), return: A"""),
            new Snippet().title("Function: %darken").code("%darken(⨁)").description("""
                    Return a darken color of a given color with some ratio.
                    example: %darken("red", 20), return: #CC0000"""),
            new Snippet().title("Function: %date").code("%date(⨁)").description("""
                    Retrieve current date. You can provide an optional format for the date.
                    example: %date("yyyy.MM.dd' at 'HH:mm"), return: current date"""),
            new Snippet().title("Function: %dec2hex").code("%dec2hex(⨁)").description("""
                    Return the hexadecimal string (String) of a decimal value (Int).
                    example: %dec2hex(12), return: c"""),
            new Snippet().title("Function: %dirpath").code("%dirpath()").description("""
                    Retrieve current dirpath.
                    example: %dirpath(), return: current path"""),
            new Snippet().title("Function: %feature").code("%feature(⨁)").description("""
                    Check if some feature is available in the current PlantUML running version.
                    example: %feature("theme"), return: true"""),
            new Snippet().title("Function: %false").code("%false()").description("""
                    Return always false.
                    example: %false(), return: false"""),
            new Snippet().title("Function: %file_exists").code("%file_exists(⨁)").description("""
                    Check if a file exists on the local filesystem.
                    example: %file_exists("c:/foo/dummy.txt"), return: true if the file exists"""),
            new Snippet().title("Function: %filename").code("%filename()").description("""
                    Retrieve current filename.
                    example: %filename(), return: current filename"""),
            new Snippet().title("Function: %function_exists").code("%function_exists(⨁)").description("""
                    Check if a function exists.
                    example: %function_exists("$some_function"), return: true if the function has been defined"""),
            new Snippet().title("Function: %get_variable_value").code("%get_variable_value(⨁)").description("""
                    Retrieve some variable value.
                    example: %get_variable_value("$my_variable"), return: the value of the variable"""),
            new Snippet().title("Function: %getenv").code("%getenv(⨁)").description("""
                    Retrieve environment variable value.
                    example: %getenv("OS"), return: the value of OS variable"""),
            new Snippet().title("Function: %hex2dec").code("%hex2dec(⨁)").description("""
                    Return the decimal value (Int) of a hexadecimal string (String).
                    example: %hex2dec("d") or %hex2dec(d), return: 13"""),
            new Snippet().title("Function: %hsl_color").code("%hsl_color(⨁)").description("""
                    Return the RGBa color from a HSL color %hsl_color(h, s, l) or %hsl_color(h, s, l, a).
                    example: %hsl_color(120, 100, 50), return: #00FF00"""),
            new Snippet().title("Function: %intval").code("%intval(⨁)").description("""
                    Convert a String to Int.
                    example: %intval("42"), return: 42"""),
            new Snippet().title("Function: %is_dark").code("%is_dark(⨁)").description("""
                    Check if a color is a dark one.
                    example: %is_dark("#000000"), return: true"""),
            new Snippet().title("Function: %is_light").code("%is_light(⨁)").description("""
                    Check if a color is a light one.
                    example: %is_light("#000000"), return: false"""),
            new Snippet().title("Function: %lighten").code("%lighten(⨁)").description("""
                    Return a lighten color of a given color with some ratio.
                    example: %lighten("red", 20), return: #CC3333"""),
            new Snippet().title("Function: %loadJSON").code("%loadJSON(⨁)").description("""
                    Load JSON data from local file or external URL.
                    example: %loadJSON("http://localhost:7778/management/health"), return: JSON data"""),
            new Snippet().title("Function: %lower").code("%lower(⨁)").description("""
                    Return a lowercase string.
                    example: %lower("Hello"), return: hello in that example"""),
            new Snippet().title("Function: %newline").code("%newline()").description("""
                    Return a newline.
                    example: %newline(), return: a newline"""),
            new Snippet().title("Function: %not").code("%not(⨁)").description("""
                    Return the logical negation of an expression.
                    example: %not(2+2==4), return: false in that example"""),
            new Snippet().title("Function: %lighten").code("%lighten(⨁)").description("""
                    Return a lighten color of a given color with some ratio.
                    example: %lighten("red", 20), return: #CC3333"""),
            new Snippet().title("Function: %reverse_color").code("%reverse_color(⨁)").description("""
                    Reverse a color using RGB.
                    example: %reverse_color("#FF7700"), return: #0088FF"""),
            new Snippet().title("Function: %reverse_hsluv_color").code("%reverse_hsluv_color(⨁)").description("""
                    Reverse a color using HSLuv.
                    example: %reverse_hsluv_color("#FF7700"), return: #602800"""),
            new Snippet().title("Function: %set_variable_value").code("%set_variable_value(⨁)").description("""
                    Set a global variable.
                    example: %set_variable_value("$my_variable", "some_value"), return: an empty string"""),
            new Snippet().title("Function: %size").code("%size(⨁)").description("""
                    Return the size of any string or JSON structure.
                    example: %size("foo"), return: 3 in the example"""),
            new Snippet().title("Function: %string").code("%string(⨁)").description("""
                    Convert an expression to String.
                    example: %string(1 + 2), return: 3 in the example"""),
            new Snippet().title("Function: %strlen").code("%strlen(⨁)").description("""
                    Calculate the length of a String.
                    example: %strlen("foo"), return: 3 in the example"""),
            new Snippet().title("Function: %strpos").code("%strpos(⨁)").description("""
                    Search a substring in a string.
                    example: %strpos("abcdef", "ef"), return: 4 (position of ef)"""),
            new Snippet().title("Function: %substr").code("%substr(⨁)").description("""
                    Extract a substring. Takes 2 or 3 arguments.
                    example: %substr("abcdef", 3, 2), return: "de" in the example"""),
            new Snippet().title("Function: %true").code("%true()").description("""
                    Return always true.
                    example: %true(), return: true"""),
            new Snippet().title("Function: %upper").code("%upper(⨁)").description("""
                    Return an uppercase string.
                    example: %upper("Hello"), return: HELLO in that example"""),
            new Snippet().title("Function: %variable_exists").code("%variable_exists(⨁)").description("""
                    Check if a variable exists.
                    example: %variable_exists("$my_variable"), return: true if the variable has been defined exists"""),
            new Snippet().title("Function: %version").code("%version()").description("""
                    Return PlantUML current version.
                    example: %version(), return: 1.2020.8 for example""")
    );

    @Override
    public String getTitle() {
        return "Processing&Builtin Functions";
    }

    @Override
    public void init() {
        super.snippets.addAll(processing);
        super.snippets.addAll(builtinFunctions);
    }
}
