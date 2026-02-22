/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModelUtils {

    public static final Comparator<String> STRING_COMPARATOR = new StringComparator();
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

    private static final Pattern UNESCAPE_BR = Pattern.compile("(?i)\\<\\s*?br\\s*?\\/?\\>");
    private static final Pattern MD_ESCAPED_PATTERN =
            Pattern.compile("(\\\\[\\\\`*_{}\\[\\]()#<>+-.!])");
    private static final String MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!";
    private static final Pattern URI_QUERY_PARAMETERS = Pattern.compile("\\&?([^=]+)=([^&]*)");


    private ModelUtils() {
    }

    public static int calcCharsOnStart(char chr, String text) {
        int result = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == chr) {
                result++;
            }
            else {
                break;
            }
        }
        return result;
    }

    public static String makePreBlock(String text) {
        return String.format("<pre>%s</pre>", escapeTextForPreBlock(text));
    }


    public static String escapeTextForPreBlock(String text) {
        int length = text.length();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);

            switch (chr) {
                case '\"':
                    result.append("&quot;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                default: {
                    result.append(chr);
                }
                break;
            }
        }

        return result.toString();
    }


    public static String makeMDCodeBlock(String text) throws IOException {
        int maxQuotes = calcMaxLengthOfBacktickQuotesSubstr(text) + 1;
        StringBuilder result = new StringBuilder(text.length() + 16);
        writeChar(result, '`', maxQuotes);
        result.append(text);
        writeChar(result, '`', maxQuotes);
        return result.toString();
    }


    public static String escapeMarkdownStr(String text) {
        StringBuilder buffer = new StringBuilder(text.length() * 2);
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                buffer.append("<br/>");
                continue;
            }
            else if (Character.isISOControl(c)) {
                continue;
            }
            else if (MD_ESCAPED_CHARS.indexOf(c) >= 0) {
                buffer.append('\\');
            }

            buffer.append(c);
        }
        return buffer.toString();
    }

    public static int calcMaxLengthOfBacktickQuotesSubstr(String text) {
        int result = 0;
        if (text != null) {
            int pos = 0;
            while (pos >= 0) {
                pos = text.indexOf('`', pos);
                if (pos >= 0) {
                    int found = 0;
                    while (pos < text.length() && text.charAt(pos) == '`') {
                        found++;
                        pos++;
                    }
                    result = Math.max(result, found);
                }
            }
        }
        return result;
    }

    public static void writeChar(Appendable out, char chr, int times)
            throws IOException {
        for (int i = 0; i < times; i++) {
            out.append(chr);
        }
    }


    public static String unescapeMarkdownStr(String text) {
        String unescaped = UNESCAPE_BR.matcher(text).replaceAll("\n");
        StringBuilder result = new StringBuilder(text.length());
        Matcher escaped = MD_ESCAPED_PATTERN.matcher(unescaped);
        while (escaped.find()) {
            String group = escaped.group(1);
            escaped.appendReplacement(result, Matcher.quoteReplacement(group.substring(1)));
        }
        escaped.appendTail(result);
        return result.toString();
    }


    public static String makeShortTextVersion(String text, int maxLength) {
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength) + "...";
        }
        return text;
    }

    public static int countLines(String text) {
        int result = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                result++;
            }
        }
        return result;
    }

    public static String makeQueryStringForURI(Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();

        List<String> orderedkeys = new ArrayList<>(properties.stringPropertyNames());
        Collections.sort(orderedkeys);

        for (String k : orderedkeys) {
            try {
                String encodedKey = URLEncoder.encode(k, "UTF-8");
                String encodedValue = URLEncoder.encode(properties.getProperty(k), "UTF-8");

                if (!buffer.isEmpty()) {
                    buffer.append('&');
                }
                buffer.append(encodedKey).append('=').append(encodedValue);
            } catch (UnsupportedEncodingException ex) {
                LOGGER.error("Can't encode URI query", ex);
                throw new Error("Unexpected exception, can't find UTF-8 charset!");
            }
        }
        return buffer.toString();
    }


    public static Properties extractQueryPropertiesFromURI(URI uri) {
        Properties result = new Properties();

        String rawQuery = uri.getRawQuery();
        if (rawQuery != null) {
            Matcher matcher = URI_QUERY_PARAMETERS.matcher(rawQuery);

            try {
                while (matcher.find()) {
                    String key = URLDecoder.decode(matcher.group(1), "UTF-8");
                    String value = URLDecoder.decode(matcher.group(2), "UTF-8");
                    result.put(key, value);
                }
            } catch (UnsupportedEncodingException ex) {
                LOGGER.error("Can't decode URI query", ex);
                throw new Error("Unexpected exception, can't find UTF-8 charset!");
            }
        }

        return result;
    }


    private static String char2UriHexByte(char ch) {
        String s = Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
        return '%' + (s.length() < 2 ? "0" : "") + s;
    }


    public static String encodeForURI(String s) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || "-_.~".indexOf(c) >= 0) {
                result.append(c);
            }
            else {
                if (":/?#[]@!$^'()*+,;= ".indexOf(c) >= 0) {
                    result.append(char2UriHexByte(c));
                }
                else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }


    public static File makeFileForPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("file:")) {
            try {
                return new File(new URI(normalizeFileURI(path)));
            } catch (URISyntaxException ex) {
                LOGGER.error("URISyntaxException for " + path, ex);
                return null;
            }
        }
        else {
            return new File(path);
        }
    }


    public static String escapeURIPath(String text) {
        String chars = "% :<>?";
        String result = text;
        for (char ch : chars.toCharArray()) {
            result = result.replace(Character.toString(ch),
                    "%" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
        }

        return result;
    }


    public static String removeISOControls(String text) {
        StringBuilder result = null;
        boolean detected = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (detected) {
                if (!Character.isISOControl(ch)) {
                    result.append(ch);
                }
            }
            else {
                if (Character.isISOControl(ch)) {
                    detected = true;
                    result = new StringBuilder(text.length());
                    result.append(text, 0, i);
                }
            }
        }
        return detected ? result.toString() : text;
    }


    private static String normalizeFileURI(String fileUri) {
        int schemePosition = fileUri.indexOf(':');
        String scheme =
                schemePosition < 0 ? "" : fileUri.substring(0, schemePosition + 1);
        String chars = " :<>?";
        String result = fileUri.substring(scheme.length());
        for (char ch : chars.toCharArray()) {
            result = result.replace(Character.toString(ch),
                    "%" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH));
        }
        return scheme + result;
    }


    public static URI toURI(Path path) {
        if (path == null) {
            return null;
        }
        try {
            StringBuilder buffer = new StringBuilder();

            Path root = path.getRoot();
            if (root != null) {
                buffer.append(root.toString().replace('\\', '/'));
            }

            for (Path p : path) {
                if (!buffer.isEmpty() && buffer.charAt(buffer.length() - 1) != '/') {
                    buffer.append('/');
                }
                buffer.append(encodeForURI(p.toFile().getName()));
            }

            if (path.isAbsolute()) {
                buffer.insert(0, "file://" + (root == null ? "/" : ""));
            }

            return new URI(buffer.toString());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't convert path to URI: " + path, ex);
        }
    }


    public static File toFile(URI uri) {
        List<String> pathItems = new ArrayList<>();

        String authority = uri.getAuthority();
        if (authority != null && !authority.isEmpty()) {
            pathItems.add(authority);
        }

        String[] splittedPath = uri.getPath().split("\\/");
        boolean separator = false;
        if (splittedPath.length == 0) {
            separator = true;
        }
        else {
            for (String s : splittedPath) {
                if (!s.isEmpty()) {
                    pathItems.add(separator ? File.separatorChar + s : s);
                    separator = false;
                }
                else {
                    separator = true;
                }
            }
        }

        if (separator) {
            pathItems.add(File.separator);
        }

        String[] fullArray = pathItems.toArray(new String[0]);
        String[] next = Arrays.copyOfRange(fullArray, 1, fullArray.length);
        return Paths.get(fullArray[0], next).toFile();
    }

    public static String removeAllISOControls(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (Character.isISOControl(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static String removeAllISOControlsButTabs(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (c != '\t' && Character.isISOControl(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    private static final class StringComparator implements Comparator<String>, Serializable {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

}
