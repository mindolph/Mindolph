package com.mindolph.fx;

import com.mindolph.core.model.NodeData;
import com.mindolph.fx.constant.IconName;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public class IconManager {


    private final Map<IconName, Image> iconMap = new HashMap<>();
    private static IconManager ins;

    public static synchronized IconManager getInstance() {
        if (ins == null) {
            ins = new IconManager();
        }
        return ins;
    }

    private IconManager() {
        try {
            iconMap.put(IconName.WORKSPACE, new Image(getClass().getResource("/icons/project.png").openStream()));
            iconMap.put(IconName.FOLDER, new Image(getClass().getResource("/icons/folder.png").openStream()));
            iconMap.put(IconName.FILE_MMD, new Image(getClass().getResource("/icons/file_mmd.png").openStream()));
            iconMap.put(IconName.FILE_PUML, new Image(getClass().getResource("/icons/file_puml.png").openStream()));
            iconMap.put(IconName.FILE_TXT, new Image(getClass().getResource("/icons/blank_file.png").openStream()));
            iconMap.put(IconName.FILE_MARKDOWN, new Image(getClass().getResource("/icons/file_md.png").openStream()));
            iconMap.put(IconName.FILE_CSV, new Image(getClass().getResource("/icons/file_csv.png").openStream()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load icon resources");
        }
    }

    public Image getIcon(IconName iconName) {
        return iconMap.get(iconName);
    }

    public Image getFileIcon(NodeData fileData) {
        if (fileData.isMindMap()) {
            return getIcon(IconName.FILE_MMD);
        }
        else if (fileData.isPlantUml()) {
            return getIcon(IconName.FILE_PUML);
        }
        else if (fileData.isPlainText()) {
            return getIcon(IconName.FILE_TXT);
        }
        else if (fileData.isMarkdown()) {
            return getIcon(IconName.FILE_MARKDOWN);
        }
        else if (fileData.isCsv()) {
            return getIcon(IconName.FILE_CSV);
        }
        else {
            return getIcon(IconName.FILE_TXT); // as default icon
        }
    }

}
