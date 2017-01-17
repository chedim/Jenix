package com.onkiup.jendri.db.mysql.structure;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.Model;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.Indexes;
import com.onkiup.jendri.db.structure.ConnectedResult;
import com.onkiup.jendri.db.mysql.exceptions.ItemNotFound;
import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.util.UnsafeConsumer;
import org.apache.commons.lang3.StringUtils;

public class TableImpl<T extends PersistantObject> implements Table<T> {

    private Class<T> represents;

    private Declaration declaration;
    private Database source;

    public void init() throws Exception {
        declaration = new Declaration();
        declaration.populate();
        getConnection(connection -> {
            connection.setAutoCommit(false);
            update();
            connection.commit();
            declaration.cleanIndices(connection);
            connection.commit();
            connection.setAutoCommit(true);
        });
    }

    @Override
    public void update() throws Exception {
        declaration.update();
    }

    @Override
    public boolean existsInDatabase() {
        return declaration.tableExists;
    }

    @Override
    public boolean matchesDatabase() {
        return declaration.tableExists && declaration.tableMatches;
    }

    @Override
    public String getName() {
        return declaration.name;
    }

    @Override
    public Field getField(String id) {
        Field f = declaration.getField(id);
        return f;
    }

    @Override
    public Field getPrimaryKey() {
        return declaration.getPrimaryKey();
    }

    @Override
    public void populateObject(Object primaryKey, T instance) throws Exception {
        try {
            if (instance == null) {
                instance = represents.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Field pkField = declaration.getPrimaryKey();
        DataType type = pkField.getType();
        String tableName = Table.generateNameFromCamelCase(represents.getSimpleName());
        String fieldName = pkField.getName();

        List<String> joins = new ArrayList<>();
        if (declaration.parent != null) {
            List<String> parentTables = declaration.parent.getAllTableNames();
            for (String parentTable : parentTables) {
                joins.add(" RIGHT JOIN " + parentTable + " ON `" + tableName + "`.`" + fieldName + "` = `" + parentTable + "`.`" + fieldName + "`");
            }
        }

        final T finalInstance = instance;
        getConnection(connection -> {
            try (Statement stmt = connection.createStatement()) {
                String query = "SELECT * FROM `" + tableName + "` " + StringUtils.join(joins, " ") +
                        " WHERE `" + tableName + "`.`" + fieldName + "` = " + type.store(primaryKey);
                ResultSet resultSet = stmt.executeQuery(query);
                if (resultSet.next()) {
                    ConnectedResult result = new ConnectedResult(source, resultSet);
                    fromResultSet(result, finalInstance);
                } else {
                    throw new ItemNotFound();
                }
            }
        });
    }

    @Override
    public List<String> getAllTableNames() {
        List<String> names = new ArrayList<>();
        names.add(getName());
        if (declaration.parent != null) {
            names.addAll(declaration.parent.getAllTableNames());
        }

        return names;
    }

    @Override
    public void fromResultSet(ConnectedResult result, T instance) throws Exception {
        if (!Fetchable.class.isAssignableFrom(represents)) {
            throw new RuntimeException("Type " + represents.getName() + " is not fetchable.");
        }

        ResultSet resultSet = result.getResult();
        instance.beforeLoaded();
        try {
            if (declaration.parent != null) {
                declaration.parent.fromResultSet(result, instance);
            }

            Fetchable fetchable = (Fetchable) instance;
            ResultSetMetaData meta = resultSet.getMetaData();
            HashMap<String, Object> OLD = new HashMap<>();
            for (int i = 0; i < meta.getColumnCount(); i++) {
                String colName = meta.getColumnName(i + 1);
                Field field = declaration.getField(colName);
                if (field != null) {
                    DataType fieldType = field.getType();
                    java.lang.reflect.Field originalField = field.getRepresentedField();
                    Object value = fieldType.read(result, i + 1, originalField.getType(), fetchable.getId());
                    OLD.put(field.getName(), value);
                    if (!(field instanceof ShadowField)) {
                        Fetchable.setField(fetchable, originalField, value);
                    }
                }
            }
            if (instance instanceof Record) {
                java.lang.reflect.Field oldValues = Record.class.getDeclaredField("OLD");
                Fetchable.setField(fetchable, oldValues, OLD);
            }
        } catch (ItemNotFound e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        instance.afterLoaded();
    }

    @Override
    public Class getRepresentedClass() {
        return represents;
    }

    @Override
    public void save(T content) throws Exception {

        HashMap<String, Object> values = new HashMap<>();
        HashMap<String, Table.Field> fields = declaration.getFields();
        Field primary = getPrimaryKey();
        Long id = (Long) primary.get(content);
        Boolean insert = id == null;

        if (declaration.parent != null) {
            declaration.parent.save(content);
        }

        for (String key : fields.keySet()) {
            Table.Field field = fields.get(key);
            Object value = field.get(content);
            Object storeValue = null;
            if (field.isPrimary() && declaration.parent == null) {
                continue;
            } else if (value != null) {
                storeValue = field.getType().store(value);
            } else if (field.getDefaultValue() != null) {
                storeValue = field.getType().store(field.getDefaultValue());
            } else if (field.isAutoIncrement()) {
                continue;
            } else if (!field.canBeNull()) {
                throw new RuntimeException("Field " + getName() + "." + field.getName() + " cannot be null");
            }

            values.put(key, storeValue);

            List<ShadowField> shades = field.getShadowFields();
            if (shades != null) {
                for (ShadowField shade : shades) {
                    Object shadeValue = shade.generate(value);
                    values.put(shade.getName(), shadeValue);
                }
            }

            if (field.isPrimary()) {
                id = (Long) value;
            }
        }

        if (insert) {
            content.beforeInsert();
            if (id == null) {
                id = performInsert(values);
                primary.set(content, id);
            } else {
                performInsert(values);
            }
            content.afterInsert();
        } else {
            content.beforeUpdate();
            performUpdate(id, values);
            content.afterUpdate();
        }
    }

    @Override
    public void getConnection(UnsafeConsumer<Connection> consumer) throws Exception {
        source.getConnection(consumer);
    }

    @Override
    public void close() {
        source.destruct();
    }

    @Override
    public void drop() throws Exception {
        getConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE " + getName());
            }
        });
    }

