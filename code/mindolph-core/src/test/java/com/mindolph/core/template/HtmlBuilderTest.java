package com.mindolph.core.template;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * To test this, setup sans-font-file path and mono-font-file path in environment variable.
 */
public class HtmlBuilderTest {

    @Test
    public void testToHtmlFile() {
        String sansFontFile = SystemUtils.getEnvironmentVariable("sans-font-file", "");
        String monoFontFile = SystemUtils.getEnvironmentVariable("mono-font-file", "");

        if (StringUtils.isAnyBlank(sansFontFile, monoFontFile)) {
            System.out.println("WARN: sans-font-file and mono-font-file are blank");
            return;
        }

        String html = new HtmlBuilder("<body><h1>世界真奇妙</h1><p>こにちわ</p></body>")
                .title("exported html file")
                .css("style/markdown_preview_github.css")
                .script("function func(a){return b;}", "func")
                .markdown("markdown-body")
                .pdf(new File(sansFontFile),
                        new File(monoFontFile))
                .build(String.valueOf(System.currentTimeMillis()));
        System.out.println(html);
        File file = new File(SystemUtils.getJavaIoTmpDir(), "test_html_builder.html");
        System.out.println(file);
        try {
            FileUtils.writeStringToFile(file, html, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
