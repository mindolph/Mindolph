package com.mindolph.mindmap.util;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.api.PopUpMenuItemExtension;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static com.mindolph.mindmap.extension.ContextMenuSection.*;

/**
 * @author mindolph.com@gmail.com
 */
public class MenuUtils {

    public static void traverseMenuItems(Collection<MenuItem> menuItems, Consumer<MenuItem> consumer) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem instanceof Menu mi){
                traverseMenuItems(mi.getItems(), consumer);
            }
            else {
                consumer.accept(menuItem);
            }
        }
    }

    public static ContextMenu makePopUp(ExtensionContext context, MindMap<TopicNode> model,
                                        boolean isFullScreen, TopicNode topicUnderMouse) {

        ContextMenu ctxMenu = new ContextMenu();
        List<PopUpMenuItemExtension> extensionMenuItems = MindMapExtensionRegistry.getInstance().findFor(PopUpMenuItemExtension.class);
        List<MenuItem> tmpList = new ArrayList<>();

        boolean isModelNotEmpty = model.getRoot() != null;

        putAllItemsAsSection(ctxMenu, null, findPopupMenuItems(context, MAIN, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));
        putAllItemsAsSection(ctxMenu, null, findPopupMenuItems(context, MANIPULATE, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));
        putAllItemsAsSection(ctxMenu, null, findPopupMenuItems(context, EXTRAS, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));

        Menu importMenu = new Menu(I18n.getIns().getString("MMDImporters.SubmenuName"), FontIconManager.getIns().getIcon(IconKey.IMPORT));
        Menu exportMenu = new Menu(I18n.getIns().getString("MMDExporters.SubmenuName"), FontIconManager.getIns().getIcon(IconKey.EXPORT));

        putAllItemsAsSection(ctxMenu, importMenu, findPopupMenuItems(context, IMPORT, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));
        if (isModelNotEmpty) {
            putAllItemsAsSection(ctxMenu, exportMenu, findPopupMenuItems(context, EXPORT, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));
        }

        putAllItemsAsSection(ctxMenu, null, findPopupMenuItems(context, TOOLS, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));
        putAllItemsAsSection(ctxMenu, null, findPopupMenuItems(context, MISC, isFullScreen, tmpList, topicUnderMouse, extensionMenuItems));

        return ctxMenu;
    }


    private static List<MenuItem> putAllItemsAsSection(ContextMenu menu, Menu subMenu, List<MenuItem> items) {
        if (!items.isEmpty()) {
            if (menu.getItems().size() > 0) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            for (MenuItem i : items) {
                if (subMenu == null) {
                    menu.getItems().add(i);
                }
                else {
                    subMenu.getItems().add(i);
                }
            }

            if (subMenu != null) {
                menu.getItems().add(subMenu);
            }
        }
        return items;
    }

    private static List<MenuItem> findPopupMenuItems(
            ExtensionContext context,
            ContextMenuSection section,
            boolean fullScreenModeActive,
            List<MenuItem> menuItems,
            TopicNode topicUnderMouse,
            List<PopUpMenuItemExtension> extensionMenuItems) {
        menuItems.clear();

        for (PopUpMenuItemExtension p : extensionMenuItems) {
            if (fullScreenModeActive && !p.isCompatibleWithFullScreenMode()) {
                continue;
            }
            if (p.getSection() == section) {
                boolean noTopicsNeeded = !(p.needsTopicUnderMouse() || p.needsSelectedTopics());
                if (noTopicsNeeded
                        || (p.needsTopicUnderMouse() && topicUnderMouse != null)
                        || (p.needsSelectedTopics() && context.getSelectedTopics().size() > 0)) {

                    MenuItem item = p.makeMenuItem(context, topicUnderMouse);
                    if (item != null) {
                        item.setDisable(!p.isEnabled(context, topicUnderMouse));
                        menuItems.add(item);
                    }
                }
            }
        }
        return menuItems;
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
