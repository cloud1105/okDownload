package com.leo.okdownload.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author leo
 */
public class CustomThreadFactory implements ThreadFactory {
    private final String groupName;
    private final AtomicInteger nextId = new AtomicInteger(1);

    public CustomThreadFactory(String groupName) {
        this.groupName = "ThreadFactoryMain -" + groupName + "-worker-";
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        String threadName = groupName + nextId.incrementAndGet();
        return new Thread(null,r,threadName,0);
    }
}
