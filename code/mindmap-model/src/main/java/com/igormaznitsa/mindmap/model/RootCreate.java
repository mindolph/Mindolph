package com.igormaznitsa.mindmap.model;

/**
 * @author mindolph.com@gmail.com
 * @param <T>
 */
public interface RootCreate<T extends Topic<T>> {

    T createRoot(MindMap<T> map);

    T createRoot(MindMap<T> map, String text);

    T createRoot(MindMap<T> map, String text, Extra<?>... extras);

}
