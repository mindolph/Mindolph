package com.mindolph.core.template;

import com.mindolph.core.util.FileNameUtils;
import com.mindolph.core.util.ResourceUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.mindolph.core.template.ResourcePathType.ABSOLUTE;
import static com.mindolph.core.template.ResourcePathType.RELATIVE;

/**
 *
 */
public final class HtmlBuilder {

    private static final String TOOL_SCRIPT = """
            function getViewportHeight() {
                return window.innerHeight;
            }
            function getTotalHeight() {
                return document.documentElement.scrollHeight;
            }
            function getScrollPosX() {
                return document.scrollingElement.scrollLeft;
            }
            function getScrollPosY() {
                return document.scrollingElement.scrollTop;
            }
            function setScrollPos(xPos, yPos) {
                window.scrollTo(xPos, yPos);
            }
            console.log("scripts are ready");
            """;

    private final Logger log = LoggerFactory.getLogger(HtmlBuilder.class);

    private final String content;

    private String title;
    private String css;

    private String script = StringUtils.EMPTY;

    private String onLoadFunction = StringUtils.EMPTY;

    private File resourceBaseDir;
    private ResourcePathType resourcePathType = ABSOLUTE;

    private String markdownClassName;

    private File pdfFontFile;

    private Set<File> images;

    /**
     * HTML content, body will be removed if provided.
     *
     * @param content
     */
    public HtmlBuilder(String content) {
        this.content = content;
    }

    /**
     * Title in html header.
     *
     * @param title
     * @return
     */
    public HtmlBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * CSS content to be applied to html header.
     *
     * @param cssResourceUri
     * @return
     */
    public HtmlBuilder css(String cssResourceUri) {
        this.css = ResourceUtils.readResourceToString(cssResourceUri);
        return this;
    }

    /**
     * @param script         Script content to be applied to html header.
     * @param onLoadFunction Function to be called when html body loaded.
     * @return
     */
    public HtmlBuilder script(String script, String onLoadFunction) {
        this.script = script;
        this.onLoadFunction = onLoadFunction;
        return this;
    }

    /**
     * Convert all image's relative path to absolute path.
     * (to display or export)
     *
     * @param baseDir
     * @return
     */
    public HtmlBuilder absoluteUri(File baseDir) {
        this.resourceBaseDir = baseDir;
        this.resourcePathType = ABSOLUTE;
        return this;
    }

    /**
     * Convert all image path to a relative path 'images/xxx'.
     * call getImages() to get all existing image {@code File}s.
     *
     * @param baseDir
     * @return
     */
    public HtmlBuilder relativeUri(File baseDir) {
        this.resourceBaseDir = baseDir;
        this.resourcePathType = RELATIVE;
        return this;
    }

    /**
     * Class name for rendering markdown.
     *
     * @param className
     * @return
     */
    public HtmlBuilder markdown(String className) {
        this.markdownClassName = className;
        return this;
    }

    /**
     * Font file for exporting PDF with correct font.
     *
     * @param fontFile
     * @return
     */
    public HtmlBuilder pdf(File fontFile) {
        this.pdfFontFile = fontFile;
        return this;
    }

    public Set<File> getImages() {
        return images;
    }

    /**
     * @param resourceToken token that is used to determine whether the resource is really changed.
     * @return
     */
    public String build(String resourceToken) {
        String ret = null;
//        System.out.println(content);
        ret = StringUtils.substringBetween(content, "<body>", "</body>");
//        System.out.println(ret);
        if (ret == null) {
            ret = content;
        }

        if (resourceBaseDir != null) {
            org.jsoup.nodes.Document doc = Jsoup.parse(ret);
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            Elements imgs = doc.select("img");
            for (Element img : imgs) {
                String src = img.attr("src");
                if (!FileNameUtils.isAbsolutePath(src)) {
                    // is relative path
                    src = StringUtils.startsWith(src, "./") ? StringUtils.substring(src, 2) : src;
                    src = new File(resourceBaseDir, src).getPath();
                }
                log.trace("find markdown resource: " + src);
                if (resourcePathType == ABSOLUTE) {
                    log.trace(String.format("absolute path: %s%n", src));
                    img.attr("src", "file://%s?%s".formatted(src, resourceToken));
                } else if (resourcePathType == ResourcePathType.RELATIVE) {
                    String fileName = FilenameUtils.getName(src);
                    img.attr("src", "images/%s?%s".formatted(fileName, resourceToken));
                    File imgFile = new File(src);
                    if (imgFile.exists()) {
                        if (images == null) images = new HashSet<>();
                        images.add(imgFile);
                    }
                }
            }
            ret = doc.html();
        }

        if (StringUtils.isNotBlank(this.markdownClassName)) {
//            styles = """
//                    <link rel="stylesheet" href="https://github.com/sindresorhus/github-markdown-css/blob/main/github-markdown.css"/>
//                    """;
            ret = """
                    <article class="markdown-body">
                    %s
                    </article>
                    """.formatted(ret);
        }

        String styles = null;
        if (StringUtils.isNotBlank(css)) {
            styles = """
                    <style>
                    %s
                    </style>
                    """.formatted(css);
        }
        if (pdfFontFile != null && pdfFontFile.exists()) {
            String fontFileUri;
            if (SystemUtils.IS_OS_WINDOWS) {
                // for Windows file URI: replace it's '\' to '/' and add an extra '/' as prefix.
                fontFileUri = "/" + StringUtils.replace(pdfFontFile.getPath(), "\\", "/");
            } else {
                fontFileUri = pdfFontFile.getPath();
            }
            styles = """
                    %s
                    <style>
                    @font-face {
                      font-family: 'pdf-font';
                      src: url('file://%s');
                      font-weight: normal;
                      font-style: normal;
                    }
                    * {
                      font-family: 'pdf-font', sans-serif;
                    }
                    </style>
                    """.formatted(styles, fontFileUri);
        }
        if (StringUtils.isNotBlank(title)) {
            ret = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta charset="utf-8"/>
                    <title>%s</title>
                    <script>
                    %s
                    </script>
                    <script>
                    %s
                    function onload() {
                        console.log('onload()');
                        console.log('init scroll position');
                        %s();
                        console.log('register onscroll event handler');
                        setTimeout(()=> {
                            window.onscroll = function() {
                                console.log('onscroll()');
                                var scrolling = document.scrollingElement;
                                window.scrollListener.onWebviewScroll(scrolling.scrollLeft, scrolling.scrollTop);
                            }
                        }, 500); // setTimeout is workaround to let scroll goes before listening.
                        // register hover event for all links
                        const v = document.getElementsByTagName("a")
                        for (let i = 0; i < v.length; i++) {
                            let link = v.item(i);
                            link.onmouseenter = () => {
                                window.hoverListener.onHover(link.href)
                            }
                        }
                    }
                    </script>
                    %s
                    </head>
                    <body onload='onload()'>
                    %s
                    </body>
                    </html>
                    """.formatted(title, TOOL_SCRIPT, script, onLoadFunction, styles, ret);
        }
        return ret;
    }

}
