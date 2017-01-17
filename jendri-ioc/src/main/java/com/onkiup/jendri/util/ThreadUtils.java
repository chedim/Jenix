package com.onkiup.jendri.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ThreadUtils {

    private static Logger LOGGER = LogManager.getLogger(ThreadUtils.class);

    public static Thread fork(String name, Runnable r) {
        Thread thread = new Thread(r, name);
        thread.start();
        return thread;
    }

    public static Thread forkWithTimer(String name, long limit, Runnable r) {
        final Thread workThread = fork(name, r);
        fork("Timer for [" + workThread.getName() + "]", () -> {
            try {
                Thread.sleep(limit);
            } catch (InterruptedException e) {
                LOGGER.error("timer sleep interrupted", e);
            }
            if (workThread.isAlive()) {
                workThread.interrupt();
            }
        });
        return workThread;
    }
}
