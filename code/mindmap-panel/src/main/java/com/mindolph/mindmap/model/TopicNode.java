package com.mindolph.mindmap.model;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.core.model.ItemData;
import com.mindolph.mfx.util.TextUtils;
import com.mindolph.mindmap.constant.StandardTopicAttribute;
import javafx.scene.paint.Color;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mindolph.mindmap.constant.StandardTopicAttribute.*;

/**
 * @author mindolph.com@gmail.com
 */
public class TopicNode extends Topic<TopicNode> implements ItemData {
    static final Logger log = LoggerFactory.getLogger(TopicNode.class);

    public TopicNode(MindMap<TopicNode> mindMap, TopicNode base, boolean copyChildren) {
        super(mindMap, base, copyChildren);
    }

    /**
     * @param map
     * @param parent
     * @param text
     * @param extras can't be null
     */
    public TopicNode(MindMap<TopicNode> map, TopicNode parent, String text, Extra<?>... extras) {
        super(map, parent, text, extras);
    }

    @Override
    public TopicNode createChild(String text, Extra<?>... extras) {
        return new TopicNode(getMap(), this, text, extras);
    }

    @Override
    public TopicNode cloneTopic(MindMap<TopicNode> mapModel, boolean copyChildren) {
        return new TopicNode(mapModel, this, copyChildren);
    }

    @Override
    public TopicNode cloneTopic(boolean copyChildren) {
        return new TopicNode(getMap(), this, copyChildren);
    }

    public boolean isHidden() {
        String collapsed = this.findAttributeInAncestors(ATTR_COLLAPSED.getText());
        return Boolean.parseBoolean(collapsed);
    }

    public boolean setCollapsed(boolean fold) {
        return this.setAttribute(ATTR_COLLAPSED.getText(), fold ? "true" : null);
    }

    public boolean isCollapsed() {
        return "true".equalsIgnoreCase(this.getAttribute(ATTR_COLLAPSED.getText()));
    }

    public boolean isSameDirection(TopicNode t2) {
        return StringUtils.equals(this.getAttribute(ATTR_LEFTSIDE.getText()), t2.getAttribute(ATTR_LEFTSIDE.getText()));
    }

    public boolean isLeftSidedTopic() {
        return "true".equals(this.getAttribute(ATTR_LEFTSIDE.getText()));
    }

    public void makeTopicLeftSided(boolean left) {
        if (left) {
            this.setAttribute(ATTR_LEFTSIDE.getText(), "true");
        }
        else {
            this.setAttribute(ATTR_LEFTSIDE.getText(), null);
        }
    }

    public boolean isTopicVisible() {
        boolean result = true;
        TopicNode current = this.getParent();
        while (current != null) {
            if (current.isCollapsed()) {
                result = false;
                break;
            }
            current = current.getParent();
        }
        return result;
    }

    public boolean foldOrUnfoldChildren(boolean fold, int levelCount) {
        boolean result = false;
        if (levelCount > 0 && this.hasChildren()) {
            for (TopicNode c : this) {
                result |= c.foldOrUnfoldChildren(fold, levelCount - 1);
            }
            result |= this.setCollapsed(fold);
        }
        return result;
    }

    public TopicNode findSameSidePrevSibling() {
        TopicNode sibling = this;
        while (sibling != null) {
            sibling = sibling.prevSibling();
            if (sibling == null || sibling.isSameDirection(this)) {
                return sibling;
            }
        }
        return null;
    }

    public TopicNode findSameSideNextSibling() {
        TopicNode sibling = this;
        while (sibling != null) {
            sibling = sibling.nextSibling();
            if (sibling == null || sibling.isSameDirection(this)) {
                return sibling;
            }
        }
        return null;
    }

    public TopicNode findFirstVisibleAncestor() {
        List<TopicNode> path = this.getPath();
        TopicNode lastVisible = null;
        if (path.size() > 0) {
            for (TopicNode t : path) {
                lastVisible = t;
                boolean collapsed = Boolean.parseBoolean(t.getAttribute(ATTR_COLLAPSED.getText()));
                if (collapsed) {
                    break;
                }
            }
        }
        return lastVisible;
    }

    public TopicNode[] getLeftToRightOrderedChildren() {
        List<TopicNode> result = new ArrayList<>();
        if (this.getTopicLevel() == 0) {
            result.addAll(this.getChildren().stream().filter(TopicNode::isLeftSidedTopic).toList());
            result.addAll(this.getChildren().stream().filter((t) -> !t.isLeftSidedTopic()).toList());
        }
        else {
            result.addAll(this.getChildren());
        }
        return result.toArray(new TopicNode[0]);
    }

    public Color getColorFromAttribute(StandardTopicAttribute attribute) {
        String textColor = this.getAttribute(attribute.getText());
        return StringUtils.isBlank(textColor) ? null : Color.web(textColor, 1);
    }

    public void copyColorAttributes(TopicNode source) {
        this.setAttribute(ATTR_FILL_COLOR.getText(), source.getAttribute(ATTR_FILL_COLOR.getText()));
        this.setAttribute(ATTR_BORDER_COLOR.getText(), source.getAttribute(ATTR_BORDER_COLOR.getText()));
        this.setAttribute(ATTR_TEXT_COLOR.getText(), source.getAttribute(ATTR_TEXT_COLOR.getText()));
    }

