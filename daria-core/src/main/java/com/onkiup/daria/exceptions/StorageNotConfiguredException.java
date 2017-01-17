package com.onkiup.daria.exceptions;

import com.onkiup.daria.Storage;

public class StorageNotConfiguredException extends RuntimeException {
    public StorageNotConfiguredException(Class javaClass, Class<? extends Storage> value) {
        super("\nClass " + javaClass.getName() + " should be stored in storage of type " + value.getName() + ",\nbut there was no configured storage instances of this type");
    }
}
