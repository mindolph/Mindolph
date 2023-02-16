package com.mindolph.base.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public class FxImageUtils {

    /**
     * Dump image to temporary for test.
     *
     * @param image
     */
    public static File dumpImage(Image image) {
        File snapshotFile = MindolphFileUtils.getTempFile("snapshot.png");
        try {
            if (snapshotFile != null) {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", snapshotFile);
            }
            return snapshotFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A legacy image utils for reference.
     *
     * @param image
     * @param opacity
     */
    public void makeImageTransparent(WritableImage image, float opacity) {
        int alpha;
        if (opacity <= 0.0f) {
            alpha = 0x00;
        }
        else if (opacity >= 1.0f) {
            alpha = 0xFF;
        }
        else {
            alpha = Math.round(0xFF * opacity);
        }

        alpha <<= 24;

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int curAlpha = pixelReader.getArgb(x, y) >>> 24;
                if (curAlpha == 0xFF) {
                    pixelWriter.setArgb(x, y, (pixelReader.getArgb(x, y) & 0x00FFFFFF) | alpha);
                }
                else if (curAlpha != 0x00) {
                    int calculated = Math.round(curAlpha * opacity) << 24;
                    pixelWriter.setArgb(x, y, (pixelReader.getArgb(x, y) & 0x00FFFFFF) | calculated);
                }
            }
        }
    }
}
