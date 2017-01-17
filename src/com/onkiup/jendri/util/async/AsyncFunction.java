package com.onkiup.jendri.util.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.sun.istack.internal.NotNull;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AsyncFunction<X, R> implements IAsyncFunction<X, R> {
    private static final Logger LOGGER = LogManager.getLogger(AsyncFunction.class);

    private ArrayBlockingQueue<X> requests = new ArrayBlockingQueue<X>(10);
    private ArrayBlockingQueue<R> responses = new ArrayBlockingQueue<R>(10);

    private boolean closed = false;
    private List<AsyncFunctionThread> processors = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger running = new AtomicInteger(0);

    public AsyncFunction() {
        spawnProcessors();
    }

    public AsyncFunction(ArrayBlockingQueue<X> requests) {
        this();
        this.requests = requests;
    }

    public void put(X request) {
        if (closed) {
            throw new RuntimeException("The queue is closed");
        }
        requests.offer(request);
    }

    public R get() {
        if (closed && requests.size() == 0 && responses.size() == 0) {
            throw new NoSuchElementException();
        }
        return responses.poll();
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
        while (processors.size() > 0 && !Thread.currentThread().isInterrupted()) {
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
            try {
                processors.add(this);
                LOGGER.info("Processor spawned: " + this.toString());
                while (!((closed && requests.isEmpty()) || Thread.currentThread().isInterrupted())) {
                    X request = requests.take();
                    if (request != null) {
                        if (!Thread.currentThread().isInterrupted()) {
                            try {
                                isActive.set(true);
                                running.addAndGet(1);
                                R response = apply(request);
                                responses.offer(response);
                            } catch (Exception e) {
                                LOGGER.error("Error while processing " + request, e);
                            } finally {
                                isActive.set(false);
                                running.addAndGet(-1);
                            }
                        } else {
                            requests.add(request);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.info("Processor has been interrupted", e);
            } finally {
                processors.remove(this);
                LOGGER.info("Processor stopped: " + this.toString());
            }
        }
    }
}
