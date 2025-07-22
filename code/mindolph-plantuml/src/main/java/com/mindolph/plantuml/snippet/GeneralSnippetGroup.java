package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.core.model.Snippet;

import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class GeneralSnippetGroup extends BasePlantUmlSnippetGroup {

    private static final List<Snippet> general = Arrays.asList(
            new ImageSnippet().title("Title").code("""
                    title
                     <u>This</u> title
                     on <i>several</i> lines and using <back:cadetblue>creole tags</back>
                    end title
                    """).generateImage(),
            new ImageSnippet().title("Header and footer").code("""
                    header
                    <font color=red>Header:</font>
                    This is a header
                    endheader
                    center footer "this is a center footer"
                    """).generateImage(),
            new ImageSnippet().title("Caption").code("""
                    "caption this is a caption"
                    """).generateImage(),
            new ImageSnippet().title("Legend").code("""
                    legend top left
                      This is a legend
                    endlegend
                    """).generateImage(),
            new ImageSnippet().title("Block Comment").code("""
                    /'
                    many lines comments
                    here⨁
                    '/
                    """).generateImage(),
            new ImageSnippet().title("Style").code("""
                    <style>
                    ⨁
                    </style>
                    """),
            new ImageSnippet().title("Mainframe").code("""
                    mainframe This is a **mainframe**
                    """).generateImage()
    );

    // https://plantuml.com/zh/style
    private static final List<Snippet> css = Arrays.asList(
            new Snippet().title("CSS Style").code("""
                    <style>
                    ⨁
                    </style>
                    """).description("PlantUML now supports CSS-like styling through the <style> tag, providing precise control over diagram elements such as participants, arrows, and global settings. This new feature is set to replace the older skinparam approach, which is now deprecated."),
            new Snippet().title("CSS: FontName").code("FontName: ⨁").description("Sets the font family for text elements."),
            new Snippet().title("CSS: FontColor").code("FontColor: ⨁").description("Sets the color of the text."),
            new Snippet().title("CSS: FontSize").code("FontSize: ⨁").description("Specifies the size of the text."),
            new Snippet().title("CSS: FontStyle").code("FontStyle: ⨁").description("Defines the text style (e.g., bold, italic, normal)."),
            new Snippet().title("CSS: BackGroundColor").code("BackGroundColor: ⨁").description("Sets the background color of an element."),
            new Snippet().title("CSS: HyperLinkColor").code("HyperLinkColor: ⨁").description("Sets the color used for hyperlinks."),
            new Snippet().title("CSS: RoundCorner").code("RoundCorner: ⨁").description("Sets the radius for rounding the corners of elements."),
            new Snippet().title("CSS: DiagonalCorner").code("DiagonalCorner: ⨁").description("Applies a diagonal cut effect to element corners."),
            new Snippet().title("CSS: LineColor").code("LineColor: ⨁").description("Specifies the color of lines or borders."),
            new Snippet().title("CSS: LineThickness").code("LineThickness: ⨁").description("Sets the thickness of lines or borders."),
            new Snippet().title("CSS: LineStyle").code("LineStyle: ⨁").description("Defines the line style (solid, dashed, dotted, etc.)."),
            new Snippet().title("CSS: Padding").code("Padding: ⨁").description("Sets the internal spacing within an element."),
            new Snippet().title("CSS: Margin").code("Margin: ⨁").description("Defines the external spacing around an element."),
            new Snippet().title("CSS: MaximumWidth").code("MaximumWidth: ⨁").description("Specifies the maximum width an element can occupy."),
            new Snippet().title("CSS: Shadowing").code("Shadowing: ⨁").description("Adds a shadow effect by specifying a numeric value for the shadow distance."),
            new Snippet().title("CSS: HyperlinkUnderlineStyle").code("HyperlinkUnderlineStyle: ⨁").description("Specifies the underline style for hyperlinks."),
            new Snippet().title("CSS: HyperlinkUnderlineThickness").code("HyperlinkUnderlineThickness: ⨁").description("Specifies the thickness of the hyperlink underline."),
            new Snippet().title("CSS: HorizontalAlignment").code("HorizontalAlignment: ⨁").description("Aligns content horizontally (left, center, or right).")
    );

    private static final List<Snippet> skinparam = Arrays.asList(
            new Snippet().title("Skinparam: Shadowing").code("""
                    skinparam shadowing true
                    """),
            new Snippet().title("Skinparam: Reverse Color").code("""
                    skinparam monochrome reverse
                    """),
            new Snippet().title("Skinparam: Change Default Font Name").code("""
                    skinparam defaultFontName ⨁
                    """),
            new Snippet().title("Skinparam: Font Name").code("""
                    skinparam classFontName ⨁
                    """),
            new Snippet().title("Skinparam: Font Color").code("""
                    skinparam classFontColor ⨁
                    """),
            new Snippet().title("Skinparam: Font Size").code("""
                    skinparam classFontSize ⨁
                    """),
            new Snippet().title("Skinparam: Text Alignment: Message").code("""
                    skinparam sequenceMessageAlign ⨁
                    """).description("""
                    Text alignment can be set to left, right or center in skinparam sequenceMessageAlign.
                    You can also use direction or reverseDirection values to align text depending on arrow direction.
                    """),
            new Snippet().title("Skinparam: Text Alignment: Reference").code("""
                    skinparam sequenceReferenceAlign ⨁
                    """).description("""
                    Text alignment can be set to left, right or center in skinparam sequenceMessageAlign.
                    You can also use direction or reverseDirection values to align text depending on arrow direction.
                    """)
    );

    @Override
    public String getTitle() {
        return "General&Styles";
    }

    @Override
    public void init() {
        super.snippets.addAll(general);
        super.snippets.addAll(css);
        super.snippets.addAll(skinparam);
    }
}
