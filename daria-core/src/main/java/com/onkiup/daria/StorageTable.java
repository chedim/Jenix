package com.onkiup.daria;

import java.util.Set;

import com.onkiup.daria.annotations.AnnotationProcessor;
import com.onkiup.daria.annotations.StoreIn;
import com.onkiup.daria.exceptions.StorageNotConfiguredException;

public interface StorageTable<C extends Storageable> extends SchemaItem {
    Class<C> getJavaClass();

    void setStorage(Storage storage);

    Storage getStorage();

    StorageColumn<C, ?> getColumn(String name);

    StorageIndex getIndex(String indexName);

    Set<StorageIndex> getIndexes();
}
