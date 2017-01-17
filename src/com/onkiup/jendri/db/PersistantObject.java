package com.onkiup.jendri.db;

public interface PersistantObject {
    default void beforeInsert() throws Exception {

    }

    default void beforeUpdate() throws Exception {

    }

    default void afterInsert() throws Exception {

    }

    default void afterUpdate() throws Exception {

    }

    default void beforeCloneInto(PersistantObject target) throws Exception {

    }

    default void beforeCloneFrom(PersistantObject source) throws Exception {

    }

    default void afterCloneInto(PersistantObject target) throws Exception {

    }

    default void afterCloneFrom(PersistantObject source) throws Exception {

    }

    default void beforeLoaded() throws Exception {

    }

    default void afterLoaded() throws Exception {

    }
}
