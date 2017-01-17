package com.onkiup.jendri.db.mysql.structure;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.annotations.AutoIncrement;
import com.onkiup.jendri.db.annotations.DefaultValue;
import com.onkiup.jendri.db.annotations.Name;
import com.onkiup.jendri.db.annotations.FieldNotNull;
import com.onkiup.jendri.db.annotations.PrimaryKey;
import com.onkiup.jendri.db.annotations.Unique;
import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.util.NullUtils;
import com.onkiup.jendri.util.OopUtils;
import org.apache.commons.lang3.StringUtils;

public class FieldImpl implements Table.Field {

    private String name;
    private DataType type;
    private boolean isNotNull;
    private boolean isUnique;
    private boolean isPrimary;
    private boolean isAutoIncrement;
    private boolean isIndexed;
    private Table.Reference reference;
    private Table.ReferenceList referenceList;
    private Table table;

    private boolean foundInDatabase;
    private DataType databaseType;
    private boolean isNotNullInDb;
    private boolean isUniqueInDb;
    private boolean isPrimaryInDb;
    private boolean isAutoIncrementInDb;
    private boolean isIndexedInDb;
    private String defaultValueInDb;

    private Object defaultValue;
    private java.lang.reflect.Field representedField;
    private boolean updateNeeded;

    public FieldImpl() {

    }

    protected FieldImpl(Field representedField, Table table) throws SQLException {
        setRepresentedField(representedField);
        setTable(table);
    }

    public void init() {
        readAnnotations();
        try {
            readDbField();
        } catch (Exception e) {
            // table doesn't exist?
        }
    }

