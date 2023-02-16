package com.mindolph.mindmap.extension;

import com.igormaznitsa.mindmap.model.TopicFinder;
import com.mindolph.mindmap.extension.api.Extension;
import com.mindolph.mindmap.extension.attributes.emoticon.EmoticonPopUpMenuExtension;
import com.mindolph.mindmap.extension.attributes.emoticon.EmoticonVisualAttributeExtension;
import com.mindolph.mindmap.extension.attributes.images.ImagePopUpMenuExtension;
import com.mindolph.mindmap.extension.attributes.images.ImageVisualAttributeExtension;
import com.mindolph.mindmap.extension.manipulate.*;
import com.mindolph.mindmap.extension.process.*;
import com.mindolph.mindmap.model.TopicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public final class MindMapExtensionRegistry implements Iterable<Extension> {

    private final Logger log = LoggerFactory.getLogger(MindMapExtensionRegistry.class);

    private static final MindMapExtensionRegistry INSTANCE = new MindMapExtensionRegistry();
    private final List<Extension> extensionList = new ArrayList<>();
    private final Map<Class<? extends Extension>, List<? extends Extension>> FIND_CACHE = new HashMap<>();

    private MindMapExtensionRegistry() {
        registerExtension(new EditTextExtension());
        registerExtension(new AddChildExtension());
        registerExtension(new CloneTopicExtension());
        registerExtension(new RemoveTopicExtension());
        registerExtension(new TextAlignMenuExtension());

        registerExtension(new ShowJumpsExtension());
        registerExtension(new CollapseAllExtension());
        registerExtension(new ExpandAllExtension());
        registerExtension(new TopicColorExtension());
        registerExtension(new ConvertTopicExtension());

        registerExtension(new EmoticonPopUpMenuExtension());
        registerExtension(new EmoticonVisualAttributeExtension());

        registerExtension(new ImagePopUpMenuExtension());
        registerExtension(new ImageVisualAttributeExtension());
    }

    public Set<TopicFinder<TopicNode>> findAllTopicFinders() {
        Set<TopicFinder<TopicNode>> result = new HashSet<>();
        for (Extension p : this.extensionList) {
            if (p instanceof TopicFinder) {
                result.add((TopicFinder<TopicNode>) p);
            }
        }
        return result;
    }

    public static MindMapExtensionRegistry getInstance() {
        return INSTANCE;
    }

    public void registerExtension(Extension extension) {
        synchronized (FIND_CACHE) {
            this.extensionList.add(extension);
            log.info("Registered extension " + extension.getClass().getName());
            Collections.sort(this.extensionList);
            FIND_CACHE.clear();
        }
    }

    public void unregisterExtensionForClass(Class<? extends Extension> extensionClass) {
        synchronized (FIND_CACHE) {
            Iterator<Extension> iterator = this.extensionList.iterator();
            while (iterator.hasNext()) {
                Extension extension = iterator.next();
                if (extensionClass.isAssignableFrom(extension.getClass())) {
                    log.info(String.format("Unregistered extension %s for class %s", extension.getClass().getName(), extensionClass.getName()));
                    iterator.remove();
                }
            }
        }
    }

    public void unregisterExtension(Extension extension) {
        synchronized (FIND_CACHE) {
            if (this.extensionList.remove(extension)) {
                log.info("Unregistered extension " + extension.getClass().getName());
                Collections.sort(this.extensionList);
            }
            FIND_CACHE.clear();
        }
    }

    public int size() {
        synchronized (FIND_CACHE) {
            return this.extensionList.size();
        }
    }

    public void clear() {
        synchronized (FIND_CACHE) {
            this.extensionList.clear();
            FIND_CACHE.clear();
        }
    }

    public <T extends Extension> List<T> findFor(Class<T> klazz) {
        synchronized (FIND_CACHE) {
            List<T> result = (List<T>) FIND_CACHE.get(klazz);

            if (result == null) {
                result = new ArrayList<>();
                if (klazz != null) {
                    for (Extension p : this.extensionList) {
                        if (klazz.isInstance(p)) {
                            result.add(klazz.cast(p));
                        }
                    }
                }
                result = Collections.unmodifiableList(result);
                FIND_CACHE.put(klazz, result);
            }
            return result;
        }
    }

    @Override
    public Iterator<Extension> iterator() {
        return this.extensionList.iterator();
    }
}
