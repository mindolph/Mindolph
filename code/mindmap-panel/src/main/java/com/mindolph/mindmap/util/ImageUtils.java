package com.mindolph.mindmap.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracted from Utils
 *
 * @author mindolph.com@gmail.com
 * @deprecated This can be replaced by mfx ImageConverter.
 */
public class ImageUtils {
    /**
     * Load and encode image into Base64.
     *
     * @param in      stream to read image
     * @param maxSize max size of image, if less or zero then don't rescale
     * @return null if it was impossible to load image for its format, loaded
     * prepared image
     * @throws IOException if any error during conversion or loading
     */
    public static String rescaleImageAndEncodeAsBase64(InputStream in, int maxSize) throws IOException {
        Image image = ImageIO.read(in);
        String result = null;
        if (image != null) {
            result = rescaleImageAndEncodeAsBase64(image, maxSize);
        }
        return result;
    }

    /**
     * Load and encode image into Base64 from file.
     *
     * @param file    image file
     * @param maxSize max size of image, if less or zero then don't rescale
     * @return image
     * @throws IOException if any error during conversion or loading
     */
    public static String rescaleImageAndEncodeAsBase64(File file, int maxSize) throws IOException {
        Image image = ImageIO.read(file);
        if (image == null) {
            throw new IllegalArgumentException("Can't load image file : " + file);
        }
        return rescaleImageAndEncodeAsBase64(image, maxSize);
    }

    /**
     * Rescale image and encode into Base64.
     *
     * @param image   image to rescale and encode
     * @param maxSize max size of image, if less or zero then don't rescale
     * @return scaled and encoded image
     * @throws IOException if it was impossible to encode image
     */
    public static String rescaleImageAndEncodeAsBase64(Image image, int maxSize) throws IOException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        int maxImageSideSize = maxSize > 0 ? maxSize : Math.max(width, height);

        float imageScale = width > maxImageSideSize || height > maxImageSideSize ? (float) maxImageSideSize / (float) Math.max(width, height) : 1.0f;

        if (!(image instanceof RenderedImage) || Float.compare(imageScale, 1.0f) != 0) {
            int swidth;
            int sheight;

            if (Float.compare(imageScale, 1.0f) == 0) {
                swidth = width;
                sheight = height;
            }
            else {
                swidth = Math.round(imageScale * width);
                sheight = Math.round(imageScale * height);
            }

            BufferedImage buffer = new BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gfx = buffer.createGraphics();

            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            gfx.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            gfx.drawImage(image, AffineTransform.getScaleInstance(imageScale, imageScale), null);
            gfx.dispose();
            image = buffer;
        }


        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            if (!ImageIO.write((RenderedImage) image, "png", bos)) {
                throw new IOException("Can't encode image as PNG");
            }
            return CryptoUtils.base64encode(bos.toByteArray());
        }
        catch (IOException e) {
            throw new IOException("Can't encode image as PNG", e);
        }
    }


}