    @Override
    public Object get(Object value) {
        try {
            representedField.setAccessible(true);
            return representedField.get(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object to, Object value) {
        try {
            representedField.setAccessible(true);
            representedField.set(to, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Field getRepresentedField() {
        return representedField;
    }

    @Override
    public void setRepresentedField(Field field) {
        this.representedField = field;
        readAnnotations();
    }

    @Override
    public void setTable(Table owner) {
        table = owner;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public String getDeclaration() {
        List<String> declaration = new ArrayList<String>();

        declaration.add("`" + getName() + "`");

        Class representedType = getRepresentedType();
        if (representedType.equals(table.getRepresentedClass())) {
            declaration.add(table.getPrimaryKey().getType().getDeclaration());
        } else {
            declaration.add(type.getDeclaration(representedField.getType()));
        }

        if (isNotNull) {
            declaration.add("NOT NULL");
        }

        if (defaultValue != null) {
            declaration.add("DEFAULT " + type.store(defaultValue));
        }

        if (isAutoIncrement) {
            declaration.add("AUTO_INCREMENT");
        }

        if (isUnique && !String.class.isAssignableFrom(getRepresentedType())) {
            declaration.add("UNIQUE");
        }

        if (isPrimary) {
            declaration.add("PRIMARY KEY");
        }

        return StringUtils.join(declaration, " ");
    }

    protected Class getRepresentedType() {
        return representedField.getType();
    }

    @Override
    public void update() throws Exception {
        if (!updateNeeded) {
            return;
        }

        table.getConnection(connection -> {
            String update = null;
            if (!foundInDatabase) {
                update = "ALTER TABLE `" + table.getName() + "` ADD COLUMN " + getDeclaration();
            } else {
                update = "ALTER TABLE `" + table.getName() + "` MODIFY COLUMN " + getDeclaration();
            }
            try (Statement statement = ((Connection) connection).createStatement()) {
                statement.execute(update);
                updateNeeded = false;
                foundInDatabase = true;
            } catch (SQLException e) {
                throw new UpdateFailedException(this, update, e);
            }
        });
    }

    @Override
    public boolean existsInDatabase() {
        return foundInDatabase;
    }

    @Override
    public boolean matchesDatabase() {
        return foundInDatabase && !updateNeeded;
    }

    @Override
    public boolean canBeNull() {
        return !isNotNull;
    }

    @Override
    public void setNull(boolean canBeNull) {
        this.isNotNull = !canBeNull;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

    @Override
    public boolean isUnique() {
        return isUnique && !String.class.isAssignableFrom(getRepresentedType());
    }

    @Override
    public void setUnique(boolean unique) {
        this.isUnique = unique;
        if (unique) {
            setIndexed(true);
        }
    }

    @Override
    public boolean isPrimary() {
        return isPrimary;
    }

    @Override
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
        if (primary) {
            setUnique(true);
        }
    }

    @Override
    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    @Override
    public void setAutoIncrement(boolean autoIncrement) {
        this.isAutoIncrement = autoIncrement;
    }

    @Override
    public Table.Reference getReference() {
        return reference;
    }

    @Override
    public Table.ReferenceList getReferenceList() {
        return referenceList;
    }

    @Override
    public void setReference(Table.Reference reference) {
        this.reference = reference;
    }

    @Override
    public boolean isIndexed() {
        return false;
    }

    @Override
    public void setIndexed(boolean is) {

    }

    @Override
    public List<Table.ShadowField> getShadowFields() {
        if (isUnique && String.class.isAssignableFrom(representedField.getType())) {
            List<Table.ShadowField> shadowFields = new ArrayList<>();
            try {
                shadowFields.add(new HashShadow(this));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return shadowFields;
        }
        return null;
    }

    private void readAnnotations() {
        name = Table.generateNameFromCamelCase(representedField.getName());

        Class representedType = representedField.getType();
        type = MySqlType.forType(representedType);
        if (type == MySqlType.REFERENCE) {
//            reference;
            try {
                Table referenced = null;
                if (representedField.getType().equals(table.getRepresentedClass())) {
                    referenced = table;
                } else if (Collection.class.isAssignableFrom(representedField.getType())) {
                    // omg
                    Class collectionType = OopUtils.getBoundClass(representedField.getType(), Collection.class, "E");
                    referenced = Table.forJavaClass(collectionType, table.getSource());
                } else {
                    referenced = Table.forJavaClass((Class<? extends PersistantObject>) representedField.getType(), table.getSource());
                }
                reference = new ReferenceImpl(table, this, referenced);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Name nameAnnotation = representedField.getAnnotation(Name.class);
        if (nameAnnotation != null) {
            name = nameAnnotation.value();
        }

        FieldNotNull fieldNotNullAnnotation = representedField.getAnnotation(FieldNotNull.class);
        if (fieldNotNullAnnotation != null) {
            isNotNull = fieldNotNullAnnotation.value();
        }

        DefaultValue defaultValueAnnotation = representedField.getAnnotation(DefaultValue.class);
        if (defaultValueAnnotation != null) {
            defaultValue = defaultValueAnnotation.value();
        }

        Unique uniqueAnnotation = representedField.getAnnotation(Unique.class);
        if (uniqueAnnotation != null) {
            isUnique = uniqueAnnotation.value();
        }

        PrimaryKey primaryAnnotation = representedField.getAnnotation(PrimaryKey.class);
        if (primaryAnnotation != null) {
            isPrimary = primaryAnnotation.value();
            isUnique = isPrimary;
        }

        AutoIncrement autoIncrementAnnotation = representedField.getAnnotation(AutoIncrement.class);
        if (autoIncrementAnnotation != null) {
            isAutoIncrement = autoIncrementAnnotation.value();
        }

    }

    private void readDbField() throws Exception {
        table.getConnection(connection -> {
            try (Statement statement = ((Connection) connection).createStatement()) {
                try (ResultSet info = statement.executeQuery("SHOW FIELDS FROM `" + table.getName() + "` WHERE Field = '" + getName() + "'")) {
                    if (info.next()) {
                        foundInDatabase = true;
                        updateNeeded = false;
                        String key = info.getString(4);

                        databaseType = MySqlType.forDeclaration(info.getString(2));
                        if (type != MySqlType.REFERENCE) {
                            updateNeeded |= !databaseType.equals(type);
                        } else {
                            List<Table.Field> referencedFields = reference.getFields();
                            updateNeeded |= referencedFields.size() == 1 && !referencedFields.get(0).getType().equals(databaseType);
                        }
                        isNotNullInDb = !info.getString(3).equals("YES");
                        updateNeeded |= isNotNullInDb != isNotNull;
                        isPrimaryInDb = key.equals("PRI");
                        updateNeeded |= isPrimaryInDb != isPrimary;
                        isUniqueInDb = key.equals("UNI") || isPrimaryInDb;
                        updateNeeded |= isUniqueInDb != isUnique;
                        defaultValueInDb = info.getString(5);
                        if (!NullUtils.andNull(defaultValue, defaultValueInDb)) {
                            if (NullUtils.xnorNull(defaultValue, defaultValueInDb)) {
                                updateNeeded |= !defaultValueInDb.equals(type.store(defaultValue));
                            } else {
                                updateNeeded = true;
                            }
                        }
                        isAutoIncrementInDb = info.getString(6).contains("auto_increment");
                        updateNeeded |= isAutoIncrementInDb != isAutoIncrement;

                        info.close();
                    } else {
                        foundInDatabase = false;
                        updateNeeded = true;
                    }
                }
            }
        });
        try (Table owner = table) {
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FieldImpl)) {
            return false;
        }
        FieldImpl field = (FieldImpl) obj;

        return name.equals(field.name)
                && type.equals(field.type)
                && isNotNull == field.isNotNull
                && isUnique == field.isUnique
                && isPrimary == field.isPrimary
                && isAutoIncrement == field.isAutoIncrement
                && isIndexed == field.isIndexed;
    }

    private static class HashShadow extends FieldImpl implements Table.ShadowField<Object> {
        private FieldImpl source;

        public HashShadow(FieldImpl source) throws SQLException {
            super(source.getRepresentedField(), source.getTable());
            init();
            setType(MySqlType.INTEGER);
            this.source = source;
        }

        @Override
        public String getName() {
            return super.getName() + "_shadow";
        }

        @Override
        public String generate(Object value) {
            return String.valueOf(value.hashCode());
        }

        @Override
        public List<Table.ShadowField> getShadowFields() {
            return null;
        }

        @Override
        protected Class getRepresentedType() {
            return Integer.class;
        }
    }
}
