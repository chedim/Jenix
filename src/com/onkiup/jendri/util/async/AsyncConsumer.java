package com.onkiup.jendri.util.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AsyncConsumer<X> implements IAsyncConsumer<X> {
    private static final Logger LOGGER = LogManager.getLogger(AsyncFunction.class);

    private SynchronousQueue<X> requests = new SynchronousQueue<>();

    private boolean closed = false;
    private List<AsyncFunctionThread> processors = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger running = new AtomicInteger(0);

    public AsyncConsumer() {
    }

    public AsyncConsumer(SynchronousQueue<X> requests) {
        this.requests = requests;
    }

    public void put(X request) {
        if (closed) {
            throw new RuntimeException("The queue is closed");
        }
        try {
            requests.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void spawnProcessors() {
        spawnProcessors(Runtime.getRuntime().availableProcessors());
    }

    public void spawnProcessors(int number) {
        for (int i = processors.size(); i < number + 1; i++) {
            AsyncFunctionThread thread = new AsyncFunctionThread();
            thread.start();
        }
    }

    public void killProcessors(int number) {
        while (processors.size() > number) {
            for (AsyncFunctionThread processor : processors) {
                if (!processor.isActive.get()) {
                    processor.interrupt();
                }
            }
        }
    }

    public void close() {
        closed = true;
    }

    public void closeAndWait() {
        close();
        while(processors.size() > 0 && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private class AsyncFunctionThread extends Thread {

        private AtomicBoolean isActive = new AtomicBoolean();

        @Override
        public void run() {
            processors.add(this);
            while(!((closed && requests.isEmpty()) || Thread.currentThread().isInterrupted())) {
                X request = requests.poll();
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        isActive.set(true);
                        running.addAndGet(1);
                        accept(request);
                    } catch (Exception e) {
                        LOGGER.error("Error while processing " + request, e);
                    } finally {
                        isActive.set(false);
                        running.addAndGet(-1);
                    }
                } else {
                    try {
                        requests.put(request);
                    } catch (InterruptedException e) {
                        LOGGER.error("Unable to put " + request + " back in queue");
                        throw new RuntimeException(e);
                    }
                }
            }
            processors.remove(this);
        }
    }
}
