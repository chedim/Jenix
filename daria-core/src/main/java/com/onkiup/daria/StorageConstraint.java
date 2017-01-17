package com.onkiup.daria;

import java.util.Set;

public interface StorageConstraint extends SchemaItem {
    Set<StorageColumn> getColumns();
    Set<StorageTable> getTables();
}
