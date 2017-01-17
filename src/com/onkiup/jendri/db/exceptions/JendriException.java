package com.onkiup.jendri.db.exceptions;

import com.onkiup.jendri.db.Model;

public abstract class JendriException extends Exception {
    public JendriException() {
    }

    public JendriException(String message) {
        super(message);
    }

    public JendriException(String message, Throwable cause) {
        super(message, cause);
    }

    public JendriException(Throwable cause) {
        super(cause);
    }

    public JendriException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
