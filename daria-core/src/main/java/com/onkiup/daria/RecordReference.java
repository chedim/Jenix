package com.onkiup.daria;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.stream.Stream;

public class RecordReference<T extends Storageable> extends WeakReference<T> {

    public RecordReference(T referent) {
        super(referent);
    }

    public RecordReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

    @Override
    public T get() {
        return super.get();
    }
}
