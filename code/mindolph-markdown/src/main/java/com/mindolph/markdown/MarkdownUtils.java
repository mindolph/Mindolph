package com.mindolph.markdown;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class MarkdownUtils {

    private static final String nonLatinFonts = """
            @font-face {
              font-family: 'noto-cjk';
              src: url('NotoSansCJK-Regular.ttf');
              font-weight: normal;
              font-style: normal;
            }

            @font-face {
              font-family: 'noto-serif';
              src: url('NotoSerifCJK-Regular.ttf');
              font-weight: normal;
              font-style: normal;
            }

            @font-face {
              font-family: 'noto-sans';
              src: url('NotoSansCJK-Regular.ttf');
              font-weight: normal;
              font-style: normal;
            }

            @font-face {
              font-family: 'noto-mono';
              src: url('NotoSansCJK-Regular.ttf');
              font-weight: normal;
              font-style: normal;
            }

            body {
                font-family: 'noto-sans', 'noto-cjk', sans-serif;
                overflow: hidden;
                word-wrap: break-word;
                font-size: 14px;
            }

            var,
            code,
            kbd,
            pre {
                font: 0.9em 'noto-mono', Consolas, "Liberation Mono", Menlo, Courier, monospace;
            }
            """;

    public static String makeExportStyles() {
//        File d = new File(SystemUtils.getUserHome(), "Library/Fonts/");
        File d = new File("/usr/share/fonts/opentype/noto/");
        System.out.println("find font in :" + d.getPath());
        return StringUtils.replace(nonLatinFonts, "${font_file_path}", d.getPath());
    }
}
