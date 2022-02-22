package com.leo.okdownload.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskPool {
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 16;
    private static final int KEEP_ALIVE_TIME = 30;

    private static TaskPool instance;
    private final Executor executor;

    private TaskPool() {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10), (runnable, threadPoolExecutor) -> {
                });
    }

    public static TaskPool getInstance() {
        if (instance == null) {
            instance = new TaskPool();
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

}
