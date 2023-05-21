package com.mindolph.fx;

import com.mindolph.core.model.NodeData;
import com.mindolph.fx.constant.IconName;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Builder for icon's ImageView.
 *
 * @author mindolph.com@gmail.com
 */
public class IconBuilder {
    public static final int DEFAULT_ICON_SIZE = 16;
    private double size = DEFAULT_ICON_SIZE;
    private Image icon;

    public IconBuilder name(IconName name) {
        this.icon = IconManager.getInstance().getIcon(name);
        return this;
    }

    public IconBuilder size(double size) {
        this.size = size;
        return this;
    }

    /**
     *
     * @param fileData
     * @return
     * @deprecated
     */
    public IconBuilder fileData(NodeData fileData) {
        this.icon = IconManager.getInstance().getFileIcon(fileData);
        return this;
    }

    public IconBuilder icon(Image icon) {
        this.icon = icon;
        return this;
    }

    public ImageView build() {
        ImageView iv = new ImageView(icon);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        return iv;
    }
}
