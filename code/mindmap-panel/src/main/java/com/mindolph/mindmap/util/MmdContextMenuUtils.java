package com.mindolph.mindmap.util;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public class MmdContextMenuUtils {

    public static void traverseMenuItems(Collection<MenuItem> menuItems, Consumer<MenuItem> consumer) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem instanceof Menu mi) {
                traverseMenuItems(mi.getItems(), consumer);
            }
            else {
                consumer.accept(menuItem);
            }
        }
    }

    public static MenuItem makeRadioButtonMenuItem(String text, Image icon, boolean checked) {
        ImageView imageView = new ImageView(icon);
        RadioMenuItem radioMenuItem = new RadioMenuItem(text, imageView);
        radioMenuItem.setSelected(checked);
        return radioMenuItem;
    }

    public static MenuItem makeCheckboxMenuItem(String text, Image icon, boolean checked) {
        ImageView imageView = new ImageView(icon);
        CheckMenuItem checkMenuItem = new CheckMenuItem(text, imageView);
        checkMenuItem.setSelected(checked);
        return checkMenuItem;
    }

}
