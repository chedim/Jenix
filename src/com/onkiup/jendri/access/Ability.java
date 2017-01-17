package com.onkiup.jendri.access;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.onkiup.jendri.db.Record;

public class Ability {

    public static final String ADMIN = "admin";
    public static final String GRANT = "grant";
    public static final String REVOKE = "revoke";

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Write {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Read {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Owned {

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface System {

    }
}