    @Override
    public T createInstance() {
        try {
            return represents.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cloneInto(T source, T target) throws Exception {
        if (declaration.parent != null) {
            declaration.parent.cloneInto(source, target);
        }

        source.beforeCloneInto(target);
        target.beforeCloneFrom(source);

        for (Table.Field field : declaration.fields.values()) {
            java.lang.reflect.Field represented = field.getRepresentedField();
            represented.setAccessible(true);
            try {
                represented.set(target, represented.get(source));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        source.afterCloneInto(target);
        target.afterCloneFrom(source);
    }

    @Override
    public List<Index> getIndexes() {
        return new ArrayList<>(declaration.indexes.values());
    }

    @Override
    public String getCreateScript() {
        return declaration.createScript;
    }

    @Override
    public Database getSource() {
        return source;
    }

    @Override
    public void setRepresented(Class<T> jClass) {
        represents = jClass;
    }

    @Override
    public void setSource(Database source) {
        this.source = source;
    }

    protected Long performInsert(HashMap<String, Object> values) throws Exception {
        Set<String> keys = values.keySet();
        List<Object> vals = new ArrayList(values.values());
        List<String> questions = new ArrayList<>();
        for (int i = 0; i < vals.size(); i++) {
            questions.add("?");
        }

        String query = "INSERT INTO `" + getName() + "` ( `" + StringUtils.join(keys, "`, `") + "` ) VALUES ( " +
                StringUtils.join(questions, ", ") + " )";

        final Long[] id = {null};
        getConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < vals.size(); i++) {
                Object val = vals.get(i);
                stmt.setObject(i + 1, val);
            }

            stmt.execute();
            ResultSet gen = stmt.getGeneratedKeys();
            if (gen.next()) {
                id[0] = gen.getLong(1);
            }
        });

        return id[0];
    }

    protected void performUpdate(Long id, HashMap<String, Object> values) throws Exception {
        List<String> updates = new ArrayList<>();
        for (String key : values.keySet()) {
            updates.add(key + " = ?");
        }
        getConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE `" + getName() + "` SET " + StringUtils.join(updates, ", ") + " WHERE id = ?");
            int i = 1;
            for (Object value : values.values()) {
                stmt.setObject(i++, value);
            }

            stmt.setObject(i, id);
            stmt.execute();
        });
    }

    @Override
    public String getDeclaration() {
        return declaration.toString();
    }

    public String getGeneratedDeclaration() {
        return declaration.toString();
    }

    private final class Declaration {
        private HashMap<String, Field> fields = new HashMap<>();
        private HashMap<String, ShadowField> shadowFields = new HashMap<>();
        private HashMap<String, Reference> references = new HashMap<>();
        private Table parent;
        private String prefix;

        private String name;

        private boolean embed;
        private boolean stable;
        private HashMap<String, Index> indexes = new HashMap<>();

        private boolean tableExists;
        private boolean tableMatches;
        private HashMap<String, Field> dbFields = new HashMap<>();
        private HashMap<String, Index> dbIndexes = new HashMap<>();

        private String createScript;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TableImpl.Declaration)) {
                return false;
            }
            Declaration declaration = (Declaration) obj;
            if (!name.equals(declaration.name)) {
                return false;
            }

            if (!fields.equals(declaration.fields)) {
                return false;
            }

            if (!shadowFields.equals(declaration.shadowFields)) {
                return false;
            }

            if (!indexes.equals(declaration.indexes)) {
                return false;
            }

            return true;
        }

        private void readIndices() throws Exception {
            Indexes tableIndexes = represents.getAnnotation(Indexes.class);

            if (tableIndexes != null) {
                for (com.onkiup.jendri.db.annotations.Index indexAnnot : tableIndexes.value()) {
                    IndexImpl index = new IndexImpl(TableImpl.this, indexAnnot);
                    indexes.put(index.getName(), index);
                    if (index.existsInDatabase()) {
                        tableExists = true;
                    } else {
                        tableMatches = false;
                    }
                }
            }

            for (Field field : fields.values()) {
                if (field.isUnique() && !field.isIndexed()) {
                    IndexImpl index = new IndexImpl(TableImpl.this, field);
                    indexes.put(index.getName(), index);
                    if (index.existsInDatabase()) {
                        tableExists = true;
                    } else {
                        tableMatches = false;
                    }
                }

                List<ShadowField> shades = field.getShadowFields();
                if (shades != null) {
                    for (ShadowField shade : shades) {
                        if (shade.isUnique() || shade.isIndexed()) {
                            IndexImpl index = new IndexImpl(TableImpl.this, shade);
                            indexes.put(index.getName(), index);
                            if (index.existsInDatabase()) {
                                tableExists = true;
                            } else {
                                tableMatches = false;
                            }
                        }
                    }
                }
            }
        }

        public Declaration() {
        }

        private void readFields(Class<? extends PersistantObject> represents) throws Exception {
            java.lang.reflect.Field[] classFields = represents.getDeclaredFields();
            tableMatches = true;
            Class superClass = represents.getSuperclass();
            if (superClass != null && PersistantObject.class.isAssignableFrom(superClass)) {
                if (superClass.equals(Record.class)) {
                    readFields(superClass);
                } else {
                    parent = Table.forJavaClass(superClass, source);
                    Table.Field pk = parent.getPrimaryKey();
                    Field f = new FieldImpl(pk.getRepresentedField(), TableImpl.this);
                    fields.put(f.getName(), f);
                    List<ShadowField> shadows = f.getShadowFields();
                    if (f.existsInDatabase()) {
                        tableExists = true;
                    } else {
                        tableMatches = false;
                    }
                    if (shadows != null) {
                        for (ShadowField shadow : shadows) {
                            shadowFields.put(shadow.getName(), shadow);
                            if (shadow.existsInDatabase()) {
                                tableExists = true;
                            } else {
                                tableMatches = false;
                            }
                        }
                    }
                }
            }

            for (java.lang.reflect.Field field : classFields) {
                if (!Modifier.isTransient(field.getModifiers())) {
                    Field f = Field.forJavaField(TableImpl.this, field);
                    if (f.existsInDatabase()) {
                        tableExists = true;
                    } else {
                        tableMatches = false;
                    }
                    fields.put(f.getName(), f);
                    com.onkiup.jendri.db.annotations.Index indexAnnotation = field.getAnnotation(com.onkiup.jendri.db.annotations.Index.class);
                    if (indexAnnotation != null) {
                        IndexImpl index = new IndexImpl(TableImpl.this, f);
                        indexes.put(index.getName(), index);
                    }
                    List<ShadowField> shadows = f.getShadowFields();
                    if (shadows != null) {
                        for (ShadowField shadow : shadows) {
                            if (shadow.existsInDatabase()) {
                                tableExists = true;
                            } else {
                                tableMatches = false;
                            }
                            shadowFields.put(shadow.getName(), shadow);
                            if (shadow.isIndexed()) {
                                IndexImpl index = new IndexImpl(TableImpl.this, shadow);
                                indexes.put(index.getName(), index);
                            }
                        }
                    }
                }
            }
        }

        private void readAnnotations(Class represents) {
            Model.StoreAs storeAs = (Model.StoreAs) represents.getAnnotation(Model.StoreAs.class);
            if (storeAs != null) {
                name = storeAs.value();
            }

            Model.Prefix prefix = (Model.Prefix) represents.getAnnotation(Model.Prefix.class);
            if (prefix != null) {
                this.prefix = prefix.value();
            }

            Model.Embed embed = (Model.Embed) represents.getAnnotation(Model.Embed.class);
            if (embed != null) {
                this.embed = embed.value();
            }

            Model.Stable stable = (Model.Stable) represents.getAnnotation(Model.Stable.class);
            if (stable != null) {
                this.stable = stable.value();
            }
        }

        public Field getField(String id) {
            if (shadowFields.containsKey(id)) {
                return shadowFields.get(id);
            }
            Field f = fields.get(id);
            if (f == null) {
                id = Table.generateNameFromCamelCase(id);
                f = fields.get(id);
            }

            return f;
        }

        public Field getPrimaryKey() {
            for (Field field : fields.values()) {
                if (field.isPrimary()) {
                    return field;
                }
            }

            return null;
        }

        public HashMap<String, Field> getFields() {
            return fields;
        }

        @Override
        public String toString() {
            String declaration = "CREATE TABLE `" + name + "` ";
            List<String> fieldsAndIndexes = fields.values().stream().map(Field::getDeclaration).collect(Collectors.toList());
            fieldsAndIndexes.addAll(shadowFields.values().stream().map(Field::getDeclaration).collect(Collectors.toList()));
            fieldsAndIndexes.addAll(indexes.values().stream().map(Index::getDeclaration).collect(Collectors.toList()));
            fieldsAndIndexes.addAll(references.values().stream().map(Reference::getDeclaration).collect(Collectors.toList()));

            declaration += "(" + StringUtils.join(fieldsAndIndexes, ", ") + ") ";

            return declaration.trim();
        }

        public void populate() throws Exception {
            name = Table.generateNameFromCamelCase(represents.getSimpleName());

            try {
                getConnection(connection -> {
                    String query = "SHOW CREATE TABLE `" + getName() + "`";
                    try (ResultSet result = connection.createStatement().executeQuery(query)) {
                        if (result.next()) {
                            tableExists = true;
                            createScript = result.getString(2);
                        }
                    }
                });
            } catch (SQLException e) {
                tableExists = false;
            }

            readAnnotations(represents);
            readFields(represents);
            readIndices();
            readReferences(represents);
        }

        private void cleanIndices(Connection connection) throws Exception {
            String query = "SHOW KEYS FROM " + name;
            Set<String> toDelete = new HashSet<>();
            try (ResultSet set = connection.createStatement().executeQuery(query)) {
                while (set.next()) {
                    String name = set.getString("Key_name");
                    if (name.startsWith("ref_")) {
                        continue;
                    }
                    if (name.toLowerCase().equals("primary")) {
                        continue;
                    }
                    if (!indexes.containsKey(name)) {
                        toDelete.add(name);
                    }
                }
            }

            for (String idx : toDelete) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE `" + name + "` DROP INDEX `" + idx + "`");
                }
            }
        }

        public void update() throws Exception {
            if (parent != null) {
                parent.update();
            }

            if (tableExists && tableMatches) {
                return;
            }

            if (!tableExists) {
                getConnection(connection -> {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(toString());
                    } catch (SQLException e) {
                        throw new UpdateFailedException(TableImpl.this, toString(), e);
                    }
                });
            } else {
                // update code
                for (Field field : fields.values()) {
                    field.update();
                    List<ShadowField> shadows = field.getShadowFields();
                    if (shadows != null) {
                        for (ShadowField shadow : shadows) {
                            shadow.update();
                        }
                    }
                }

                for (Index index : indexes.values()) {
                    index.update();
                }

                for (Reference reference : references.values()) {
                    reference.update();
                }
            }

            tableExists = true;
            tableMatches = true;

        }

        public void readReferences(Class<? extends PersistantObject> represents) throws Exception {
            Class superClass = represents.getSuperclass();
            if (superClass != null && PersistantObject.class.isAssignableFrom(superClass)) {
                if (!superClass.equals(Record.class)) {
                    Table referenced = Table.forJavaClass(superClass, source);
                    Reference reference = new ReferenceImpl(TableImpl.this, referenced);
                    if (!reference.matchesDatabase()) {
                        tableMatches = false;
                    }
                    references.put(reference.getName(), reference);
                }
            }

            for (Field field : fields.values()) {
                DataType type = field.getType();
                if (type == MySqlType.REFERENCE) {
                    Reference reference = field.getReference();
                    if (!reference.matchesDatabase()) {
                        tableMatches = false;
                    }
                    references.put(reference.getName(), reference);
                }
            }
        }

        public HashMap<String, Index> getIndexes() {
            return indexes;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Table) {
            if (((Table) obj).getRepresentedClass() == getRepresentedClass()) {
                return true;
            }
        }

        return false;
    }
}
