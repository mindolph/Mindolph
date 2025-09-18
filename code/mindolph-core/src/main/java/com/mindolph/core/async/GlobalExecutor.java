package com.mindolph.core.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Global executor for async tasks.
 * @since 1.12.4
 * @deprecated to mfx
 */
public class GlobalExecutor {

    public static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static Future<?> submit(Runnable runnable) {
        return executor.submit(runnable);
    }

    public static <T> Future<T> submit(Runnable runnable, T result) {
        return executor.submit(runnable, result);
    }

    public static <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }
}
