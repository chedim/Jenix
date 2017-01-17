package com.onkiup.jendri.db.memory;

import java.sql.Connection;
import java.util.function.BiConsumer;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.QueryResult;
import com.onkiup.jendri.db.Storageable;
import com.onkiup.jendri.util.UnsafeConsumer;

public class Driver extends Database {
    protected Driver(String key) {
        super(key);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public <T extends Storageable> void save(T content) throws Exception {

    }

    @Override
    public <T extends Storageable> void save(T content, BiConsumer<T, Exception> callback) throws Exception {

    }

    @Override
    public <T extends Storageable> void saveImmediately(T content) throws Exception {

    }

    @Override
    public <T extends Storageable> void delete(T content) throws Exception {

    }

    @Override
    public <T extends Storageable> void delete(T content, BiConsumer<T, Exception> callback) throws Exception {

    }

    @Override
    public <T extends Storageable> void deleteImmediately(T content) throws Exception {

    }

    @Override
    public <T extends Fetchable> QueryResult<T> search(QueryBuilder<T> query) {
        return null;
    }

    @Override
    public void destruct() {

    }

    @Override
    public <T extends Fetchable> QueryBuilder<T> createQuery(Class<T> from) throws Exception {
        return null;
    }

    @Override
    public void getConnection(UnsafeConsumer<Connection> consumer) throws Exception {

    }

    @Override
    public <T extends Fetchable> T get(Class<T> type, Object id) throws Exception {
        return null;
    }

    @Override
    public <T extends Fetchable> QueryResult<T> get(QueryBuilder<T> builder) throws Exception {
        return null;
    }

    @Override
    public <X extends Fetchable> long count(QueryBuilder builder) throws Exception {
        return 0;
    }

    @Override
    public <X extends Fetchable> void delete(QueryBuilder xQueryBuilder) throws Exception {

    }

    @Override
    public <X extends Fetchable> void deleteImmediately(QueryBuilder<X> xQueryBuilder) throws Exception {

    }
}
