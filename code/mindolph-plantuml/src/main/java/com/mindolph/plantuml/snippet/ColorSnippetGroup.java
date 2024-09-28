package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.ColorSnippet;
import com.mindolph.core.model.Snippet;

import java.util.Arrays;

/**
 * @author mindolph.com@gmail.com
 */
public class ColorSnippetGroup extends BaseSnippetGroup {

    @Override
    public void init() {
        super.snippets.addAll(Arrays.asList(
                        new Snippet<>().title("Color Gradient").code("""
                                #Red/Yellow""").description("""
                                |, /, \\, or -"""),
                        new ColorSnippet().title("#AliceBlue").code("""
                                #AliceBlue""").color(),
                        new ColorSnippet().title("#AntiqueWhite").code("""
                                #AntiqueWhite""").color(),
                        new ColorSnippet().title("#Aqua").code("""
                                #Aqua""").color(),
                        new ColorSnippet().title("#Aquamarine").code("""
                                #Aquamarine""").color(),
                        new ColorSnippet().title("#Azure").code("""
                                #Azure""").color(),
                        new ColorSnippet().title("#Beige").code("""
                                #Beige""").color(),
                        new ColorSnippet().title("#Bisque").code("""
                                #Bisque""").color(),
                        new ColorSnippet().title("#Black").code("""
                                #Black""").color(),
                        new ColorSnippet().title("#BlanchedAlmond").code("""
                                #BlanchedAlmond""").color(),
                        new ColorSnippet().title("#Blue").code("""
                                #Blue""").color(),
                        new ColorSnippet().title("#BlueViolet").code("""
                                #BlueViolet""").color(),
                        new ColorSnippet().title("#Brown").code("""
                                #Brown""").color(),
                        new ColorSnippet().title("#BurlyWood").code("""
                                #BurlyWood""").color(),
                        new ColorSnippet().title("#CadetBlue").code("""
                                #CadetBlue""").color(),
                        new ColorSnippet().title("#Chartreuse").code("""
                                #Chartreuse""").color(),
                        new ColorSnippet().title("#Chocolate").code("""
                                #Chocolate""").color(),
                        new ColorSnippet().title("#Coral").code("""
                                #Coral""").color(),
                        new ColorSnippet().title("#CornflowerBlue").code("""
                                #CornflowerBlue""").color(),
                        new ColorSnippet().title("#Cornsilk").code("""
                                #Cornsilk""").color(),
                        new ColorSnippet().title("#Crimson").code("""
                                #Crimson""").color(),
                        new ColorSnippet().title("#Cyan").code("""
                                #Cyan""").color(),
                        new ColorSnippet().title("#DarkBlue").code("""
                                #DarkBlue""").color(),
                        new ColorSnippet().title("#DarkCyan").code("""
                                #DarkCyan""").color(),
                        new ColorSnippet().title("#DarkGoldenRod").code("""
                                #DarkGoldenRod""").color(),
                        new ColorSnippet().title("#DarkGray").code("""
                                #DarkGray""").color(),
                        new ColorSnippet().title("#DarkGreen").code("""
                                #DarkGreen""").color(),
                        new ColorSnippet().title("#DarkGrey").code("""
                                #DarkGrey""").color(),
                        new ColorSnippet().title("#DarkKhaki").code("""
                                #DarkKhaki""").color(),
                        new ColorSnippet().title("#DarkMagenta").code("""
                                #DarkMagenta""").color(),
                        new ColorSnippet().title("#DarkOliveGreen").code("""
                                #DarkOliveGreen""").color(),
                        new ColorSnippet().title("#DarkOrchid").code("""
                                #DarkOrchid""").color(),
                        new ColorSnippet().title("#DarkRed").code("""
                                #DarkRed""").color(),
                        new ColorSnippet().title("#DarkSalmon").code("""
                                #DarkSalmon""").color(),
                        new ColorSnippet().title("#DarkSeaGreen").code("""
                                #DarkSeaGreen""").color(),
                        new ColorSnippet().title("#DarkSlateBlue").code("""
                                #DarkSlateBlue""").color(),
                        new ColorSnippet().title("#DarkSlateGray").code("""
                                #DarkSlateGray""").color(),
                        new ColorSnippet().title("#DarkSlateGrey").code("""
                                #DarkSlateGrey""").color(),
                        new ColorSnippet().title("#DarkTurquoise").code("""
                                #DarkTurquoise""").color(),
                        new ColorSnippet().title("#DarkViolet").code("""
                                #DarkViolet""").color(),
                        new ColorSnippet().title("#Darkorange").code("""
                                #Darkorange""").color(),
                        new ColorSnippet().title("#DeepPink").code("""
                                #DeepPink""").color(),
                        new ColorSnippet().title("#DeepSkyBlue").code("""
                                #DeepSkyBlue""").color(),
                        new ColorSnippet().title("#DimGray").code("""
                                #DimGray""").color(),
                        new ColorSnippet().title("#DimGrey").code("""
                                #DimGrey""").color(),
                        new ColorSnippet().title("#DodgerBlue").code("""
                                #DodgerBlue""").color(),
                        new ColorSnippet().title("#FireBrick").code("""
                                #FireBrick""").color(),
                        new ColorSnippet().title("#FloralWhite").code("""
                                #FloralWhite""").color(),
                        new ColorSnippet().title("#ForestGreen").code("""
                                #ForestGreen""").color(),
                        new ColorSnippet().title("#Fuchsia").code("""
                                #Fuchsia""").color(),
                        new ColorSnippet().title("#Gainsboro").code("""
                                #Gainsboro""").color(),
                        new ColorSnippet().title("#GhostWhite").code("""
                                #GhostWhite""").color(),
                        new ColorSnippet().title("#Gold").code("""
                                #Gold""").color(),
                        new ColorSnippet().title("#GoldenRod").code("""
                                #GoldenRod""").color(),
                        new ColorSnippet().title("#Gray").code("""
                                #Gray""").color(),
                        new ColorSnippet().title("#Green").code("""
                                #Green""").color(),
                        new ColorSnippet().title("#GreenYellow").code("""
                                #GreenYellow""").color(),
                        new ColorSnippet().title("#Grey").code("""
                                #Grey""").color(),
                        new ColorSnippet().title("#HoneyDew").code("""
                                #HoneyDew""").color(),
                        new ColorSnippet().title("#HotPink").code("""
                                #HotPink""").color(),
                        new ColorSnippet().title("#IndianRed").code("""
                                #IndianRed""").color(),
                        new ColorSnippet().title("#Indigo").code("""
                                #Indigo""").color(),
                        new ColorSnippet().title("#Ivory").code("""
                                #Ivory""").color(),
                        new ColorSnippet().title("#Khaki").code("""
                                #Khaki""").color(),
                        new ColorSnippet().title("#Lavender").code("""
                                #Lavender""").color(),
                        new ColorSnippet().title("#LavenderBlush").code("""
                                #LavenderBlush""").color(),
                        new ColorSnippet().title("#LawnGreen").code("""
                                #LawnGreen""").color(),
                        new ColorSnippet().title("#LemonChiffon").code("""
                                #LemonChiffon""").color(),
                        new ColorSnippet().title("#LightBlue").code("""
                                #LightBlue""").color(),
                        new ColorSnippet().title("#LightCoral").code("""
                                #LightCoral""").color(),
                        new ColorSnippet().title("#LightCyan").code("""
                                #LightCyan""").color(),
                        new ColorSnippet().title("#LightGoldenRodYellow").code("""
                                #LightGoldenRodYellow""").color(),
                        new ColorSnippet().title("#LightGray").code("""
                                #LightGray""").color(),
                        new ColorSnippet().title("#LightGreen").code("""
                                #LightGreen""").color(),
                        new ColorSnippet().title("#LightGrey").code("""
                                #LightGrey""").color(),
                        new ColorSnippet().title("#LightPink").code("""
                                #LightPink""").color(),
                        new ColorSnippet().title("#LightSalmon").code("""
                                #LightSalmon""").color(),
                        new ColorSnippet().title("#LightSeaGreen").code("""
                                #LightSeaGreen""").color(),
                        new ColorSnippet().title("#LightSkyBlue").code("""
                                #LightSkyBlue""").color(),
                        new ColorSnippet().title("#LightSlateGray").code("""
                                #LightSlateGray""").color(),
                        new ColorSnippet().title("#LightSlateGrey").code("""
                                #LightSlateGrey""").color(),
                        new ColorSnippet().title("#LightSteelBlue").code("""
                                #LightSteelBlue""").color(),
                        new ColorSnippet().title("#LightYellow").code("""
                                #LightYellow""").color(),
                        new ColorSnippet().title("#Lime").code("""
                                #Lime""").color(),
                        new ColorSnippet().title("#LimeGreen").code("""
                                #LimeGreen""").color(),
                        new ColorSnippet().title("#Linen").code("""
                                #Linen""").color(),
                        new ColorSnippet().title("#Magenta").code("""
                                #Magenta""").color(),
                        new ColorSnippet().title("#Maroon").code("""
                                #Maroon""").color(),
                        new ColorSnippet().title("#MediumAquaMarine").code("""
                                #MediumAquaMarine""").color(),
                        new ColorSnippet().title("#MediumBlue").code("""
                                #MediumBlue""").color(),
                        new ColorSnippet().title("#MediumOrchid").code("""
                                #MediumOrchid""").color(),
                        new ColorSnippet().title("#MediumPurple").code("""
                                #MediumPurple""").color(),
                        new ColorSnippet().title("#MediumSeaGreen").code("""
                                #MediumSeaGreen""").color(),
                        new ColorSnippet().title("#MediumSlateBlue").code("""
                                #MediumSlateBlue""").color(),
                        new ColorSnippet().title("#MediumSpringGreen").code("""
                                #MediumSpringGreen""").color(),
                        new ColorSnippet().title("#MediumTurquoise").code("""
                                #MediumTurquoise""").color(),
                        new ColorSnippet().title("#MediumVioletRed").code("""
                                #MediumVioletRed""").color(),
                        new ColorSnippet().title("#MidnightBlue").code("""
                                #MidnightBlue""").color(),
                        new ColorSnippet().title("#MintCream").code("""
                                #MintCream""").color(),
                        new ColorSnippet().title("#MistyRose").code("""
                                #MistyRose""").color(),
                        new ColorSnippet().title("#Moccasin").code("""
                                #Moccasin""").color(),
                        new ColorSnippet().title("#NavajoWhite").code("""
                                #NavajoWhite""").color(),
                        new ColorSnippet().title("#Navy").code("""
                                #Navy""").color(),
                        new ColorSnippet().title("#OldLace").code("""
                                #OldLace""").color(),
                        new ColorSnippet().title("#Olive").code("""
                                #Olive""").color(),
                        new ColorSnippet().title("#OliveDrab").code("""
                                #OliveDrab""").color(),
                        new ColorSnippet().title("#Orange").code("""
                                #Orange""").color(),
                        new ColorSnippet().title("#OrangeRed").code("""
                                #OrangeRed""").color(),
                        new ColorSnippet().title("#Orchid").code("""
                                #Orchid""").color(),
                        new ColorSnippet().title("#PaleGoldenRod").code("""
                                #PaleGoldenRod""").color(),
                        new ColorSnippet().title("#PaleGreen").code("""
                                #PaleGreen""").color(),
                        new ColorSnippet().title("#PaleTurquoise").code("""
                                #PaleTurquoise""").color(),
                        new ColorSnippet().title("#PaleVioletRed").code("""
                                #PaleVioletRed""").color(),
                        new ColorSnippet().title("#PapayaWhip").code("""
                                #PapayaWhip""").color(),
                        new ColorSnippet().title("#PeachPuff").code("""
                                #PeachPuff""").color(),
                        new ColorSnippet().title("#Peru").code("""
                                #Peru""").color(),
                        new ColorSnippet().title("#Pink").code("""
                                #Pink""").color(),
                        new ColorSnippet().title("#Plum").code("""
                                #Plum""").color(),
                        new ColorSnippet().title("#PowderBlue").code("""
                                #PowderBlue""").color(),
                        new ColorSnippet().title("#Purple").code("""
                                #Purple""").color(),
                        new ColorSnippet().title("#Red").code("""
                                #Red""").color(),
                        new ColorSnippet().title("#RosyBrown").code("""
                                #RosyBrown""").color(),
                        new ColorSnippet().title("#RoyalBlue").code("""
                                #RoyalBlue""").color(),
                        new ColorSnippet().title("#SaddleBrown").code("""
                                #SaddleBrown""").color(),
                        new ColorSnippet().title("#Salmon").code("""
                                #Salmon""").color(),
                        new ColorSnippet().title("#SandyBrown").code("""
                                #SandyBrown""").color(),
                        new ColorSnippet().title("#SeaGreen").code("""
                                #SeaGreen""").color(),
                        new ColorSnippet().title("#SeaShell").code("""
                                #SeaShell""").color(),
                        new ColorSnippet().title("#Sienna").code("""
                                #Sienna""").color(),
                        new ColorSnippet().title("#Silver").code("""
                                #Silver""").color(),
                        new ColorSnippet().title("#SkyBlue").code("""
                                #SkyBlue""").color(),
                        new ColorSnippet().title("#SlateBlue").code("""
                                #SlateBlue""").color(),
                        new ColorSnippet().title("#SlateGray").code("""
                                #SlateGray""").color(),
                        new ColorSnippet().title("#SlateGrey").code("""
                                #SlateGrey""").color(),
                        new ColorSnippet().title("#Snow").code("""
                                #Snow""").color(),
                        new ColorSnippet().title("#SpringGreen").code("""
                                #SpringGreen""").color(),
                        new ColorSnippet().title("#SteelBlue").code("""
                                #SteelBlue""").color(),
                        new ColorSnippet().title("#Tan").code("""
                                #Tan""").color(),
                        new ColorSnippet().title("#Teal").code("""
                                #Teal""").color(),
                        new ColorSnippet().title("#Thistle").code("""
                                #Thistle""").color(),
                        new ColorSnippet().title("#Tomato").code("""
                                #Tomato""").color(),
                        new ColorSnippet().title("#Turquoise").code("""
                                #Turquoise""").color(),
                        new ColorSnippet().title("#Violet").code("""
                                #Violet""").color(),
                        new ColorSnippet().title("#Wheat").code("""
                                #Wheat""").color(),
                        new ColorSnippet().title("#White").code("""
                                #White""").color(),
                        new ColorSnippet().title("#WhiteSmoke").code("""
                                #WhiteSmoke""").color(),
                        new ColorSnippet().title("#Yellow").code("""
                                #Yellow""").color(),
                        new ColorSnippet().title("#YellowGreen").code("""
                                #YellowGreen""").color()

                )
        );
    }

    @Override
    public String getTitle() {
        return "Color";
    }
}
