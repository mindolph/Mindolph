package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class BuiltinFunctionsSnippetGroup extends BaseSnippetGroup {

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("%chr").code("%chr(⨁)").description("""
                                Return a character from a give Unicode value.
                                example: %chr(65), return: A"""),
                        new Snippet().title("%darken").code("%darken(⨁)").description("""
                                Return a darken color of a given color with some ratio.
                                example: %darken("red", 20), return: #CC0000"""),
                        new Snippet().title("%date").code("%date(⨁)").description("""
                                Retrieve current date. You can provide an optional format for the date.
                                example: %date("yyyy.MM.dd' at 'HH:mm"), return: current date"""),
                        new Snippet().title("%dec2hex").code("%dec2hex(⨁)").description("""
                                Return the hexadecimal string (String) of a decimal value (Int).
                                example: %dec2hex(12), return: c"""),
                        new Snippet().title("%dirpath").code("%dirpath()").description("""
                                Retrieve current dirpath.
                                example: %dirpath(), return: current path"""),
                        new Snippet().title("%feature").code("%feature(⨁)").description("""
                                Check if some feature is available in the current PlantUML running version.
                                example: %feature("theme"), return: true"""),
                        new Snippet().title("%false").code("%false()").description("""
                                Return always false.
                                example: %false(), return: false"""),
                        new Snippet().title("%file_exists").code("%file_exists(⨁)").description("""
                                Check if a file exists on the local filesystem.
                                example: %file_exists("c:/foo/dummy.txt"), return: true if the file exists"""),
                        new Snippet().title("%filename").code("%filename()").description("""
                                Retrieve current filename.
                                example: %filename(), return: current filename"""),
                        new Snippet().title("%function_exists").code("%function_exists(⨁)").description("""
                                Check if a function exists.
                                example: %function_exists("$some_function"), return: true if the function has been defined"""),
                        new Snippet().title("%get_variable_value").code("%get_variable_value(⨁)").description("""
                                Retrieve some variable value.
                                example: %get_variable_value("$my_variable"), return: the value of the variable"""),
                        new Snippet().title("%getenv").code("%getenv(⨁)").description("""
                                Retrieve environment variable value.
                                example: %getenv("OS"), return: the value of OS variable"""),
                        new Snippet().title("%hex2dec").code("%hex2dec(⨁)").description("""
                                Return the decimal value (Int) of a hexadecimal string (String).
                                example: %hex2dec("d") or %hex2dec(d), return: 13"""),
                        new Snippet().title("%hsl_color").code("%hsl_color(⨁)").description("""
                                Return the RGBa color from a HSL color %hsl_color(h, s, l) or %hsl_color(h, s, l, a).
                                example: %hsl_color(120, 100, 50), return: #00FF00"""),
                        new Snippet().title("%intval").code("%intval(⨁)").description("""
                                Convert a String to Int.
                                example: %intval("42"), return: 42"""),
                        new Snippet().title("%is_dark").code("%is_dark(⨁)").description("""
                                Check if a color is a dark one.
                                example: %is_dark("#000000"), return: true"""),
                        new Snippet().title("%is_light").code("%is_light(⨁)").description("""
                                Check if a color is a light one.
                                example: %is_light("#000000"), return: false"""),
                        new Snippet().title("%lighten").code("%lighten(⨁)").description("""
                                Return a lighten color of a given color with some ratio.
                                example: %lighten("red", 20), return: #CC3333"""),
                        new Snippet().title("%loadJSON").code("%loadJSON(⨁)").description("""
                                Load JSON data from local file or external URL.
                                example: %loadJSON("http://localhost:7778/management/health"), return: JSON data"""),
                        new Snippet().title("%lower").code("%lower(⨁)").description("""
                                Return a lowercase string.
                                example: %lower("Hello"), return: hello in that example"""),
                        new Snippet().title("%newline").code("%newline()").description("""
                                Return a newline.
                                example: %newline(), return: a newline"""),
                        new Snippet().title("%not").code("%not(⨁)").description("""
                                Return the logical negation of an expression.
                                example: %not(2+2==4), return: false in that example"""),
                        new Snippet().title("%lighten").code("%lighten(⨁)").description("""
                                Return a lighten color of a given color with some ratio.
                                example: %lighten("red", 20), return: #CC3333"""),
                        new Snippet().title("%reverse_color").code("%reverse_color(⨁)").description("""
                                Reverse a color using RGB.
                                example: %reverse_color("#FF7700"), return: #0088FF"""),
                        new Snippet().title("%reverse_hsluv_color").code("%reverse_hsluv_color(⨁)").description("""
                                Reverse a color using HSLuv.
                                example: %reverse_hsluv_color("#FF7700"), return: #602800"""),
                        new Snippet().title("%set_variable_value").code("%set_variable_value(⨁)").description("""
                                Set a global variable.
                                example: %set_variable_value("$my_variable", "some_value"), return: an empty string"""),
                        new Snippet().title("%size").code("%size(⨁)").description("""
                                Return the size of any string or JSON structure.
                                example: %size("foo"), return: 3 in the example"""),
                        new Snippet().title("%string").code("%string(⨁)").description("""
                                Convert an expression to String.
                                example: %string(1 + 2), return: 3 in the example"""),
                        new Snippet().title("%strlen").code("%strlen(⨁)").description("""
                                Calculate the length of a String.
                                example: %strlen("foo"), return: 3 in the example"""),
                        new Snippet().title("%strpos").code("%strpos(⨁)").description("""
                                Search a substring in a string.
                                example: %strpos("abcdef", "ef"), return: 4 (position of ef)"""),
                        new Snippet().title("%substr").code("%substr(⨁)").description("""
                                Extract a substring. Takes 2 or 3 arguments.
                                example: %substr("abcdef", 3, 2), return: "de" in the example"""),
                        new Snippet().title("%true").code("%true()").description("""
                                Return always true.
                                example: %true(), return: true"""),
                        new Snippet().title("%upper").code("%upper(⨁)").description("""
                                Return an uppercase string.
                                example: %upper("Hello"), return: HELLO in that example"""),
                        new Snippet().title("%variable_exists").code("%variable_exists(⨁)").description("""
                                Check if a variable exists.
                                example: %variable_exists("$my_variable"), return: true if the variable has been defined exists"""),
                        new Snippet().title("%version").code("%version()").description("""
                                Return PlantUML current version.
                                example: %version(), return: 1.2020.8 for example""")
                )
        );
    }

    @Override
    public String getTitle() {
        return "Builtin Functions";
    }
}
