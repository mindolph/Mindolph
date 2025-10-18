/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not usne this file except in compliance with the License.
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

package com.mindolph.mindmap.extension.exporters;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.AwtImageUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.gfx.MindMapCanvas;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;

public final class PNGImageExporter extends BaseExportExtension {

    private static final Logger log = LoggerFactory.getLogger(PNGImageExporter.class);
    private boolean flagExpandAllNodes = false;
    private boolean flagDrawBackground = true;


    public PNGImageExporter() {
        super();
    }


    private Image makeImage(ExtensionContext context, List<Boolean> options) throws IOException {
        MindMapConfig newConfig = new MindMapConfig(context.getMindMapConfig());
        newConfig.getTheme().setDrawBackground(this.flagDrawBackground);
        return MindMapCanvas.renderMindMapAsImage(context.getModel(), newConfig, flagExpandAllNodes);
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        Image image = makeImage(context, options);
        if (image != null) {
            ClipboardContent content = new ClipboardContent();
            content.putImage(image);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList(I18n.getIns().getString("PNGImageExporter.optionUnfoldAll"), I18n.getIns().getString("PNGImageExporter.optionDrawBackground"));
    }

    @Override
    public List<Boolean> getDefaults() {
        return Arrays.asList(true, true);
    }

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        if (options != null) {
            this.flagExpandAllNodes = options.get(0);
            this.flagDrawBackground = options.get(1);
        }
        Image image = makeImage(context, options);

        if (image == null) {
            if (out == null) {
                log.error("Can't render map as image");
                DialogFactory.errDialog(I18n.getIns().getString("PNGImageExporter.msgErrorDuringRendering"));
                return;
            }
            else {
                throw new IOException("Can't render image");
            }
        }

//        ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
//        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
//        ImageIO.write(bufferedImage, "png", buff);
//        byte[] imageData = buff.toByteArray();

        File fileToSave = null;
        if (out == null) {
            fileToSave = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("PNGImageExporter.saveDialogTitle"),
                    null,
                    ".png",
                    I18n.getIns().getString("PNGImageExporter.filterDescription"),
                    exportFileName);
            fileToSave = MindMapUtils.checkFileAndExtension(fileToSave, ".png");
            log.debug("Save to %s".formatted(fileToSave));
//            out = fileToSave == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSave, false));
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            AwtImageUtils.addCommentToImageAndSave(bufferedImage, "png", fileToSave, "This file is created by Mindolph (https://github.com/mindolph/Mindolph)");
            Files.setLastModifiedTime(fileToSave.toPath(), FileTime.fromMillis(System.currentTimeMillis()));
        }
//        if (out != null) {
//            try {
//                IOUtils.write(imageData, out);
//                Files.setLastModifiedTime(fileToSave.toPath(), FileTime.fromMillis(System.currentTimeMillis()));
//            } finally {
//                if (fileToSave != null) {
//                    IOUtils.closeQuietly(out);
//                }
//            }
//        }
    }

    @Override
    public String getName(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("PNGImageExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("PNGImageExporter.exporterReference");
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return FontIconManager.getIns().getIcon(IconKey.IMAGE);
    }

    @Override
    public int getOrder() {
        return 4;
    }

}
