package com.onkiup.jendri.db;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;
import com.onkiup.jendri.service.StartPoint;
import com.onkiup.jendri.util.UnsafeConsumer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Database implements ServiceStub {

    public static final String CONFIG_PREFIX = "jendri.database.";
    public static final String CONFIG_DRIVER = ".driver";
    protected static final Logger LOGGER = LogManager.getLogger(Database.class);

    @Inject
    public static Class<? extends Database> implementation;

    protected static HashMap<String, Database> instances = new HashMap<>();

    public static Database getInstance() {
        if (instances.size() == 0) {
            instances.put("default", createInstance("default"));
        }
        return instances.get("default");
    }

    private static Database createInstance(String key) {
        try {
            Constructor<? extends Database> constructor = implementation.getConstructor(String.class);
            Database driver = constructor.newInstance(key);
            return driver;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StartPoint getStartPoint() {
        return StartPoint.INIT;
    }

    protected Database(String key) {
        instances.put(key, this);
    }

    public abstract <T extends Storageable> void save(T content) throws Exception;
    public abstract <T extends Storageable> void save(T content, BiConsumer<T, Exception> callback) throws Exception;
    public abstract <T extends Storageable> void saveImmediately(T content) throws Exception;

    public abstract <T extends Storageable> void delete(T content) throws Exception;
    public abstract <T extends Storageable> void delete(T content, BiConsumer<T, Exception> callback) throws Exception;
    public abstract <T extends Storageable> void deleteImmediately(T content) throws Exception;

    public abstract <T extends Fetchable> QueryResult<T> search(QueryBuilder<T> query);

    public abstract void destruct();

    public abstract <T extends Fetchable> QueryBuilder<T> createQuery(Class<T> from) throws Exception;

    public abstract void getConnection(UnsafeConsumer<Connection> consumer) throws Exception;

    public abstract <T extends Fetchable> T get(Class<T> type, Object id) throws Exception;

    public abstract <T extends Fetchable> QueryResult<T> get(QueryBuilder<T> builder) throws Exception;

    public abstract <X extends Fetchable> long count(QueryBuilder builder) throws Exception;

    public abstract <X extends Fetchable> void delete(QueryBuilder xQueryBuilder) throws Exception;

    public abstract <X extends Fetchable> void deleteImmediately(QueryBuilder<X> xQueryBuilder) throws Exception;
}
