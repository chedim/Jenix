package com.onkiup.daria.exceptions;

import com.onkiup.daria.StorageTable;
import com.onkiup.daria.Storageable;

public class NoSuchColumnException extends RuntimeException {
    public <T extends Storageable> NoSuchColumnException(StorageTable<T> tStorageTable, String fieldName) {
        super("Table " + tStorageTable + " has no column named '" + fieldName + "'");
    }
}
