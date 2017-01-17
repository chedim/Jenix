package com.onkiup.daria;

import java.util.Set;

public interface StorageIndex extends SchemaItem {
    Set<StorageColumn> getColumns();
    void addColumn(StorageColumn column);
}
