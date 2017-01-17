package com.onkiup.jendri.db;

import java.util.stream.Stream;

public interface Query<T> {
    static <T extends Fetchable> QueryBuilder<T> from(Class<T> from) {
        try {
            return Database.getInstance().createQuery(from);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    long count();
    Stream<T> stream() throws Exception;
    T fetchOne();
    void delete();
}
