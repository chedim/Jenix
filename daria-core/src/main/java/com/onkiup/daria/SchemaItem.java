package com.onkiup.daria;

import java.util.List;

public interface SchemaItem {
    List<StorageOperation> getSchemaUpdateOperations();
}