    public void removeCollapseAttr() {
        this.setAttribute(ATTR_COLLAPSED.getText(), null);
        if (this.hasChildren()) {
            for (TopicNode ch : this.getChildren()) {
                ch.removeCollapseAttr();
            }
        }
    }

    public boolean upgradeOrDowngrade(boolean isUpgrade) {
        BaseElement ele = (BaseElement) this.getPayload();
        if (ele != null) {
            if (isUpgrade) {
                TopicNode parentTopic = this.getParent();
                if (parentTopic != null && parentTopic.getParent() != null) {
                    this.moveToNewParent(parentTopic.getParent());
                    this.moveAfter(parentTopic);
                    this.makeTopicLeftSided(parentTopic.isLeftSidedTopic());
                    return true;
                }
            }
            else {
                TopicNode baseSibling = this.findSameSidePrevSibling();
                if (baseSibling == null) {
                    baseSibling = this.findSameSideNextSibling();
                }
                if (baseSibling != null) {
                    if (baseSibling.isCollapsed()) {
                        BaseElement eleSib = (BaseElement) baseSibling.getPayload();
                        if (eleSib instanceof BaseCollapsableElement) {
                            ((BaseCollapsableElement) eleSib).setCollapse(false);
                        }
                    }
                    this.moveToNewParent(baseSibling);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate sub tree for whitespace offsets in text lines.
     *
     * @param text
     * @return all created topics.
     */
    public List<TopicNode> fromText(String text) {
        List<TopicNode> result = null;
        try (ByteArrayInputStream bains = new ByteArrayInputStream(text.getBytes())) {
            List<String> lines = IOUtils.readLines(bains, "UTF-8");
            if (CollectionUtils.isNotEmpty(lines)) {
                result = new ArrayList<>();
                TopicNode parentNode = this;
                int lastIndentCount = -1;
                for (String line : lines) {
                    if (StringUtils.isBlank(line)) {
                        continue;
                    }
                    int indentCount = TextUtils.countIndent(line, 1);
                    log.trace("[%d] '%s'".formatted(indentCount, line));
                    if (indentCount <= lastIndentCount) {
                        parentNode = parentNode.getParent(); // might be sibling or parents sibling.
                        if (indentCount < lastIndentCount) {
                            parentNode = parentNode.getParent(); // is parents sibling.
                        }
                    }
                    TopicNode newTopicNode = parentNode.makeChild(line.trim(), null);
                    parentNode = newTopicNode;
                    result.add(newTopicNode);
                    lastIndentCount = indentCount;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Generate sub tree for whitespace offsets in text lines.
     *
     * @param text text source to make topics
     * @return created topics
     * @deprecated
     */
    public List<TopicNode> makeSubTreeFromText(String text) {
        String[] lines = StringUtils.split(text, TextConstants.LINE_SEPARATOR);

        if (lines.length == 0) {
            return null;
        }

        int ignoredLeadingSpaces = Integer.MAX_VALUE;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            line = line.replace("\t", "    ");
            int leadingSpacesNumber = TextUtils.countInStarting(line, ' ');
            ignoredLeadingSpaces = Math.min(leadingSpacesNumber, ignoredLeadingSpaces);
        }

        int maxLineLength = 0;
        for (int i = 0; i < lines.length; i++) {
            String old = lines[i];
            if (old.trim().isEmpty()) {
                lines[i] = null;
            }
            else {
                lines[i] = old.substring(ignoredLeadingSpaces);
                maxLineLength = Math.max(lines[i].length(), maxLineLength);
            }
        }

        TopicNode[] topics = new TopicNode[lines.length];
        Set<Integer> justAddedSibling = new HashSet<>();
        for (int checkCharPosition = 0; checkCharPosition < maxLineLength; checkCharPosition++) {
            justAddedSibling.clear();
            for (int index = 0; index < lines.length; index++) {
                String textStr = lines[index];
                if (textStr == null) {
                    continue;
                }
                if (!Character.isWhitespace(textStr.charAt(checkCharPosition))) {
                    lines[index] = null;
                    TopicNode mostCloseParentTopic = null;
                    for (int off = 1; index - off >= 0; off++) {
                        if (!justAddedSibling.contains(index - off)) {
                            if (topics[index - off] != null) {
                                mostCloseParentTopic = topics[index - off];
                                break;
                            }
                        }
                    }
                    if (mostCloseParentTopic == null) {
                        for (int off = 1; index + off < topics.length; off++) {
                            if (!justAddedSibling.contains(index + off)) {
                                if (topics[index + off] != null) {
                                    mostCloseParentTopic = topics[index + off];
                                    break;
                                }
                            }
                        }
                    }

                    TopicNode newTopic = mostCloseParentTopic == null ? this.makeChild(textStr.trim(), null) : mostCloseParentTopic.makeChild(textStr.trim(), null);
                    topics[index] = newTopic;
                    justAddedSibling.add(index);
                }
            }
        }
        return new ArrayList<>(List.of(topics));
    }

    @Override
    public Integer getDisplayIndex() {
        return 0;
    }

    @Override
    public void setDisplayIndex(Integer displayIndex) {

    }
}
