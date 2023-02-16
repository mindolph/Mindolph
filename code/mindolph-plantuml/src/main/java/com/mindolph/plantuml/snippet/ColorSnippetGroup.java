package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class ColorSnippetGroup extends BaseSnippetGroup {
    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet().title("Color Gradient").code("""
                                #Red/Yellow""").description("""
                                |, /, \\, or -"""),
                        new Snippet().title("#AliceBlue").code("""
                                #AliceBlue""").color(),
                        new Snippet().title("#AntiqueWhite").code("""
                                #AntiqueWhite""").color(),
                        new Snippet().title("#Aqua").code("""
                                #Aqua""").color(),
                        new Snippet().title("#Aquamarine").code("""
                                #Aquamarine""").color(),
                        new Snippet().title("#Azure").code("""
                                #Azure""").color(),
                        new Snippet().title("#Beige").code("""
                                #Beige""").color(),
                        new Snippet().title("#Bisque").code("""
                                #Bisque""").color(),
                        new Snippet().title("#Black").code("""
                                #Black""").color(),
                        new Snippet().title("#BlanchedAlmond").code("""
                                #BlanchedAlmond""").color(),
                        new Snippet().title("#Blue").code("""
                                #Blue""").color(),
                        new Snippet().title("#BlueViolet").code("""
                                #BlueViolet""").color(),
                        new Snippet().title("#Brown").code("""
                                #Brown""").color(),
                        new Snippet().title("#BurlyWood").code("""
                                #BurlyWood""").color(),
                        new Snippet().title("#CadetBlue").code("""
                                #CadetBlue""").color(),
                        new Snippet().title("#Chartreuse").code("""
                                #Chartreuse""").color(),
                        new Snippet().title("#Chocolate").code("""
                                #Chocolate""").color(),
                        new Snippet().title("#Coral").code("""
                                #Coral""").color(),
                        new Snippet().title("#CornflowerBlue").code("""
                                #CornflowerBlue""").color(),
                        new Snippet().title("#Cornsilk").code("""
                                #Cornsilk""").color(),
                        new Snippet().title("#Crimson").code("""
                                #Crimson""").color(),
                        new Snippet().title("#Cyan").code("""
                                #Cyan""").color(),
                        new Snippet().title("#DarkBlue").code("""
                                #DarkBlue""").color(),
                        new Snippet().title("#DarkCyan").code("""
                                #DarkCyan""").color(),
                        new Snippet().title("#DarkGoldenRod").code("""
                                #DarkGoldenRod""").color(),
                        new Snippet().title("#DarkGray").code("""
                                #DarkGray""").color(),
                        new Snippet().title("#DarkGreen").code("""
                                #DarkGreen""").color(),
                        new Snippet().title("#DarkGrey").code("""
                                #DarkGrey""").color(),
                        new Snippet().title("#DarkKhaki").code("""
                                #DarkKhaki""").color(),
                        new Snippet().title("#DarkMagenta").code("""
                                #DarkMagenta""").color(),
                        new Snippet().title("#DarkOliveGreen").code("""
                                #DarkOliveGreen""").color(),
                        new Snippet().title("#DarkOrchid").code("""
                                #DarkOrchid""").color(),
                        new Snippet().title("#DarkRed").code("""
                                #DarkRed""").color(),
                        new Snippet().title("#DarkSalmon").code("""
                                #DarkSalmon""").color(),
                        new Snippet().title("#DarkSeaGreen").code("""
                                #DarkSeaGreen""").color(),
                        new Snippet().title("#DarkSlateBlue").code("""
                                #DarkSlateBlue""").color(),
                        new Snippet().title("#DarkSlateGray").code("""
                                #DarkSlateGray""").color(),
                        new Snippet().title("#DarkSlateGrey").code("""
                                #DarkSlateGrey""").color(),
                        new Snippet().title("#DarkTurquoise").code("""
                                #DarkTurquoise""").color(),
                        new Snippet().title("#DarkViolet").code("""
                                #DarkViolet""").color(),
                        new Snippet().title("#Darkorange").code("""
                                #Darkorange""").color(),
                        new Snippet().title("#DeepPink").code("""
                                #DeepPink""").color(),
                        new Snippet().title("#DeepSkyBlue").code("""
                                #DeepSkyBlue""").color(),
                        new Snippet().title("#DimGray").code("""
                                #DimGray""").color(),
                        new Snippet().title("#DimGrey").code("""
                                #DimGrey""").color(),
                        new Snippet().title("#DodgerBlue").code("""
                                #DodgerBlue""").color(),
                        new Snippet().title("#FireBrick").code("""
                                #FireBrick""").color(),
                        new Snippet().title("#FloralWhite").code("""
                                #FloralWhite""").color(),
                        new Snippet().title("#ForestGreen").code("""
                                #ForestGreen""").color(),
                        new Snippet().title("#Fuchsia").code("""
                                #Fuchsia""").color(),
                        new Snippet().title("#Gainsboro").code("""
                                #Gainsboro""").color(),
                        new Snippet().title("#GhostWhite").code("""
                                #GhostWhite""").color(),
                        new Snippet().title("#Gold").code("""
                                #Gold""").color(),
                        new Snippet().title("#GoldenRod").code("""
                                #GoldenRod""").color(),
                        new Snippet().title("#Gray").code("""
                                #Gray""").color(),
                        new Snippet().title("#Green").code("""
                                #Green""").color(),
                        new Snippet().title("#GreenYellow").code("""
                                #GreenYellow""").color(),
                        new Snippet().title("#Grey").code("""
                                #Grey""").color(),
                        new Snippet().title("#HoneyDew").code("""
                                #HoneyDew""").color(),
                        new Snippet().title("#HotPink").code("""
                                #HotPink""").color(),
                        new Snippet().title("#IndianRed").code("""
                                #IndianRed""").color(),
                        new Snippet().title("#Indigo").code("""
                                #Indigo""").color(),
                        new Snippet().title("#Ivory").code("""
                                #Ivory""").color(),
                        new Snippet().title("#Khaki").code("""
                                #Khaki""").color(),
                        new Snippet().title("#Lavender").code("""
                                #Lavender""").color(),
                        new Snippet().title("#LavenderBlush").code("""
                                #LavenderBlush""").color(),
                        new Snippet().title("#LawnGreen").code("""
                                #LawnGreen""").color(),
                        new Snippet().title("#LemonChiffon").code("""
                                #LemonChiffon""").color(),
                        new Snippet().title("#LightBlue").code("""
                                #LightBlue""").color(),
                        new Snippet().title("#LightCoral").code("""
                                #LightCoral""").color(),
                        new Snippet().title("#LightCyan").code("""
                                #LightCyan""").color(),
                        new Snippet().title("#LightGoldenRodYellow").code("""
                                #LightGoldenRodYellow""").color(),
                        new Snippet().title("#LightGray").code("""
                                #LightGray""").color(),
                        new Snippet().title("#LightGreen").code("""
                                #LightGreen""").color(),
                        new Snippet().title("#LightGrey").code("""
                                #LightGrey""").color(),
                        new Snippet().title("#LightPink").code("""
                                #LightPink""").color(),
                        new Snippet().title("#LightSalmon").code("""
                                #LightSalmon""").color(),
                        new Snippet().title("#LightSeaGreen").code("""
                                #LightSeaGreen""").color(),
                        new Snippet().title("#LightSkyBlue").code("""
                                #LightSkyBlue""").color(),
                        new Snippet().title("#LightSlateGray").code("""
                                #LightSlateGray""").color(),
                        new Snippet().title("#LightSlateGrey").code("""
                                #LightSlateGrey""").color(),
                        new Snippet().title("#LightSteelBlue").code("""
                                #LightSteelBlue""").color(),
                        new Snippet().title("#LightYellow").code("""
                                #LightYellow""").color(),
                        new Snippet().title("#Lime").code("""
                                #Lime""").color(),
                        new Snippet().title("#LimeGreen").code("""
                                #LimeGreen""").color(),
                        new Snippet().title("#Linen").code("""
                                #Linen""").color(),
                        new Snippet().title("#Magenta").code("""
                                #Magenta""").color(),
                        new Snippet().title("#Maroon").code("""
                                #Maroon""").color(),
                        new Snippet().title("#MediumAquaMarine").code("""
                                #MediumAquaMarine""").color(),
                        new Snippet().title("#MediumBlue").code("""
                                #MediumBlue""").color(),
                        new Snippet().title("#MediumOrchid").code("""
                                #MediumOrchid""").color(),
                        new Snippet().title("#MediumPurple").code("""
                                #MediumPurple""").color(),
                        new Snippet().title("#MediumSeaGreen").code("""
                                #MediumSeaGreen""").color(),
                        new Snippet().title("#MediumSlateBlue").code("""
                                #MediumSlateBlue""").color(),
                        new Snippet().title("#MediumSpringGreen").code("""
                                #MediumSpringGreen""").color(),
                        new Snippet().title("#MediumTurquoise").code("""
                                #MediumTurquoise""").color(),
                        new Snippet().title("#MediumVioletRed").code("""
                                #MediumVioletRed""").color(),
                        new Snippet().title("#MidnightBlue").code("""
                                #MidnightBlue""").color(),
                        new Snippet().title("#MintCream").code("""
                                #MintCream""").color(),
                        new Snippet().title("#MistyRose").code("""
                                #MistyRose""").color(),
                        new Snippet().title("#Moccasin").code("""
                                #Moccasin""").color(),
                        new Snippet().title("#NavajoWhite").code("""
                                #NavajoWhite""").color(),
                        new Snippet().title("#Navy").code("""
                                #Navy""").color(),
                        new Snippet().title("#OldLace").code("""
                                #OldLace""").color(),
                        new Snippet().title("#Olive").code("""
                                #Olive""").color(),
                        new Snippet().title("#OliveDrab").code("""
                                #OliveDrab""").color(),
                        new Snippet().title("#Orange").code("""
                                #Orange""").color(),
                        new Snippet().title("#OrangeRed").code("""
                                #OrangeRed""").color(),
                        new Snippet().title("#Orchid").code("""
                                #Orchid""").color(),
                        new Snippet().title("#PaleGoldenRod").code("""
                                #PaleGoldenRod""").color(),
                        new Snippet().title("#PaleGreen").code("""
                                #PaleGreen""").color(),
                        new Snippet().title("#PaleTurquoise").code("""
                                #PaleTurquoise""").color(),
                        new Snippet().title("#PaleVioletRed").code("""
                                #PaleVioletRed""").color(),
                        new Snippet().title("#PapayaWhip").code("""
                                #PapayaWhip""").color(),
                        new Snippet().title("#PeachPuff").code("""
                                #PeachPuff""").color(),
                        new Snippet().title("#Peru").code("""
                                #Peru""").color(),
                        new Snippet().title("#Pink").code("""
                                #Pink""").color(),
                        new Snippet().title("#Plum").code("""
                                #Plum""").color(),
                        new Snippet().title("#PowderBlue").code("""
                                #PowderBlue""").color(),
                        new Snippet().title("#Purple").code("""
                                #Purple""").color(),
                        new Snippet().title("#Red").code("""
                                #Red""").color(),
                        new Snippet().title("#RosyBrown").code("""
                                #RosyBrown""").color(),
                        new Snippet().title("#RoyalBlue").code("""
                                #RoyalBlue""").color(),
                        new Snippet().title("#SaddleBrown").code("""
                                #SaddleBrown""").color(),
                        new Snippet().title("#Salmon").code("""
                                #Salmon""").color(),
                        new Snippet().title("#SandyBrown").code("""
                                #SandyBrown""").color(),
                        new Snippet().title("#SeaGreen").code("""
                                #SeaGreen""").color(),
                        new Snippet().title("#SeaShell").code("""
                                #SeaShell""").color(),
                        new Snippet().title("#Sienna").code("""
                                #Sienna""").color(),
                        new Snippet().title("#Silver").code("""
                                #Silver""").color(),
                        new Snippet().title("#SkyBlue").code("""
                                #SkyBlue""").color(),
                        new Snippet().title("#SlateBlue").code("""
                                #SlateBlue""").color(),
                        new Snippet().title("#SlateGray").code("""
                                #SlateGray""").color(),
                        new Snippet().title("#SlateGrey").code("""
                                #SlateGrey""").color(),
                        new Snippet().title("#Snow").code("""
                                #Snow""").color(),
                        new Snippet().title("#SpringGreen").code("""
                                #SpringGreen""").color(),
                        new Snippet().title("#SteelBlue").code("""
                                #SteelBlue""").color(),
                        new Snippet().title("#Tan").code("""
                                #Tan""").color(),
                        new Snippet().title("#Teal").code("""
                                #Teal""").color(),
                        new Snippet().title("#Thistle").code("""
                                #Thistle""").color(),
                        new Snippet().title("#Tomato").code("""
                                #Tomato""").color(),
                        new Snippet().title("#Turquoise").code("""
                                #Turquoise""").color(),
                        new Snippet().title("#Violet").code("""
                                #Violet""").color(),
                        new Snippet().title("#Wheat").code("""
                                #Wheat""").color(),
                        new Snippet().title("#White").code("""
                                #White""").color(),
                        new Snippet().title("#WhiteSmoke").code("""
                                #WhiteSmoke""").color(),
                        new Snippet().title("#Yellow").code("""
                                #Yellow""").color(),
                        new Snippet().title("#YellowGreen").code("""
                                #YellowGreen""").color()

                )
        );
    }

    @Override
    public String getTitle() {
        return "Color";
    }
}
