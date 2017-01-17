package com.onkiup.daria.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.sun.istack.internal.NotNull;

public class ObjectTracker {

    private static List<TrackReference> references = Collections.synchronizedList(new ArrayList<TrackReference>());
    private static final ReferenceQueue<TrackReference> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static Thread runningTracker;
    private static final Lock lock = new Lock();

    public static void track(Object key, Object tracked, @NotNull Consumer callback) {
        references.add(new TrackReference(key, tracked, callback));
        if (runningTracker == null) {
            synchronized (lock) {
                if (runningTracker != null) {
                    runningTracker = new TrackerThread();
                    runningTracker.start();
                }
            }
        }
    }

    private static class Lock {

    }

    private static class TrackerThread extends Thread {

        public TrackerThread() {
            super("Reference Tracking");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TrackReference collected = (TrackReference) REFERENCE_QUEUE.remove();
                    collected.invokeCallback();
                    references.remove(collected);
                } catch (InterruptedException e) {
                    runningTracker = null;
                    break;
                }
            }
        }
    }

    private static class TrackReference extends PhantomReference {

        Object key;
        Consumer callback;

        public TrackReference(Object key, Object referent, @NotNull Consumer callback) {
            super(referent, ObjectTracker.REFERENCE_QUEUE);
            this.key = key;
            this.callback = callback;
        }

        protected void invokeCallback() {
            callback.accept(key);
        }
    }

}
