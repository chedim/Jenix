package com.onkiup.daria;

import java.util.List;

import com.onkiup.daria.internal.InstancesHolder;
import com.onkiup.daria.internal.TypeParameterMap;

public interface StorageType<J, S> {
    InstancesHolder<StorageType> INSTANCES = new InstancesHolder<>();
    TypeParameterMap<StorageType> MAP = new TypeParameterMap<>(StorageType.class);

    <T extends Storageable> void parse(StorageOperationResult source, T target, StorageColumn<T, S> column);
    <T extends Storageable> List<StorageOperation> store(T source, StorageColumn<T, J> column);
}