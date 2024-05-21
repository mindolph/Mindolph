package com.mindolph.markdown;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;

/**
 * To test this, setup sans-font-file path and mono-font-file path in environment variable.
 */
public class MarkdownToPdfDemo {
    final static String hh = """
            <html>
            <head>
            nonLatinFonts
            <title>Examples</title>
            <style>
            body {
              font-family: 'sans-pdf', sans-serif;
            }
            var,
            code,
            kbd,
            pre {
                font: 0.9em 'mono-pdf', Consolas, "Liberation Mono", Menlo, Courier, monospace;
            }
            </style>
            </head>
            <body class="markdown-body">
                <div>
                    <h1>  你好！おはよう   <small>Subtext for header</small></h1>
                </div>
                <pre><code>
                hello witch
                中文显示正常
                </code></pre>
            </body>
            </html>
            """;

    public static void main(String[] args) throws Exception {
        String sansFontFile = SystemUtils.getEnvironmentVariable("sans-font-file", "");
        String monoFontFile = SystemUtils.getEnvironmentVariable("mono-font-file", "");
        if (StringUtils.isAnyBlank(sansFontFile, monoFontFile)) {
            System.out.println("WARN: sans-font-file and mono-font-file are blank");
            return;
        }
        System.out.println("Using sans font: " + sansFontFile);
        System.out.println("Using mono-font: " + monoFontFile);
        File test = new File(SystemUtils.getUserHome(), "test.pdf");
        System.out.println(test);
        OutputStream os = new FileOutputStream(test);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(() -> {
            try {
                return new FileInputStream(sansFontFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, "sans-pdf", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(()-> {
            try {
                return new FileInputStream(monoFontFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, "mono-pdf", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.withHtmlContent(hh, null);
        builder.toStream(os);
        builder.run();
    }
}
