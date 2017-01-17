package com.onkiup.jendri.db;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface QueryResult<X extends Fetchable> extends List<X> {

    public QueryBuilder<X> getCreator();

    public QueryResult<X> next() throws Exception;
    public QueryResult<X> prev() throws Exception;

    default void saveAll() throws Exception {
        saveAll(null);
    }

    void saveAll(Consumer<X> trigger) throws Exception;
}
