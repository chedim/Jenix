package com.onkiup.daria.internal;

import java.util.HashMap;

public class InstancesHolder<TYPE> {
    private HashMap<Class<? extends TYPE>, TYPE> instances = new HashMap<>();

    public synchronized <T extends TYPE> T get(Class<T> type) {
        if (!instances.containsKey(type)) {
            try {
                instances.put(type, type.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return (T) instances.get(type);
    }
}
