/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class Topic<T extends Topic<T>> implements Serializable, Constants, Iterable<T> {

    private static final AtomicLong LOCALUID_GENERATOR = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(Topic.class);
    private final EnumMap<Extra.ExtraType, Extra<?>> extras = new EnumMap<>(Extra.ExtraType.class);
    private final Map<Extra.ExtraType, Extra<?>> unmodifableExtras = Collections.unmodifiableMap(this.extras);
    private final Map<String, String> attributes = new TreeMap<>(ModelUtils.STRING_COMPARATOR);
    private final Map<String, String> unmodifableAttributes = Collections.unmodifiableMap(this.attributes);
    private final Map<String, String> codeSnippets = new TreeMap<>(ModelUtils.STRING_COMPARATOR);
    private final Map<String, String> unmodifableCodeSnippets = Collections.unmodifiableMap(this.codeSnippets);

    private final List<T> children = new ArrayList<>();

    private final List<T> unmodifableChildren = Collections.unmodifiableList(this.children);
    private final transient long localUID = LOCALUID_GENERATOR.getAndIncrement();

    private final MindMap<T> map;

    private T parent;

    private volatile String text;

    private transient Object payload;

    private Topic(MindMap<T> map, String text, Extra<?>... extras) {
        this.map = Assertions.assertNotNull(map);
        this.text = Assertions.assertNotNull(text);

        for (Extra<?> e : extras) {
            if (e != null) {
                this.extras.put(e.getType(), e);
            }
        }
    }

    /**
     * Constructor to build topic on base of another topic for another mind map.
     *
     * @param mindMap      mind map to be owner for new topic
     * @param base         base source topic
     * @param copyChildren flag to make copy of children, true if to make copy,
     *                     false otherwise
     * @since 1.2.2
     */
    public Topic(MindMap<T> mindMap, T base, boolean copyChildren) {
        this(mindMap, base.getText());
        this.attributes.putAll(base.getAttributes());
        this.extras.putAll(base.getExtras());
        this.codeSnippets.putAll(base.getCodeSnippets());

        if (copyChildren) {
            for (T t : base.getChildren()) {
                T clonedChildren = t.cloneTopic(mindMap, true);
                clonedChildren.moveToNewParent((T) this);
            }
        }
    }

    public Topic(MindMap<T> map, T parent, String text, Extra<?>... extras) {
        this(map, text, extras);
        this.parent = parent;

        if (parent != null) {
            if (parent.getMap() != map) {
                throw new IllegalArgumentException("Parent must belong to the same mind map");
            }
            parent.addChild((T) this);
        }
    }

    /**
     * Clone a topic to new mindmap model with or without children.
     *
     * @param mapModel
     * @param copyChildren
     * @return
     */
    public T cloneTopic(MindMap<T> mapModel, boolean copyChildren) {
        return (T) new Topic(mapModel, this, copyChildren);
    }

    /**
     * Clone a topic to same mindmap model with or without children.
     *
     * @param copyChildren
     * @return
     */
    public T cloneTopic(boolean copyChildren) {
        return (T) new Topic(this.getMap(), this, copyChildren);
    }

    public T createChild(String text, Extra<?>... extras) {
        return (T) new Topic(this.getMap(), this, text, extras);
    }

    public static <T extends Topic<T>> T parse(MindMap<T> map, MindMapLexer lexer, RootCreate<T> creator) {
        map.lock();
        try {
            T topic = null;
            int depth = 0;

            Extra.ExtraType extraType = null;

            String codeSnippetlanguage = null;
            StringBuilder codeSnippetBody = null;

            int detectedLevel = -1;

            while (true) {
                int oldLexerPosition = lexer.getCurrentPosition().getOffset();
                lexer.advance();
                boolean lexerPositionWasNotChanged = oldLexerPosition == lexer.getCurrentPosition().getOffset();

                MindMapLexer.TokenType token = lexer.getTokenType();
                if (token == null || lexerPositionWasNotChanged) {
                    break;
                }

                switch (token) {
                    case TOPIC_LEVEL: {
                        String tokenText = lexer.getTokenText();
                        detectedLevel = ModelUtils.calcCharsOnStart('#', tokenText);
                    }
                    break;
                    case TOPIC_TITLE: {
                        String tokenText = ModelUtils.removeISOControls(lexer.getTokenText());
                        String newTopicText = ModelUtils.unescapeMarkdownStr(tokenText);

                        if (detectedLevel == depth + 1) {
                            depth = detectedLevel;
                            topic = topic == null ? creator.createRoot(map, newTopicText) : topic.createChild(newTopicText);
                        }
                        else if (detectedLevel == depth) {
//                            topic = new Topic(map, topic == null ? null : topic.getParent(), newTopicText);
                            topic = topic == null ? creator.createRoot(map) : topic.getParent().createChild(newTopicText);
                        }
                        else if (detectedLevel < depth) {
                            if (topic != null) {
                                topic = topic.findParentForDepth(depth - detectedLevel);
//                                topic = new Topic(map, topic, newTopicText);
                                topic = topic.createChild(newTopicText);
                                depth = detectedLevel;
                            }
                        }

                    }
                    break;
                    case EXTRA_TYPE: {
                        String extraName = lexer.getTokenText().substring(1).trim();
                        try {
                            extraType = Extra.ExtraType.valueOf(extraName);
                        } catch (IllegalArgumentException ex) {
                            extraType = null;
                        }
                    }
                    break;
                    case CODE_SNIPPET_START: {
                        if (topic != null) {
                            codeSnippetlanguage = lexer.getTokenText().substring(3);
                            codeSnippetBody = new StringBuilder();
                        }
                    }
                    break;
                    case CODE_SNIPPET_BODY: {
                        codeSnippetBody.append(lexer.getTokenText());
                    }
                    break;
                    case CODE_SNIPPET_END: {
                        if (topic != null && codeSnippetlanguage != null && codeSnippetBody != null) {
                            topic.putCodeSnippet(codeSnippetlanguage.trim(), codeSnippetBody.toString());
                        }
                        codeSnippetlanguage = null;
                        codeSnippetBody = null;
                    }
                    break;
                    case ATTRIBUTE: {
                        if (topic != null) {
                            String text = lexer.getTokenText().trim();
                            Map<String, String> attrs = new TreeMap<>();
                            MindMap.fillMapByAttributes(text, attrs);
                            topic.putAttributes(attrs);
                        }
                        extraType = null;
                    }
                    break;
                    case EXTRA_TEXT: {
                        if (topic != null && extraType != null) {
                            try {
                                String text = lexer.getTokenText();
                                String groupPre = extraType.preprocessString(text.substring(5, text.length() - 6));
                                if (groupPre != null) {
                                    topic.setExtra(extraType.parseLoaded(groupPre, topic.getAttributes()));
                                }
                                else {
                                    logger.error("Detected invalid extra data " + extraType);
                                }
                            } catch (Exception ex) {
                                logger.error("Unexpected exception #23241", ex);
                            } finally {
                                extraType = null;
                            }
                        }
                    }
                    break;
                    case UNKNOWN_LINE: {
                        if (topic != null && extraType != null) {
                            extraType = null;
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
            return topic == null ? null : topic.getRoot();
        } finally {
            map.unlock();
        }
    }


    public T findRoot() {
        T result = (T) this;
        while (!result.isRoot()) {
            result = result.getParent();
        }
        return result;
    }

    /**
     * Search in the tree.
     *
     * @param topic
     * @return
     */
    public boolean containTopic(T topic) {
        boolean result = false;

        if (this == topic) {
            result = true;
        }
        else {
            for (T t : this.getChildren()) {
                if (t.containTopic(topic)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public T nextSibling() {
        int position = this.parent == null ? -1 : this.parent.getChildren().indexOf(this);

        T result;
        if (position < 0) {
            result = null;
        }
        else {
            List<T> all = this.parent.getChildren();
            int nextPosition = position + 1;
            result = all.size() > nextPosition ? all.get(nextPosition) : null;
        }
        return result;
    }


    public T prevSibling() {
        int position = this.parent == null ? -1 : this.parent.getChildren().indexOf(this);

        T result;
        if (position <= 0) {
            result = null;
        }
        else {
            List<T> all = this.parent.getChildren();
            result = all.get(position - 1);
        }
        return result;
    }

    public boolean containsPattern(File baseFolder, Pattern pattern, boolean findInTopicText,
                                   Set<Extra.ExtraType> extrasForSearch) {
        boolean result = false;

        if (findInTopicText && pattern.matcher(this.text).find()) {
            result = true;
        }
        else if (extrasForSearch != null && !extrasForSearch.isEmpty()) {
            for (Extra<?> e : this.extras.values()) {
                if (extrasForSearch.contains(e.getType()) && e.containsPattern(baseFolder, pattern)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isRoot() {
        return this.parent == null;
    }


    public Object getPayload() {
        return this.payload;
    }

    public void setPayload(Object value) {
        this.payload = value;
    }


    public MindMap<T> getMap() {
        return this.map;
    }

    public int getTopicLevel() {
        T topic = this.parent;
        int result = 0;
        while (topic != null) {
            topic = topic.getParent();
            result++;
        }
        return result;
    }


    public T findParentForDepth(int depth) {
        this.map.lock();
        try {
            T result = this.parent;
            while (depth > 0 && result != null) {
                result = result.getParent();
                depth--;
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }


    public T getRoot() {
        this.map.lock();
        try {
            T result = (T) this;
            while (true) {
                T prev = result.getParent();
                if (prev == null) {
                    break;
                }
                result = prev;
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }


    public T getFirst() {
        return this.getChildren().isEmpty() ? null : this.getChildren().get(0);
    }


    public T getLast() {
        return this.getChildren().isEmpty() ? null : this.getChildren().get(this.getChildren().size() - 1);
    }


    public List<T> getChildren() {
        return this.unmodifableChildren;
    }

    public void addChild(T t) {
        this.children.add(t);
    }

    public void addChild(int idx, T t) {
        this.children.add(idx, t);
    }

    public int getNumberOfExtras() {
        return this.extras.size();
    }


    public Map<Extra.ExtraType, Extra<?>> getExtras() {
        return this.unmodifableExtras;
    }

    public void putExtra(Extra.ExtraType type, Extra<?> extra) {
        this.extras.put(type, extra);
    }


    public Extra<?>[] extrasToArray() {
        Collection<Extra<?>> collection = this.unmodifableExtras.values();
        return collection.toArray(new Extra<?>[0]);
    }


    public Map<String, String> getAttributes() {
        return this.unmodifableAttributes;
    }


    public Map<String, String> getCodeSnippets() {
        return this.unmodifableCodeSnippets;
    }

    public void putCodeSnippet(String k, String snippet) {
        this.codeSnippets.put(k, snippet);
    }

    public void putCodeSnippets(Map<String, String> codeSnippets) {
        this.codeSnippets.putAll(codeSnippets);
    }

    public boolean setAttribute(String name, String value) {
        this.map.lock();
        try {
            if (value == null) {
                return this.attributes.remove(name) != null;
            }
            else {
                return !value.equals(this.attributes.put(name, value));
            }
        } finally {
            this.map.unlock();
        }
    }

    public void putAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
    }

    public boolean setCodeSnippet(String language, String text) {
        this.map.lock();
        try {
            if (text == null) {
                return this.codeSnippets.remove(language) != null;
            }
            else {
                return !text.equals(this.codeSnippets.put(language, text));
            }
        } finally {
            this.map.unlock();
        }
    }


    public String getCodeSnippet(String language) {
        return this.codeSnippets.get(language);
    }


    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void delete() {
        this.map.lock();
        try {
            T theParent = this.parent;
            if (theParent != null) {
                theParent.removeTopic((T) this);
            }
        } finally {
            this.map.unlock();
        }
    }


    public T getParent() {
        return this.parent;
    }

    protected void setParent(T t) {
        this.parent = t;
    }


    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.map.lock();
        try {
            this.text = Assertions.assertNotNull(text);
        } finally {
            this.map.unlock();
        }
    }

    public boolean isFirstChild(T t) {
        return !this.getChildren().isEmpty() && this.getChildren().get(0) == t;
    }

    public boolean isLastChild(T t) {
        return !this.getChildren().isEmpty() && this.getChildren().get(this.getChildren().size() - 1) == t;
    }

    public boolean isLeaf() {
        return !hasChildren();
    }

    public boolean removeExtra(Extra.ExtraType... types) {
        this.map.lock();
        try {
            boolean result = false;
            for (Extra.ExtraType e : Assertions.assertDoesntContainNull(types)) {
                Extra<?> removed = this.extras.remove(e);
                if (removed != null) {
                    removed.detachedToTopic(this);
                }
                result |= removed != null;
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }

    public void setExtra(Extra<?>... extras) {
        this.map.lock();
        try {
            for (Extra<?> e : Assertions.assertDoesntContainNull(extras)) {
                this.extras.put(e.getType(), e);
                e.attachedToTopic(this);
            }
        } finally {
            this.map.unlock();
        }
    }

    public boolean makeFirst() {
        this.map.lock();
        try {
            T theParent = this.parent;
            if (theParent != null) {
                int thatIndex = theParent.getChildren().indexOf(this);
                if (thatIndex > 0) {
                    theParent.removeTopic((T) this);
//                    theParent.getChildren().remove(thatIndex);
                    theParent.addChild(0, (T) this);
                    return true;
                }
            }
            return false;
        } finally {
            this.map.unlock();
        }
    }

    public boolean isAncestor(T topic) {
        T parent = this.parent;
        while (parent != null) {
            if (parent == topic) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public boolean makeLast() {
        this.map.lock();
        try {
            T theParent = this.parent;
            if (theParent != null) {
                int thatIndex = theParent.getChildren().indexOf(this);
                if (thatIndex >= 0 && thatIndex != theParent.getChildren().size() - 1) {
                    theParent.removeTopic((T) this);
//                    theParent.getChildren().remove(thatIndex);
                    theParent.addChild((T) this);
                    return true;
                }
            }
            return false;
        } finally {
            this.map.unlock();
        }
    }

    public void moveBefore(T topic) {
        this.map.lock();
        try {
            T theParent = this.parent;
            if (theParent != null) {
                int thatIndex = theParent.getChildren().indexOf(topic);
                int thisIndex = theParent.getChildren().indexOf(this);

                if (thatIndex > thisIndex) {
                    thatIndex--;
                }

                if (thatIndex >= 0 && thisIndex >= 0) {
                    theParent.removeTopic((T) this);
                    theParent.addChild(thatIndex, (T) this);
                }
            }
        } finally {
            this.map.unlock();
        }
    }


    public String findAttributeInAncestors(String attrName) {
        this.map.lock();
        try {
            String result = null;
            T current = this.parent;
            while (result == null && current != null) {
                result = current.getAttribute(attrName);
                current = current.getParent();
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }

    public void moveAfter(T topic) {
        this.map.lock();
        try {
            T theParent = this.parent;
            if (theParent != null) {
                int thatIndex = theParent.getChildren().indexOf(topic);
                int thisIndex = theParent.getChildren().indexOf(this);

                if (thatIndex > thisIndex) {
                    thatIndex--;
                }

                if (thatIndex >= 0 && thisIndex >= 0) {
                    theParent.removeTopic((T) this);
                    theParent.addChild(thatIndex + 1, (T) this);
                }
            }
        } finally {
            this.map.unlock();
        }
    }

    public void write(Writer out) throws IOException {
        this.map.lock();
        try {
            write(1, out);
        } finally {
            this.map.unlock();
        }
    }

    void write(int level, Writer out) throws IOException {
        out.append(NEXT_LINE);
        ModelUtils.writeChar(out, '#', level);
        out.append(' ').append(ModelUtils.escapeMarkdownStr(this.text)).append(NEXT_LINE);

        if (!this.attributes.isEmpty() || !this.extras.isEmpty()) {
            Map<String, String> attributesToWrite = new HashMap<>(this.attributes);
            for (Map.Entry<Extra.ExtraType, Extra<?>> e : this.extras.entrySet()) {
                e.getValue().addAttributesForWrite(attributesToWrite);
            }

            if (!attributesToWrite.isEmpty()) {
                out.append("> ").append(MindMap.allAttributesAsString(attributesToWrite)).append(NEXT_LINE)
                        .append(NEXT_LINE);
            }
        }

        if (!this.extras.entrySet().isEmpty()) {
            List<Extra.ExtraType> types = new ArrayList<>(this.extras.keySet());
            types.sort(Comparator.comparing(Enum::name));

            for (Extra.ExtraType e : types) {
                this.extras.get(e).write(out);
                out.append(NEXT_LINE);
            }
        }

        if (!this.codeSnippets.isEmpty()) {
            List<String> sortedKeys = new ArrayList<>(this.codeSnippets.keySet());
            Collections.sort(sortedKeys);
            for (String language : sortedKeys) {
                String body = this.codeSnippets.get(language);
                out.append("```").append(language).append(NEXT_LINE);
                out.append(body);
                if (!body.endsWith("\n")) {
                    out.append(NEXT_LINE);
                }
                out.append("```").append(NEXT_LINE);
            }
        }

        for (T t : this.getChildren()) {
            t.write(level + 1, out);
        }
    }

    @Override
    public int hashCode() {
        return (int) ((this.localUID >>> 32) ^ (this.localUID & 0xFFFFFFFFL));
    }

    @Override
    public boolean equals(Object topic) {
        if (this == topic) {
            return true;
        }
        if (topic instanceof Topic) {
            return this.localUID == ((T) topic).getLocalUid();
        }
        return false;
    }

    @Override

    public String toString() {
        return "MindMapTopic('%s:%d')".formatted(this.text, this.getLocalUid());
    }

    public long getLocalUid() {
        return this.localUID;
    }

    public boolean hasChildren() {
        this.map.lock();
        try {
            return !this.getChildren().isEmpty();
        } finally {
            this.map.unlock();
        }
    }

    boolean removeAllLinksTo(T topic) {
        boolean result = false;
        if (topic != null) {
            String uid = topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
            if (uid != null) {
                ExtraTopic link = (ExtraTopic) this.getExtras().get(Extra.ExtraType.TOPIC);
                if (link != null && uid.equals(link.getValue())) {
                    this.removeExtra(Extra.ExtraType.TOPIC);
                    result = true;
                }
            }

            for (T ch : this.getChildren()) {
                result |= ch.removeAllLinksTo(topic);
            }
        }

        return result;
    }

    boolean removeTopic(T topic) {
        if (topic == null) {
            return false;
        }
        Iterator<T> iterator = this.children.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (t == topic) {
                iterator.remove();
                return true;
            }
            else if (t.removeTopic(topic)) {
                return true;
            }
        }
        return false;
    }

    public void removeAllChildren() {
        this.children.clear();
    }

    public boolean moveToNewParent(T newParent) {
        this.map.lock();
        try {
            if (newParent == null || this == newParent || this.getParent() == newParent ||
                    this.children.contains(newParent)) {
                return false;
            }

            T theParent = this.parent;
            if (theParent != null) {
                theParent.removeTopic((T) this);
//                theParent.getChildren().remove(this);
            }
            newParent.addChild((T) this);
            this.parent = newParent;
            return true;
        } finally {
            this.map.unlock();
        }
    }


    public T makeChild(String text, T afterTheTopic) {
        this.map.lock();
        try {
            T result = this.createChild(GetUtils.ensureNonNull(text, ""));
            if (afterTheTopic != null && this.children.contains(afterTheTopic)) {
                result.moveAfter(afterTheTopic);
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }


    public T findNext(TopicChecker checker) {
        this.map.lock();
        try {
            T result = null;
            T current = this.getParent();
            if (current != null) {
                int indexThis = current.getChildren().indexOf(this);
                if (indexThis >= 0) {
                    for (int i = indexThis + 1; i < current.getChildren().size(); i++) {
                        if (checker == null) {
                            result = current.getChildren().get(i);
                            break;
                        }
                        else if (checker.check(current.getChildren().get(i))) {
                            result = current.getChildren().get(i);
                            break;
                        }
                    }
                }
            }

            return result;
        } finally {
            this.map.unlock();
        }
    }


    public T findPrev(TopicChecker<T> checker) {
        this.map.lock();
        try {
            T result = null;
            T current = this.getParent();
            if (current != null) {
                int indexThis = current.getChildren().indexOf(this);
                if (indexThis >= 0) {
                    for (int i = indexThis - 1; i >= 0; i--) {
                        if (checker.check(current.getChildren().get(i))) {
                            result = current.getChildren().get(i);
                            break;
                        }
                    }
                }
            }

            return result;
        } finally {
            this.map.unlock();
        }
    }

    public void removeExtras(Extra<?>... extras) {
        this.map.lock();
        try {
            if (extras == null || extras.length == 0) {
                this.extras.clear();
            }
            else {
                for (Extra<?> e : extras) {
                    if (e != null) {
                        this.extras.remove(e.getType());
                    }
                }
            }
        } finally {
            this.map.unlock();
        }
    }

    /**
     * Find max length of children chain. It doesn't count the root topic.
     *
     * @return max length of child chain, 0 if no children.
     * @since 1.4.11
     */
    public int findMaxChildPathLength() {
        int len = 0;
        for (T t : this.getChildren()) {
            int childLen = t.findMaxChildPathLength();
            len = Math.max(len, childLen + 1);
        }
        return len;
    }


    public T findForAttribute(String attrName, String value) {
        if (value.equals(this.getAttribute(attrName))) {
            return (T) this;
        }
        T result = null;
        for (T c : this.getChildren()) {
            result = c.findForAttribute(attrName, value);
            if (result != null) {
                break;
            }
        }
        return result;
    }


    public int[] getPositionPath() {
        List<T> path = getPath();
        int[] result = new int[path.size()];

        T current = path.get(0);
        int index = 1;
        while (index < path.size()) {
            T next = path.get(index);
            int theindex = current.getChildren().indexOf(next);
            result[index++] = theindex;
            if (theindex < 0) {
                break;
            }
            current = next;
        }
        return result;
    }


    /**
     * @return
     */
    public List<T> getPath() {
        List<T> list = new ArrayList<>();
        T current = (T) this;
        do {
            list.add(0, current);
            current = current.getParent();
        }
        while (current != null);
        return list;
    }

    /**
     * copy current topic with extras and attributes and attach to parent.
     * may be deprecated!
     */
    T makeCopy(MindMap<T> newMindMap, T parent, RootCreate<T> creator) {
        this.map.lock();
        try {
            //
            T result = parent == null ?
                    creator.createRoot(newMindMap, this.text, this.extras.values().toArray(new Extra<?>[0])) :
                    parent.createChild(text, this.extras.values().toArray(new Extra<?>[0]));
            for (T c : this.children) {
                c.makeCopy(newMindMap, result, creator);
            }
            result.putAttributes(this.attributes);
            result.putCodeSnippets(this.codeSnippets);
            return result;
        } finally {
            this.map.unlock();
        }
    }

    public boolean removeExtraFromSubtree(Extra.ExtraType... type) {
        boolean result = false;

        this.map.lock();
        try {
            for (Extra.ExtraType t : type) {
                result |= this.extras.remove(t) != null;
            }
            for (T c : this.children) {
                result |= c.removeExtraFromSubtree(type);
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }

    public boolean removeAttributeFromSubtree(String... names) {
        boolean result = false;

        this.map.lock();
        try {
            for (String t : names) {
                result |= this.attributes.remove(t) != null;
            }
            for (T c : this.children) {
                result |= c.removeAttributeFromSubtree(names);
            }
            return result;
        } finally {
            this.map.unlock();
        }
    }

    public boolean deleteLinkToFileIfPresented(File baseFolder, MMapURI file) {
        boolean result = false;
        if (this.extras.containsKey(Extra.ExtraType.FILE)) {
            ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
            if (fileLink.isSameOrHasParent(baseFolder, file)) {
                result = this.extras.remove(Extra.ExtraType.FILE) != null;
            }
        }
        for (T c : this.children) {
            result |= c.deleteLinkToFileIfPresented(baseFolder, file);
        }
        return result;
    }

    public boolean replaceLinkToFileIfPresented(File baseFolder, MMapURI oldFile, MMapURI newFile) {
        boolean result = false;
        if (this.extras.containsKey(Extra.ExtraType.FILE)) {
            ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
            ExtraFile replacement;

            if (fileLink.isSame(baseFolder, oldFile)) {
                replacement = new ExtraFile(newFile);
            }
            else {
                replacement = fileLink.replaceParentPath(baseFolder, oldFile, newFile);
            }

            if (replacement != null) {
                result = true;
                this.extras.remove(Extra.ExtraType.FILE);
                this.extras.put(Extra.ExtraType.FILE, replacement);
            }
        }

        for (T c : this.children) {
            result |= c.replaceLinkToFileIfPresented(baseFolder, oldFile, newFile);
        }
        return result;
    }

    public boolean doesContainFileLink(File baseFolder, MMapURI file) {
        if (this.extras.containsKey(Extra.ExtraType.FILE)) {
            ExtraFile fileLink = (ExtraFile) this.extras.get(Extra.ExtraType.FILE);
            if (fileLink.isSame(baseFolder, file)) {
                return true;
            }
        }
        for (T c : this.children) {
            if (c.doesContainFileLink(baseFolder, file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> iter = this.children.iterator();

        return new Iterator<T>() {
            T childTopic;
            Iterator<T> childIterator;

            @Override
            public void remove() {
                iter.remove();
            }

            Iterator<T> init() {
                if (iter.hasNext()) {
                    this.childTopic = iter.next();
                }
                return this;
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext() || this.childTopic != null ||
                        (this.childIterator != null && this.childIterator.hasNext());
            }

            @Override
            public T next() {
                T result;
                if (this.childTopic != null) {
                    result = this.childTopic;
                    this.childTopic = null;
                    this.childIterator = result.iterator();
                }
                else if (this.childIterator != null) {
                    if (this.childIterator.hasNext()) {
                        result = this.childIterator.next();
                    }
                    else {
                        result = iter.next();
                        this.childIterator = result.iterator();
                    }
                }
                else {
                    throw new NoSuchElementException();
                }
                return result;
            }
        }.init();
    }

    /**
     * Check that the topic contains any code snippet for language from array (case sensitive).
     *
     * @param languageNames names of language
     * @return true if code snippet is detected for any language, false otherwise
     * @since 1.3.1
     */
    public boolean doesContainCodeSnippetForAnyLanguage(String... languageNames) {
        boolean result = false;
        if (!this.codeSnippets.isEmpty()) {
            for (String s : languageNames) {
                if (this.codeSnippets.containsKey(s)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
