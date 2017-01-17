package com.onkiup.daria.exceptions;

import com.onkiup.daria.annotations.StoreIn;

public class UnknownStorageException extends RuntimeException {
    public UnknownStorageException(Class javaClass) {
        super("\nStorage is undefined for class " + javaClass.getName() + "\nUse " + StoreIn.class.getName() + " annotation to specify where to store this class");
    }
}
