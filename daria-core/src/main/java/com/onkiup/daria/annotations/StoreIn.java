package com.onkiup.daria.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onkiup.daria.Storage;
import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.StorageTable;
import com.onkiup.daria.exceptions.StorageNotConfiguredException;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DariaAnnotation(StoreIn.Processor.class)
public @interface StoreIn {
    Class<? extends com.onkiup.daria.Storage> value();

    class Processor implements AnnotationProcessor<StoreIn> {

        @Override
        public void processType(StoreIn annotation, StorageTable typeInfo) {
            Storage storage = Storage.INSTANCES.get(annotation.value());
            if (storage == null) {
                throw new StorageNotConfiguredException(typeInfo.getJavaClass(), annotation.value());
            }
            typeInfo.setStorage(storage);
        }

        @Override
        public void processField(StoreIn annotation, StorageColumn field) {

        }
    }
}
