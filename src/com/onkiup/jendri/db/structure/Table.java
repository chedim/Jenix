package com.onkiup.jendri.db.structure;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.mysql.exceptions.ItemNotFound;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.util.UnsafeConsumer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public interface Table<RepresentedClass extends PersistantObject> extends SqlEntity<RepresentedClass>, AutoCloseable {


    String getName();

    Field getField(String id);

    Field getPrimaryKey();

    void populateObject(Object primaryKey, RepresentedClass instance) throws Exception;

    List<String> getAllTableNames();

    void fromResultSet(ConnectedResult result, RepresentedClass instance) throws Exception;

    Class<RepresentedClass> getRepresentedClass();

    void save(RepresentedClass content) throws Exception;

    void getConnection(UnsafeConsumer<Connection> consumer) throws Exception;

    void close();

    void drop() throws Exception;

    RepresentedClass createInstance();

    void cloneInto(RepresentedClass source, RepresentedClass target) throws Exception;

    List<Index> getIndexes();

    String getCreateScript();

    static <T extends PersistantObject> Table<T> forJavaClass(Class<T> aClass) throws Exception {
        return forJavaClass(aClass, null);
    }

    Database getSource();

    class Pool {
        private static HashMap<Class, Table> byClass = new HashMap<>();
        private static HashMap<String, Table> byName = new HashMap<>();
    }

    static <T extends PersistantObject> Table<T> forJavaClass(Class<T> jClass, Database source) throws Exception {
        if (source == null) {
            source = Database.getInstance();
        }
        try {
            if (!Pool.byClass.containsKey(jClass)) {
                Table result = Impl.table.newInstance();
                result.setRepresented(jClass);
                result.setSource(source);
                result.init();
                Pool.byClass.put(jClass, result);
                Pool.byName.put(result.getName(), result);
            }
            return Pool.byClass.get(jClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void init() throws Exception;

    void setRepresented(Class<RepresentedClass> jClass);

    void setSource(Database source);

    static Table getByName(String name) {
        if (Pool.byName.containsKey(name)) {
            return Pool.byName.get(name);
        }
        return null;
    }

    public interface Reference extends SqlEntity {
        public Table getTarget();
        public Table getSource();

        public List<Field> getFields();

        void setHard(boolean b);
        boolean isHard();
    }

    public interface ReferenceList extends Reference {
        Table getReferenceTable();
    }

    public interface Field extends SqlEntity<java.lang.reflect.Field> {

        Object get(Object value);

        void set(Object to, Object value);

        java.lang.reflect.Field getRepresentedField();

        class Pool {
            private static HashMap<Table, HashMap<java.lang.reflect.Field, Field>> byField = new HashMap<>();
            private static HashMap<Table, HashMap<String, Field>> byName = new HashMap<>();
        }

        static Field forJavaField(Table table, java.lang.reflect.Field representedField) throws SQLException {
            try {
                Pool.byField.putIfAbsent(table, new HashMap<>());
                if (!Pool.byField.get(table).containsKey(representedField)) {
                    Field result = Impl.field.newInstance();
                    result.setTable(table);
                    result.setRepresentedField(representedField);
                    result.init();
                    Pool.byField.get(table).put(representedField, result);
                    if (!Pool.byName.containsKey(table)) {
                        Pool.byName.put(table, new HashMap<>());
                    }

                    HashMap<String, Field> fields = Pool.byName.get(table);
                    fields.put(result.getName(), result);
                    return result;
                }

                return Pool.byField.get(table).get(representedField);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void init() throws Exception;

        static Field forName(Table owner, String name) {
            if (Pool.byName.containsKey(owner)) {
                return Pool.byName.get(owner).getOrDefault(name, null);
            }

            return null;
        }

        public void setRepresentedField(java.lang.reflect.Field field);

        public void setTable(Table owner);

        public Table getTable();

        public String getName();

        public void setName(String name);

        public DataType getType();

        public void setType(DataType type);

        public boolean canBeNull();

        public void setNull(boolean canBeNull);

        public Object getDefaultValue();

        public void setDefaultValue(Object value);

        public boolean isUnique();

        public void setUnique(boolean unique);

        public boolean isPrimary();

        public void setPrimary(boolean primary);

        public boolean isAutoIncrement();

        public void setAutoIncrement(boolean autoIncrement);

        public Reference getReference();

        public ReferenceList getReferenceList();

        public void setReference(Reference reference);

        boolean isIndexed();

        void setIndexed(boolean is);

        List<ShadowField> getShadowFields();
    }

    public interface ShadowField<T> extends Field {
        String generate(T value);
    }

    public static List<Class> getTypeClasses(Class type) {
        List<Class> result = new ArrayList<>();
        java.lang.reflect.Type[] types = type.getGenericInterfaces();
        for (java.lang.reflect.Type t : types) {
            if (t instanceof ParameterizedType) {
                result.add((Class) ((ParameterizedType) t).getActualTypeArguments()[0]);
            }
        }

        return result;
    }

    public static String generateNameFromCamelCase(String camel) {
        return camel;
    }

    public static String generateNameFromLoDash(String lodash, boolean isClass) {
        String[] parts = lodash.split("_");
        if (isClass) {
            parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
        }
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
            result.append(part);
        }

        return result.toString();
    }

    public static HashMap<String, java.lang.reflect.Field> getClassFields(Class<? extends PersistantObject> represents) {
        HashMap<String, java.lang.reflect.Field> result = new HashMap<>();
        java.lang.reflect.Field[] fields = represents.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            result.put(field.getName(), field);
        }
        Class superClass = represents.getSuperclass();
        if (PersistantObject.class.isAssignableFrom(superClass)) {
            result.putAll(getClassFields(superClass));
        }
        return result;
    }

    public interface Index<Owner extends PersistantObject> extends SqlEntity<Owner> {
        enum Type {
            UNIQUE, FULLTEXT, SPATIAL,
            BTREE, HASH, RTREE
        }
    }

    class Impl {
        @Inject
        public static Class<? extends Table> table = null;
        @Inject
        public static Class<? extends Index> index = null;
        @Inject
        public static Class<? extends Reference> reference = null;
        @Inject
        public static Class<? extends Field> field = null;
    }
}


