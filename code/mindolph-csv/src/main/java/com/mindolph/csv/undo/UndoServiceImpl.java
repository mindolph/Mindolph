package com.mindolph.csv.undo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author mindolph.com@gmail.com
 */
public class UndoServiceImpl<T> implements UndoService<T> {

    private static final Logger log = LoggerFactory.getLogger(UndoServiceImpl.class);
    private final List<Command<T>> queue = new LinkedList<>();
    private int currentPos = 0; // point to the element that current on UI

    // applier for undo/redo
    private final Consumer<T> applier;

    private final AtomicBoolean isPerforming = new AtomicBoolean(false);

    private Function<String, String> abbreviate;

    public UndoServiceImpl(Consumer<T> applier) {
        this.applier = applier;
    }

    public UndoServiceImpl(Consumer<T> applier, Function<String, String> abbreviate) {
        this.applier = applier;
        this.abbreviate = abbreviate;
    }

    @Override
    public void push(T t) {
        if (!queue.isEmpty() && queue.get(currentPos).getCommand().equals(t)) {
            log.debug("No actual changes");
            return;
        }
        if (currentPos < queue.size()) {
            queue.subList(currentPos + 1, queue.size()).clear();
        }
        queue.add(new Command<>(t));
        currentPos = queue.size() - 1;
        log.debug(printQueue());
    }

    @Override
    public boolean undo() {
        if (!isUndoAvailable()) {
            return false;
        }
        log.debug("Undo to %d".formatted(currentPos - 1));
        isPerforming.set(true);
        Command<T> cmd = queue.get(currentPos - 1);
        currentPos -= 1;
        applier.accept(cmd.getCommand());
        isPerforming.set(false);
        log.debug(printQueue());
        return true;
    }

    @Override
    public boolean redo() {
        if (!isRedoAvailable()) {
            return false;
        }
        log.debug("Redo to %d".formatted(currentPos + 1));
        isPerforming.set(true);
        Command<T> cmd = queue.get(currentPos + 1);
        currentPos += 1;
        applier.accept(cmd.getCommand());
        isPerforming.set(false);
        log.debug(printQueue());
        return true;
    }

    @Override
    public boolean isUndoAvailable() {
        return currentPos > 0 && currentPos < queue.size() && queue.get(currentPos - 1) != null;
    }

    @Override
    public boolean isRedoAvailable() {
        return currentPos < (queue.size() - 1) && queue.get(currentPos + 1) != null;
    }

    @Override
    public T getNextUndo() {
        return currentPos > 0 && currentPos < queue.size() ? queue.get(currentPos - 1).getCommand() : null;
    }

    @Override
    public T getNextRedo() {
        return currentPos < (queue.size() - 1) ? queue.get(currentPos + 1).getCommand() : null;
    }

    @Override
    public void forgetHistory() {
        queue.clear();
        log.debug(printQueue());
    }

    @Override
    public boolean isPerforming() {
        return isPerforming.get();
    }

    String printQueue() {
        StringBuilder buf = new StringBuilder();
        for (Command<T> cmd : queue) {
            String abb = abbreviate == null ? StringUtils.abbreviateMiddle(cmd.getCommand().toString(), ".", 5) : abbreviate.apply(cmd.getCommand().toString());
            buf.append(" (").append(abb).append(")");
            if (queue.indexOf(cmd) == currentPos) {
                buf.append("<");
            }
        }
        return buf.toString();
    }
}
