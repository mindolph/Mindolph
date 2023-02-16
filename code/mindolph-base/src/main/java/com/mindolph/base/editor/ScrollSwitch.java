package com.mindolph.base.editor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scroll switch between 2 scrollable panels.
 */
public class ScrollSwitch {

    protected AtomicBoolean firstScrolling = new AtomicBoolean(false);
    protected AtomicBoolean secondScrolling = new AtomicBoolean(false);

    public void scrollFirst(Runnable runnable) {
        if (!secondScrolling.get()) {
            firstScrolling.set(true);
            runnable.run();
            new Thread(() -> {
                try {
                    Thread.sleep(150); // avoid the event loop
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                firstScrolling.set(false);
            }).start();
        }
    }

    public void scrollSecond(Runnable runnable) {
        if (!firstScrolling.get()) {
            secondScrolling.set(true);
            runnable.run();
            new Thread(() -> {
                try {
                    Thread.sleep(150); // avoid the event loop
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                secondScrolling.set(false);
            }).start();
        }
    }
}
