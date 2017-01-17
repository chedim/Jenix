package com.onkiup.jendri.util.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.istack.internal.NotNull;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AsyncSupplier<R>  implements IAsyncSupplier<R> {
    private static final Logger LOGGER = LogManager.getLogger(AsyncFunction.class);

    private SynchronousQueue<R> responses = new SynchronousQueue<>();

    private boolean closed = false;
    private List<AsyncFunctionThread> processors = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger running =  new AtomicInteger(0);

    public AsyncSupplier() {
    }

    public R get() {
        if (closed && responses.isEmpty()) {
            throw new NoSuchElementException();
        }
        return responses.poll();
    }

    public R get(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        if (closed && running.get() == 0 && responses.size() == 0) {
            throw new NoSuchElementException();
        }
        return responses.poll(timeout, unit);
    }

    public void spawnProcessors() {
        spawnProcessors(Runtime.getRuntime().availableProcessors());
    }

    public void spawnProcessors(int number) {
        closed = false;
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
            try {
                processors.add(this);
                while (!(closed || Thread.currentThread().isInterrupted())) {
                    if (!Thread.currentThread().isInterrupted()) {
                        try {
                            isActive.set(true);
                            running.addAndGet(1);
                            R response = get();
                            responses.put(response);
                        } catch (Exception e) {
                            LOGGER.error("Error while supply", e);
                        } finally {
                            isActive.set(false);
                            running.addAndGet(-1);
                        }
                    }
                }
            } finally {
                processors.remove(this);
            }
        }
    }
}
