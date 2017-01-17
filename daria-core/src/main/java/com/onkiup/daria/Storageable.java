package com.onkiup.daria;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public interface Storageable {
    <T> T as(Class<T> type);
    void save();
    void saveEventually();
}
