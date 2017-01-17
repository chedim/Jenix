package com.onkiup.jendri.db.mysql;

import static com.github.sommeri.less4j.core.compiler.scopes.InScopeSnapshotRunner.id;
import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.QueryResult;
import com.onkiup.jendri.db.Storageable;
import com.onkiup.jendri.db.mysql.exceptions.ItemNotFound;
import com.onkiup.jendri.db.structure.ConnectedResult;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.mysql.structure.FieldImpl;
import com.onkiup.jendri.db.mysql.structure.IndexImpl;
import com.onkiup.jendri.db.mysql.structure.MySqlType;
import com.onkiup.jendri.db.mysql.structure.ReferenceImpl;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.db.mysql.structure.TableImpl;
import com.onkiup.jendri.injection.Generate;
import com.onkiup.jendri.injection.Injector;
import com.onkiup.jendri.util.UnsafeConsumer;
import com.onkiup.jendri.util.async.AsyncConsumer;
import com.onkiup.jendri.util.async.AsyncFunction;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Driver extends Database {

    @Generate
    private static HashMap<String, Mysql> databases;

    private AsyncFunction<WriteRequest, WriteRequest> writeConsumer = new AsyncFunction<WriteRequest, WriteRequest>() {
        @Override
        public WriteRequest apply(WriteRequest request) {
            if (request.operation != null && request.item != null) {
                try {
                    if (request.operation == Operation.SAVE) {
                        saveImmediately(request.item);
                    } else if (request.operation == Operation.DELETE) {
                        deleteImmediately(request.item);
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to " + request.operation + " " + request.item.getClass().getName() + "#" + request.item.getId(), e);
                    request.error = e;
                }
            } else {
                LOGGER.warn("Invalid write operation request");
            }
            return request;
        }
    };

    private static enum Operation {
        SAVE, DELETE;
    }

    private static class WriteRequest {
        public Operation operation;
        public Storageable item;
        BiConsumer<? extends Storageable, Exception> callback;
        public Exception error;

        public WriteRequest(Operation operation, Storageable content, BiConsumer<? extends Storageable, Exception> callback) {
            this.operation = operation;
            this.item = content;
            this.callback = callback;
        }
    }

    private String key;

    public Driver() {
        this("default");
    }

    public Driver(String key) {
        super(key);

        this.key = key;
    }

    private Connection init() throws SQLException {
        if (databases.containsKey(key)) {
            Mysql db = databases.get(key);
            return DriverManager.getConnection("jdbc:" + db.url, db.user, db.password);
        } else {
            throw new RuntimeException("Database " + key + " not found");
        }
    }

    @Override
    public <T extends Storageable> void save(T content) throws Exception {
        save(content, null);
    }

    @Override
    public <T extends Storageable> void save(@NotNull T content, BiConsumer<T, Exception> callback) throws Exception {
        writeConsumer.put(new WriteRequest(Operation.SAVE, content, callback));
    }

    @Override
    public void saveImmediately(Storageable content) throws Exception {
        try {
            boolean doSave = true;
            if (content.getId() == null) {
                doSave = content.willBeCreated();
            }
            if (doSave && content.willBeSaved()) {
                Table table = Table.forJavaClass(content.getClass(), this);
                table.save(content);
                content.wasSaved();
            }
        } catch (Exception e) {
            content.saveFailed(e);
        }
    }

    @Override
    public <T extends Storageable> void delete(T content) throws Exception {
        writeConsumer.put(new WriteRequest(Operation.DELETE, content, null));
    }

    @Override
    public <T extends Storageable> void delete(T content, BiConsumer<T, Exception> callback) throws Exception {
        writeConsumer.put(new WriteRequest(Operation.DELETE, content, callback));
    }

    @Override
    public <T extends Storageable> void deleteImmediately(T content) throws Exception {
        if (content.willBeDeleted()) {
            try {
                Table<T> table = Table.forJavaClass((Class<T>) content.getClass(), this);
                Table.Field pk = table.getPrimaryKey();
                Object val = pk.get(content);
                String stored = pk.getType().store(val).toString();
                String statement = "DELETE FROM " + table.getName() + " WHERE " + pk.getName() + " = " + stored;
                getConnection(connection -> {
                    connection.createStatement().execute(statement);
                });
                content.wasDeleted();
            } catch (Exception e) {
                content.deleteFailed(e);
            } finally {
                destruct();
            }
        }
    }

    @Override
    public <T extends Fetchable> QueryResult<T> search(QueryBuilder<T> query) {
        return null;
    }

    @Override
    public void destruct() {

    }

    @Override
    public <T extends Fetchable> QueryBuilder<T> createQuery(Class<T> from) throws SQLException {
        return new MySqlQueryBuilder<T>(from, this);
    }

    @Override
    public void getConnection(UnsafeConsumer<Connection> consumer) throws Exception {
        try (Connection connection = init()) {
            consumer.accept(connection);
        }
    }

    @Override
    public <T extends Fetchable> T get(Class<T> type, Object id) throws Exception {
        Table<T> table = Table.forJavaClass(type, this);
        T instance = table.createInstance();
        try {
            table.populateObject(id, instance);
            return instance;
        } catch (ItemNotFound e) {
            return null;
        }
    }

    @Override
    public <T extends Fetchable> QueryResult<T> get(QueryBuilder<T> builder) throws Exception {
        String query = builder.build();
        Class<T> clazz = builder.getType();
        Table<T> table = Table.forJavaClass(clazz, this);
        final QueryResult<T>[] result = new QueryResult[1];
        getConnection(connection -> {
            try {
                ResultSet set = connection.createStatement().executeQuery(query);
                ConnectedResult connectedResult = new ConnectedResult(this, set);
                result[0] = builder.createResult();
                while (set.next()) {
                    T instance = table.createInstance();
                    table.fromResultSet(connectedResult, instance);
                    result[0].add(instance);
                }
            } catch (SQLException e) {
                throw new SQLException(query + ":\n" + e.getMessage(), e.getSQLState(), e.getErrorCode());
            }
        });

        return result[0];
    }

    @Override
    public <X extends Fetchable> long count(QueryBuilder builder) throws Exception {
        String query = "SELECT count(*) as count FROM (" + builder.build() + ") as sub";
        long[] result = new long[1];
        getConnection(connection -> {
            try {
                ResultSet set = connection.createStatement().executeQuery(query);
                while (set.next()) {
                    result[0] = set.getLong(1);
                }
            } catch (SQLException e) {
                throw new SQLException(query + ":\n" + e.getMessage(), e.getSQLState(), e.getErrorCode());
            }
        });

        return result[0];
    }

    @Override
    public <X extends Fetchable> void delete(QueryBuilder builder) throws Exception {
        deleteImmediately(builder);
    }

    @Override
    public <X extends Fetchable> void deleteImmediately(QueryBuilder<X> builder) throws Exception {
        Class<X> clazz = builder.getType();
        Table<X> table = Table.forJavaClass(clazz, this);
        String query = "DELETE FROM " + table.getName() + " WHERE " + id + " IN (SELECT " + id + " FROM (" + builder.build() + ") as data)";

        getConnection(connection -> {
            try {
                String id = table.getPrimaryKey().getName();
                connection.createStatement().execute(query);
            } catch (SQLException e) {
                throw new SQLException(query + ":\n" + e.getMessage(), e.getSQLState(), e.getErrorCode());
            }
        });
    }

    protected void updateStmt(String table, HashMap<String, Object> values, String where) throws Exception {
        ArrayList<String> statements = new ArrayList<>();
        ArrayList<Object> arguments = new ArrayList<>();
        for (String key : values.keySet()) {
            statements.add(key + " = ?");
            arguments.add(values.get(key));
        }

        String query = "UPDATE `" + table + "` SET " + StringUtils.join(statements, ", ") + " WHERE " + where;
        getConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query);

            for (int i = 0; i < arguments.size(); i++) {
                Object argument = arguments.get(i);
                stmt.setObject(i, argument);
            }

            stmt.execute();
        });
    }

    protected void insertStmt(String table, HashMap<String, Object> values) throws Exception {
        ArrayList<String> fields = new ArrayList<>(values.keySet());
        ArrayList<Object> arguments = new ArrayList<>(values.values());

        String query = "INSERT INTO `" + table + "` (" + StringUtils.join(fields, ", ") + ") VALUES (" +
                StringUtils.repeat("?, ", arguments.size()) + ")";
        getConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query);

            for (int i = 0; i < arguments.size(); i++) {
                stmt.setObject(i, arguments.get(i));
            }

            stmt.execute();
        });
    }

    protected void migrate(String table, Class<? extends Storageable> clazz) {

    }

    protected String createField(String table, String name, Class type) {
        if (Number.class.isAssignableFrom(type)) {
            return createNumericField(table, name, type);
        }
        return null;
    }

    protected String createNumericField(String table, String name, Class<? extends Number> type) {
        return null;
    }

    @Override
    public void start() throws ClassNotFoundException, SQLException {
        Injector.set(DataType.class, MySqlType.class);
        Injector.set(Table.Field.class, FieldImpl.class);
        Injector.set(Table.Index.class, IndexImpl.class);
        Injector.set(Table.Reference.class, ReferenceImpl.class);
        Injector.set(Table.class, TableImpl.class);
        Injector.set(DataType.class, MySqlType.class);

        Class.forName("com.mysql.jdbc.Driver");
    }

    @Override
    public void stop() throws SQLException {
        Injector.unset(DataType.class, MySqlType.class);
        Injector.unset(Table.Field.class, FieldImpl.class);
        Injector.unset(Table.Index.class, IndexImpl.class);
        Injector.unset(Table.Reference.class, ReferenceImpl.class);
        Injector.unset(Table.class, TableImpl.class);
        Injector.unset(DataType.class, MySqlType.class);
    }
}
