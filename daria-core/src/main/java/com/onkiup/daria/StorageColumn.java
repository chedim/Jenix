package com.onkiup.daria;

import java.lang.reflect.Field;
import java.util.Set;

public interface StorageColumn<C extends Storageable, T> extends SchemaItem {
    StorageTable<C> getTable();

    Field getJavaField();

    Set<StorageIndex> getIndexes();
}
