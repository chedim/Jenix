package com.onkiup.daria.internal;

import java.util.HashMap;
import java.util.Set;

import com.onkiup.jendri.util.OopUtils;
import static com.onkiup.jendri.util.OopUtils.getBoundClass;
import sun.jvm.hotspot.oops.Oop;

public class TypeParameterMap<T> {

    private HashMap<String, HashMap<Class, Class<? extends T>>> map = new HashMap<>();
    private Set<Class<? extends T>> types;
    private Class<T> baseType;

    public TypeParameterMap(Class<T> type) {
        baseType = type;
        types = OopUtils.getSubClasses(type);
    }

    public synchronized Class<? extends T> get(String param, Class value) {
        if (!map.containsKey(param)) {
            map.put(param, new HashMap<Class, Class<? extends T>>());
        }

        HashMap<Class, Class<? extends T>> paramMap = map.get(param);
        if (!paramMap.containsKey(value)) {
            boolean found = false;
            for (Class<? extends T> type : types) {
                Class bound = OopUtils.getBoundClass(type, baseType, param);
                if (bound.equals(value)) {
                    paramMap.put(value, type);
                    found = true;
                    break;
                }
            }
            if (!found) {
                paramMap.put(value, null);
            }
        }

        return paramMap.get(value);
    }
}
