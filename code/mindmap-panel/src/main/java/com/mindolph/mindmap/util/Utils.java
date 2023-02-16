package com.mindolph.mindmap.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Utils {

    public static final String PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE = "mmap.max.image.side.size";
    public static final boolean LTR_LANGUAGE = ComponentOrientation.getOrientation(new Locale(System.getProperty("user.language"))).isLeftToRight();
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final Pattern URI_PATTERN = Pattern.compile("^(?:([^:\\s]+):)(?://(?:[^?/@\\s]*@)?([^/?\\s]*)/?)?([^?\\s]+)?(?:\\?([^#\\s]*))?(?:#\\S*)?$");
    private static final double MAX_IMAGE_SIDE_SIZE_IN_PIXELS = 350;

    private Utils() {
    }

    /**
     * Get input stream for resource in zip file.
     *
     * @param zipFile      zip file
     * @param resourcePath path to resource
     * @return input stream for resource or null if not found or directory
     * @throws IOException if there is any transport error
     */
    public static InputStream findInputStreamForResource(ZipFile zipFile, String resourcePath) throws IOException {
        ZipEntry entry = zipFile.getEntry(resourcePath);
        InputStream result = null;
        if (entry != null && !entry.isDirectory()) {
            result = zipFile.getInputStream(entry);
        }
        return result;
    }

    /**
     * Read who;e zip item into byte array.
     *
     * @param zipFile zip file
     * @param path    path to resource
     * @return byte array or null if not found
     * @throws IOException thrown if there is any transport error
     */
    public static byte[] toByteArray(ZipFile zipFile, String path) throws IOException {
        InputStream in = findInputStreamForResource(zipFile, path);
        byte[] result = null;
        if (in != null) {
            try {
                result = IOUtils.toByteArray(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return result;
    }


    /**
     * Allows to check that some string has URI in appropriate format.
     *
     * @param uri string to be checked, must not be null
     * @return true if the string contains correct uri, false otherwise
     */
    public static boolean isUriCorrect(String uri) {
        return URI_PATTERN.matcher(uri).matches();
    }

    /**
     * Get max image size.
     *
     * @return max image size
     * @see #MAX_IMAGE_SIDE_SIZE_IN_PIXELS
     * @see #PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE
     */
    public static double getMaxImageSize() {
        double result = MAX_IMAGE_SIDE_SIZE_IN_PIXELS;
        try {
            String defined = System.getProperty(PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE);
            if (defined != null) {
                LOG.info("Detected redefined max size for embedded image side : " + defined);
                result = Math.max(8, Double.parseDouble(defined.trim()));
            }
        } catch (NumberFormatException ex) {
            LOG.error("Error during image size decoding : ", ex);
        }
        return result;
    }

    public static boolean isPlantUmlFileExtension(String lowerCasedTrimmedExtension) {
        boolean result = false;
        if (lowerCasedTrimmedExtension.length() > 1 && lowerCasedTrimmedExtension.charAt(0) == 'p') {
            result = "pu".equals(lowerCasedTrimmedExtension) || "puml".equals(lowerCasedTrimmedExtension) || "plantuml".equals(lowerCasedTrimmedExtension);
        }
        return result;
    }


}
