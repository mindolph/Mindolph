package com.mindolph.mindmap.util;

import com.igormaznitsa.mindmap.model.*;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.ColorType;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.*;

import static com.mindolph.mindmap.constant.StandardTopicAttribute.*;

public final class MindMapUtils {

    private MindMapUtils() {
    }

    public static MindMap<TopicNode> createModelWithRoot() {
        MindMap<TopicNode> result = new MindMap<>();
        result.setRoot(new TopicNode(result, null, ""));
        return result;
    }

    public static List<Color> findAllTopicColors(MindMap<TopicNode> map, ColorType colorType) {
        Set<Color> result = new HashSet<>();
        for (TopicNode topic : map) {
            Color color;
            switch (colorType) {
                case BORDER:
                    color = topic.getColorFromAttribute(ATTR_BORDER_COLOR);
//                    color = Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
                    break;
                case FILL:
                    color = topic.getColorFromAttribute(ATTR_FILL_COLOR);
//                    color = Utils.html2color(topic.getAttribute(ATTR_FILL_COLOR.getText()), false);
                    break;
                case TEXT:
                    color = topic.getColorFromAttribute(ATTR_TEXT_COLOR);
//                    color = Utils.html2color(topic.getAttribute(ATTR_TEXT_COLOR.getText()), false);
                    break;
                default:
                    throw new Error("Unexpected color type: " + colorType);
            }
            if (color != null) {
                result.add(color);
            }
        }
        return Arrays.asList(result.toArray(new Color[0]));
    }

    public static void removeCollapseAttributeFromTopicsWithoutChildren(MindMap<TopicNode> map) {
        removeCollapseAttrIfNoChildren(map == null ? null : map.getRoot());
    }


    public static void removeCollapseAttrIfNoChildren(TopicNode topic) {
        if (topic != null) {
            if (!topic.hasChildren()) {
                topic.setAttribute(ATTR_COLLAPSED.getText(), null);
            }
            else {
                for (TopicNode t : topic.getChildren()) {
                    removeCollapseAttrIfNoChildren(t);
                }
            }
        }
    }

    /**
     * Check if the file is not dir, and the  extension matches, if not, ask user to add extension to file name.
     *
     * @param file
     * @param dottedExtension
     * @return
     */
    public static File checkFileAndExtension(File file, String dottedExtension) {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            DialogFactory.errDialog(String.format(I18n.getIns().getString("AbstractMindMapExporter.msgErrorItIsDirectory"), file.getAbsolutePath()));
            return null;
        }
        if (file.isFile()) {
//            if (!DialogFactory.okCancelConfirmDialog(I18n.getIns().getString("AbstractMindMapExporter.titleSaveAs"), String.format(I18n.getIns().getString("AbstractMindMapExporter.msgAlreadyExistsWantToReplace"), file.getAbsolutePath()))) {
//                return null;
//            }
        }
        else if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(dottedExtension.toLowerCase(Locale.ENGLISH))) {
            if (DialogFactory.yesNoConfirmDialog(I18n.getIns().getString("AbstractMindMapExporter.msgTitleAddExtension"), String.format(I18n.getIns().getString("AbstractMindMapExporter.msgAddExtensionQuestion"), dottedExtension))) {
                return new File(file.getParent(), file.getName() + dottedExtension);
            }
        }
        return file;
    }

    /**
     * Remove duplicated and descendant.
     *
     * @param topics array to be processed
     * @return
     */
    public static List<TopicNode> removeDuplicatedAndDescendants(List<TopicNode> topics) {
        List<TopicNode> result = new ArrayList<>();
        for (TopicNode t : topics) {
            if (result.contains(t) || topics.stream().anyMatch(t::isAncestor)) {
                continue; // exclude if has ancestor
            }
            result.add(t);
        }
        return result;
    }

    public static String makeTooltipForExtra(MindMap<TopicNode> model, Extra<?> extra) {
        StringBuilder builder = new StringBuilder();
        switch (extra.getType()) {
            case FILE: {
                ExtraFile efile = (ExtraFile) extra;
                String line = efile.getAsURI().getParameters().getProperty("line", null);
                if (line != null && !line.equals("0")) {
                    builder.append("Open File\n%s\nline:%s".formatted(efile.getAsString(), line));
                }
                else {
                    builder.append("Open File\n").append(efile.getAsString());
                }
            }
            break;
            case TOPIC: {
                TopicNode topic = model.findTopicForLink((ExtraTopic) extra);
                builder.append("Jump to topic\n").append(ModelUtils.makeShortTextVersion(topic == null ? "----" : topic.getText(), 32));
            }
            break;
            case LINK: {
                builder.append("Open link\n").append(ModelUtils.makeShortTextVersion(extra.getAsString(), 48));
            }
            break;
            case NOTE: {
                ExtraNote extraNote = (ExtraNote) extra;
                if (extraNote.isEncrypted()) {
                    builder.append("Open text content\n").append("#######");
                }
                else {
                    builder.append("Open text content\n").append(ModelUtils.makeShortTextVersion(extra.getAsString(), 64));
                }
            }
            break;
            default: {
                builder.append("Unknown");
            }
            break;
        }
        return builder.toString();
    }
}
