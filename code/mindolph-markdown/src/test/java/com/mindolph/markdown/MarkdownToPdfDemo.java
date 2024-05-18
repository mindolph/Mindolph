package com.mindolph.markdown;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;

public class MarkdownToPdfDemo {
    final static String hh = """
            <html>
            <head>
            nonLatinFonts
            <title>Examples</title>
            <style>

            body {
              font-family: 'sans-cjk', sans-serif;
            }
            var,
            code,
            kbd,
            pre {
                font: 0.9em 'sans-cjk', Consolas, "Liberation Mono", Menlo, Courier, monospace;
            }
            </style>
            </head>
            <body>
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
        File test = new File(SystemUtils.getUserHome(), "test.pdf");
        System.out.println(test);
        OutputStream os = new FileOutputStream(test);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(() -> {
            try {
                return new FileInputStream("/Users/allen/Library/Fonts/NotoSansSC-VariableFont_wght.ttf");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }, "sans-cjk", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.withHtmlContent(hh, null);
        builder.toStream(os);
        builder.run();
    }
}
