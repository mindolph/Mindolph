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
import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MindMap<T extends Topic<T>> implements Serializable, Constants, Iterable<T> {

    public static final String FORMAT_VERSION = "1.1";
    private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s(.+)$");
    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*(\\`+)(.*?)\\2");
    private static final String GENERATOR_VERSION_NAME = "__version__";
    private final transient Lock locker = new ReentrantLock();
    private final Map<String, String> attributes = new TreeMap<>(ModelUtils.STRING_COMPARATOR);

    private T root;

    public MindMap() {
    }

    //    public MindMap(boolean makeRoot) {
//        if (makeRoot) {
//            this.root = new Topic(this, null, "");
//        }
//    }

    public MindMap(MindMap<T> map) {
        this.attributes.putAll(map.attributes);
        T rootTopic = map.getRoot();

        this.root = rootTopic == null ? null : rootTopic.cloneTopic(true);
    }

    public MindMap(Reader reader, RootCreate<T> creator) throws IOException {
        String text = IOUtils.toString(Assertions.assertNotNull(reader));

        MindMapLexer lexer = new MindMapLexer();
        lexer.start(text, 0, text.length(), MindMapLexer.TokenType.HEAD_LINE);

        T rootTopic = null;

        boolean process = true;

        while (process) {
            int oldLexerPosition = lexer.getCurrentPosition().getOffset();
            lexer.advance();
            boolean lexerPositionWasNotChanged = oldLexerPosition == lexer.getCurrentPosition().getOffset();

            MindMapLexer.TokenType token = lexer.getTokenType();
            if (token == null || lexerPositionWasNotChanged) {
                throw new IllegalArgumentException("Wrong format of mind map, end of header is not found");
            }
            switch (token) {
                case HEAD_LINE:
                    continue;
                case ATTRIBUTE: {
                    fillMapByAttributes(lexer.getTokenText(), this.attributes);
                }
                break;
                case HEAD_DELIMITER: {
                    process = false;
                    rootTopic = Topic.parse(this, lexer, creator);
                }
                break;
                default:
                    break;
            }
        }

        this.root = rootTopic;
        this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
    }

    static boolean fillMapByAttributes(String line, Map<String, String> map) {
        Matcher attrMatcher = PATTERN_ATTRIBUTES.matcher(line);
        if (attrMatcher.find()) {
            Matcher attrParser = PATTERN_ATTRIBUTE.matcher(attrMatcher.group(1));
            while (attrParser.find()) {
                map.put(attrParser.group(1), attrParser.group(3));
            }
            return true;
        }
        return false;
    }


    static String allAttributesAsString(Map<String, String> map) throws IOException {
        StringBuilder buffer = new StringBuilder();

        List<String> attrNames = new ArrayList<>(map.keySet());
        Collections.sort(attrNames);

        boolean nonfirst = false;
        for (String k : attrNames) {
            String value = map.get(k);
            if (nonfirst) {
                buffer.append(',');
            }
            else {
                nonfirst = true;
            }
            buffer.append(k).append('=').append(ModelUtils.makeMDCodeBlock(value));
        }

        return buffer.toString();
    }

    public void clear() {
        setRoot(null);
    }


    public T findNext(File baseFolder, T start, Pattern pattern, boolean findInTopicText, Set<Extra.ExtraType> extrasToFind) {
        return this.findNext(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
    }


    public T findNext(File baseFolder, T start,
                      Pattern pattern, boolean findInTopicText,
                      Set<Extra.ExtraType> extrasToFind,
                      Set<TopicFinder<T>> topicFinders) {
        if (start != null && start.getMap() != this) {
            throw new IllegalArgumentException("Topic doesn't belong to the mind map");
        }

        boolean findPluginNote = (extrasToFind != null && !extrasToFind.isEmpty())
                                 && (topicFinders != null && !topicFinders.isEmpty())
                                 && extrasToFind.contains(Extra.ExtraType.NOTE);
        boolean findPluginFile = (extrasToFind != null && !extrasToFind.isEmpty())
                                 && (topicFinders != null && !topicFinders.isEmpty())
                                 && extrasToFind.contains(Extra.ExtraType.FILE);

        T result = null;

        this.lock();
        try {
            boolean startFound = start == null;
            for (T t : this) {
                if (startFound) {
                    if (t.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
                        result = t;
                    }
                    else if (topicFinders != null) {
                        for (TopicFinder<T> f : topicFinders) {
                            if (f.doesTopicContentMatches(t, baseFolder, pattern, extrasToFind)) {
                                result = t;
                                break;
                            }
                        }
                    }
                    if (result != null) {
                        break;
                    }
                }
                else if (t == start) {
                    startFound = true;
                }
            }
        } finally {
            this.unlock();
        }

        return result;
    }


    public T findPrev(File baseFolder, T start,
                      Pattern pattern, boolean findInTopicText,
                      Set<Extra.ExtraType> extrasToFind) {
        return this.findPrev(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
    }


    public T findPrev(File baseFolder, T start, Pattern pattern, boolean findInTopicText,
                      Set<Extra.ExtraType> extrasToFind, Set<TopicFinder<T>> topicFinders) {
        if (start != null && start.getMap() != this) {
            throw new IllegalArgumentException("Topic doesn't belong to the mind map");
        }

        T result = null;

        this.lock();
        try {
            List<T> plain = this.makePlainList();
            int startIndex = start == null ? plain.size() : plain.indexOf(start);
            if (startIndex < 0) {
                throw new IllegalArgumentException(
                        "It looks like that topic doesn't belong to the mind map");
            }
            if (startIndex > 0) {
                while (startIndex > 0 && result == null) {
                    T candidate = plain.get(--startIndex);
                    if (candidate.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
                        result = candidate;
                    }
                    else if (topicFinders != null) {
                        for (TopicFinder<T> f : topicFinders) {
                            if (f.doesTopicContentMatches(candidate, baseFolder, pattern, extrasToFind)) {
                                result = candidate;
                                break;
                            }
                        }
                    }
                }
            }
        } finally {
            this.unlock();
        }

        return result;
    }

    public void setRoot(T newRoot) {
        this.lock();
        try {
            if (newRoot != null) {
                if (newRoot.getMap() != this) {
                    throw new IllegalStateException("Base map must be the same");
                }
            }
            this.root = newRoot;
        } finally {
            this.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        T theroot = this.root;

        return new Iterator<T>() {
            T topicroot = theroot;
            Iterator<T> children;

            @Override
            public void remove() {
                this.children.remove();
            }

            @Override
            public boolean hasNext() {
                return this.topicroot != null || (this.children != null && this.children.hasNext());
            }

            @Override
            public T next() {
                T result;
                if (this.topicroot != null) {
                    result = this.topicroot;
                    this.topicroot = null;
                    this.children = result.iterator();
                }
                else if (this.children != null) {
                    result = this.children.next();
                }
                else {
                    throw new NoSuchElementException();
                }
                return result;
            }
        };
    }

    public boolean isEmpty() {
        this.lock();
        try {
            return this.root == null;
        } finally {
            this.unlock();
        }
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        this.lock();
        try {
            if (value == null) {
                this.attributes.remove(name);
            }
            else {
                this.attributes.put(name, value);
            }
        } finally {
            this.unlock();
        }
    }

    public void resetPayload() {
        this.lock();
        try {
            if (this.root != null) {
                resetPayload(this.root);
            }
        } finally {
            this.unlock();
        }
    }

    private void resetPayload(T t) {
        if (t != null) {
            t.setPayload(null);
            for (T m : t.getChildren()) {
                resetPayload(m);
            }
        }
    }


    public T findForPositionPath(int[] positions) {
        if (positions == null || positions.length == 0) {
            return null;
        }
        if (positions.length == 1) {
            return this.root;
        }

        T result = this.root;
        int index = 1;
        while (result != null && index < positions.length) {
            int elementPosition = positions[index++];
            if (elementPosition < 0 || result.getChildren().size() <= elementPosition) {
                result = null;
                break;
            }
            result = result.getChildren().get(elementPosition);
        }
        return result;
    }


    public T getRoot() {
        this.lock();
        try {
            return this.root;
        } finally {
            this.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MindMap[");
        String delim = "";
        for (T t : this) {
            builder.append(delim);
            builder.append(t);
            delim = ",";
        }
        builder.append(']');
        return builder.toString();
    }


    public String packToString() {
        StringWriter writer;
        this.lock();
        try {
            writer = new StringWriter(16384);
            try {
                write(writer);
            } catch (IOException ex) {
                throw new Error("Unexpected exception", ex);
            }
        } finally {
            this.unlock();
        }
        return writer.toString();
    }


    public <W extends Writer> W write(W out) throws IOException {
        this.lock();
        try {
            out.append("Mind Map generated by Mindolph").append(NEXT_PARAGRAPH);
            this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
            out.append("> ").append(MindMap.allAttributesAsString(this.attributes))
                    .append(NEXT_LINE);
            out.append("---").append(NEXT_LINE);
            T rootTopic = this.root;
            if (rootTopic != null) {
                rootTopic.write(out);
            }
        } finally {
            this.unlock();
        }
        return out;
    }

    public void lock() {
        this.locker.lock();
    }

    public void unlock() {
        this.locker.unlock();
    }


    public T cloneTopic(T topic, boolean cloneFullTree) {
        this.lock();
        try {
            if (topic == null || topic == this.root) {
                return null;
            }

            T clonedTopic = topic.cloneTopic(true);
            clonedTopic.moveToNewParent(topic.getParent());
            // System.out.printf("%s %s %s%n", clonedTopic, clonedTopic.getParent(), clonedTopic.getMap().toString());
            if (!cloneFullTree) {
                clonedTopic.removeAllChildren();
            }

            clonedTopic.removeAttributeFromSubtree(ExtraTopic.TOPIC_UID_ATTR);

            return clonedTopic;
        } finally {
            this.unlock();
        }
    }

    public boolean removeTopic(T topic) {
        this.lock();
        try {
            boolean result;
            T rootTopic = this.root;
            if (rootTopic == null) {
                result = false;
            }
            else if (this.root == topic) {
                rootTopic.setText("");
                rootTopic.removeExtras();
                rootTopic.setPayload(null);
                rootTopic.removeAllChildren();
                result = true;
            }
            else {
                rootTopic.removeTopic(topic);
                result = rootTopic.removeAllLinksTo(topic);
            }
            return result;
        } finally {
            this.unlock();
        }
    }

    /**
     * @param consumer
     * @since 1.3.4
     */
    public void traverseTopicTree(Consumer<T> consumer) {
        traverseTopicTree(this.root, consumer);
    }

    public void traverseTopicTree(T parent, Consumer<T> consumer) {
        consumer.accept(parent);
        List<T> children = parent.getChildren();
        if (children != null) {
            children.forEach(child -> traverseTopicTree(child, consumer));
        }
    }

    /**
     * @param predicate
     * @since 1.3.4
     */
    public boolean anyMatchInTree(Predicate<T> predicate) {
        return this.anyMatchInTree(this.root, predicate);
    }

    public boolean anyMatchInTree(T parent, Predicate<T> predicate) {
        if (predicate.test(parent)) {
            return true;
        }
        List<T> children = parent.getChildren();
        if (children != null) {
            for (T child : children) {
                if (anyMatchInTree(child, predicate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param predicate
     * @since 1.3.4
     */
    public Optional<T> findFirstInTree(Predicate<T> predicate) {
        return this.findFirstInTree(this.root, predicate);
    }

    public Optional<T> findFirstInTree(T parent, Predicate<T> predicate) {
        if (predicate.test(parent)) {
            return Optional.ofNullable(parent);
        }
        List<T> children = parent.getChildren();
        if (children != null) {
            for (T child : children) {
                if (predicate.test(child)) {
                    return Optional.ofNullable(child);
                }
                else {
                    Optional<T> inChildren = findFirstInTree(child, predicate);
                    if (inChildren.isPresent()) {
                        return inChildren;
                    }
                }
            }
        }
        return Optional.empty();
    }

    public T findTopicForLink(ExtraTopic link) {
        T result = null;
        if (link != null) {
            T rootTopic = this.root;
            if (rootTopic != null) {
                this.lock();
                try {
                    result = rootTopic.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, link.getValue());
                } finally {
                    this.unlock();
                }
            }
        }
        return result;
    }

    /**
     * @param type
     * @return
     * @deprecated
     */
    public List<T> findAllTopicsForExtraType(Extra.ExtraType type) {
        List<T> result = new ArrayList<>();
        this.traverseTopicTree(this.root, topics -> {
            if (topics.getExtras().containsKey(type)) {
                result.add(topics);
            }
        });
        return result;
    }


    public T getChild(T parent, int index) {
        return parent.getChildren().get(index);
    }

    public int getChildCount(T parent) {
        return parent.getChildren().size();
    }

    public List<T> makePlainList() {
        this.lock();
        try {
            List<T> result = new ArrayList<>();
            for (T t : this) {
                result.add(t);
            }
            return result;
        } finally {
            this.unlock();
        }
    }

}
