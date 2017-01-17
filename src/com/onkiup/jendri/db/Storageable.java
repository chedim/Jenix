package com.onkiup.jendri.db;

import com.onkiup.jendri.util.Incrementable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;

public interface Storageable extends PersistantObject {
    public void save() throws Exception;
    public void saveImmediately() throws Exception;
    public void delete() throws Exception;
    public void deleteImmediately() throws Exception;

    public void store(HashMap<String, Object> storage, boolean diff);

    public Long getId();

    default void deleteFailed(Exception e) throws Exception {
        e.printStackTrace();
        throw e;
    }

    default void wasDeleted() {

    }

    default boolean willBeDeleted() {
        return true;
    }

    default boolean willBeSaved() throws Exception {
        return true;
    }

    default void wasSaved() {

    }

    default void saveFailed(Exception e) throws Exception {
        e.printStackTrace();
        throw e;
    }

    default boolean willBeCreated() throws Exception {
        return true;
    }
}
