package com.onkiup.daria;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.onkiup.daria.annotations.Index;
import com.onkiup.daria.annotations.StoreIn;
import com.onkiup.jendri.injection.Inject;

public class Daria {
    @Inject
    private static Storage defaultStorage;
    private static ConcurrentHashMap<Class<? extends Storage>, Storage> STORAGE_INSTANCES = new ConcurrentHashMap<>();
    private static final ReferenceQueue REFERENCE_QUEUE = new ReferenceQueue();

    public static void store(Storageable o) {
        getStorage(o.getClass()).store(o);
    }

    public static <T extends Storageable> T fetch(Class<T> from, Object id) {
        return getStorage(from).fetch(from, id);
    }

    public static <T extends Storageable> T getOriginal(T o) {
        return getStorage(o.getClass()).getOriginal(o);
    }

    public static Storage getStorage(Class forType) {
        StoreIn annotation = (StoreIn) forType.getAnnotation(StoreIn.class);
        if (annotation != null) {
            Class<? extends Storage> storageClass = annotation.value();
            if (storageClass != null) {
                if (!STORAGE_INSTANCES.containsKey(storageClass)) {
                    try {
                        Storage storage = storageClass.newInstance();
                        STORAGE_INSTANCES.put(storageClass, storage);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return STORAGE_INSTANCES.get(storageClass);
            }
        }
        return defaultStorage;
    }

    public static <T extends Storageable> T instantiate(Class<T> type) {
        return getStorage(type).instantiate(type);
    }

    public static Object getIdentifier(Object o) {
        return getStorage(o.getClass()).getIdentifier(o);
    }

    public static StorageTable getTable(Class from) {
        return getStorage(from).getTable(from);
    }
}
