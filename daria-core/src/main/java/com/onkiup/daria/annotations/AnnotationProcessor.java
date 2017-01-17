package com.onkiup.daria.annotations;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;

import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.StorageTable;

public interface AnnotationProcessor<A extends Annotation> {

    default void processType(A annotation, StorageTable typeInfo) {
        throw new UnsupportedOperationException();
    }

    default void processField(A annotation, StorageColumn field) {
        throw new UnsupportedOperationException();
    }

    class Static {
        private static final ConcurrentHashMap<Class<? extends AnnotationProcessor>, AnnotationProcessor> INSTANCES = new ConcurrentHashMap<>();

        public static <T extends AnnotationProcessor> T getInstance(Class<T> type) {
            if (!INSTANCES.containsKey(type)) {
                try {
                    INSTANCES.put(type, type.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return (T) INSTANCES.get(type);
        }
    }
}
