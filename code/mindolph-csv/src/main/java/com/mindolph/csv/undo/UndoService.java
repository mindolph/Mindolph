package com.mindolph.csv.undo;

/**
 * @author mindolph.com@gmail.com
 */
public interface UndoService<T> {

    void push(T t);

    boolean undo();

    boolean redo();

    boolean isUndoAvailable();

    boolean isRedoAvailable();

    T getNextUndo();

    T getNextRedo();

    void forgetHistory();

    boolean isPerforming();

}
