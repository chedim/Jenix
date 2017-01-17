package com.onkiup.jendri.db;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public interface Model extends Storageable, Fetchable, Modificable {

    /**
     * Specifies table/collection name for data fetching
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface StoreAs {
        String value();
    }

    /**
     * Disables table structure updates
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Stable {
        boolean value() default true;
    }

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Embed {
        boolean value() default true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Prefix {
        public String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Index {
        String name();
        String[] value();
    }
}
