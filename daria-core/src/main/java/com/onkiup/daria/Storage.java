package com.onkiup.daria;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import com.onkiup.daria.internal.InstancesHolder;
import com.onkiup.daria.query.language.daria1.Daria1Lexem;

public interface Storage {

    InstancesHolder<Storage> INSTANCES = new InstancesHolder();

    Object getIdentifier(Object o);

    <T extends Storageable> T getOriginal(T o);

    <T extends Storageable> T fetch(Class<T> from, Object identifier);

    <T extends Storageable> Stream<T> query(Class<T> from, Daria1Lexem filter, long skip, long limit);

    void store(Storageable o);

    <T extends Storageable> T instantiate(Class<T> type);

    <T extends Storageable> StorageTable<T> getTable(Class<T> type);

    <C extends Storageable> StorageColumn<C, ?> getColumn(StorageTable<C> table, Field field);

    StorageOperationResult execute(StorageOperation operation);

    <T> StorageType<T,?> getType(Class<T> type);
}
