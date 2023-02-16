package com.mindolph.mindmap.event;

import com.mindolph.mindmap.MindMapView;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.TopicNode;
import org.reactfx.EventSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public class MindmapEvents {

    private static final Map<TopicNode, EventSource<NoteEditorData>> noteSaveEvents = new HashMap<>();
    private static final Map<MindMapView, EventSource<Void>> mmdSaveEvents = new HashMap<>();

    public static void subscribeNoteSaveEvent(TopicNode topic, Consumer<NoteEditorData> consumer) {
        EventSource<NoteEditorData> eventSource = noteSaveEvents.get(topic);
        if (eventSource == null) {
            eventSource = new EventSource<>();
            noteSaveEvents.put(topic, eventSource);
            eventSource.subscribe(consumer);
        }
    }

    public static void subscribeMmdSaveEvent(MindMapView mmv, Consumer<Void> consumer) {
        EventSource<Void> eventSource = mmdSaveEvents.get(mmv);
        if (eventSource == null) {
            eventSource = new EventSource<>();
            mmdSaveEvents.put(mmv, eventSource);
            eventSource.subscribe(consumer);
        }
    }

    public static void notifyNoteSave(TopicNode topic, NoteEditorData data) {
        EventSource<NoteEditorData> eventSource = noteSaveEvents.get(topic);
        if (eventSource != null) {
            eventSource.push(data);
        }
    }

    public static void notifyMmdSave(MindMapView mmv) {
        EventSource<Void> eventSource = mmdSaveEvents.get(mmv);
        if (eventSource != null) {
            eventSource.push(null);
        }
    }

    public static void unsubscribeMmdSaveEvent(MindMapView mmv) {
        mmdSaveEvents.remove(mmv);
    }

    public static void unsubscribeNoteSaveEvent(TopicNode topic) {
        noteSaveEvents.remove(topic);
    }
}
