package com.onkiup.jendri.db.mysql.structure;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.annotations.Index;
import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.Table;
import org.apache.commons.lang3.StringUtils;

public class IndexImpl<Owner extends PersistantObject> implements Table.Index<Owner> {

    private Table<Owner> owner;

    private Index annotation;

    private Table.Field annotated;

    private boolean needsUpdate;

    private String name;
    // index parameters
    private Set<Type> types = new HashSet<>();
    private List<IndexColumn> columns = new ArrayList<>();

    // stored parameters
    private boolean foundInDb;
    private Set<Type> dbTypes = new HashSet<>();
    private List<IndexColumn> dbColumns = new ArrayList<>();

    private List<IndexColumn> columnsToUpdate = new ArrayList<>();
    private List<Type> typesToUpdate = new ArrayList<>();

    public IndexImpl(Table<Owner> owner, Index annotation) throws SQLException {
        this.owner = owner;
        this.annotation = annotation;
        readAnnotation();
        try {
            readDbIndex();
        } catch (Exception e) {
            // table doesn't exist?
        }
    }

    public IndexImpl(Table<Owner> owner, Table.Field annotatedField) throws Exception {
        this.owner = owner;
        this.annotated = annotatedField;
        this.annotation = annotatedField.getRepresentedField().getAnnotation(Index.class);
        if (this.annotation != null) {
            readAnnotation();
        } else {
            IndexColumn column = new IndexColumn(annotatedField, null, true);
            if (annotatedField.isUnique()) {
                types.add(Type.UNIQUE);
            }
            name = "idx_" + annotatedField.getName();
            columns.add(column);
        }
        readDbIndex();
    }

    @Override
    public String getDeclaration() {
        return generateDeclaration(false);
    }

    public String generateDeclaration(boolean update) {
        String declaration = (update ? "CREATE " : "");
        String typeDeclaration = null;
        String indexingTypeDeclaration = null;
        for (Type type : types) {
            if (type == Type.UNIQUE || type == Type.FULLTEXT || type == Type.SPATIAL) {
                typeDeclaration = type.toString();
            } else if (type == Type.BTREE || type == Type.HASH || type == Type.RTREE) {
                indexingTypeDeclaration = type.toString();
            }
        }
        if (typeDeclaration != null) {
            declaration += typeDeclaration + " ";
        }
        declaration += "INDEX " + name + (update ? " ON `" + owner.getName() + "` " : "");
        declaration += "(" + StringUtils.join(columns, ", ") + ")";

        if (indexingTypeDeclaration != null) {
            declaration += " " + indexingTypeDeclaration;
        }

        return declaration;
    }

    @Override
    public void update() throws UpdateFailedException {
        if (!needsUpdate) return;
        final String[] lastQuery = new String[1];
        try {
            owner.getConnection(connection -> {
                try (Statement st = connection.createStatement()){
                    if (foundInDb) {
                        lastQuery[0] = "ALTER TABLE " + owner.getName() + " DROP KEY " + name;
                        st.execute(lastQuery[0]);
                    }
                    lastQuery[0] = generateDeclaration(true);
                    st.execute(lastQuery[0]);
                    needsUpdate = false;
                    foundInDb = true;
                }
            });
        } catch (Exception e) {
            throw new UpdateFailedException(this, lastQuery[0], e);
        }
    }

    @Override
    public boolean existsInDatabase() {
        return foundInDb;
    }

    @Override
    public boolean matchesDatabase() {
        return foundInDb && !needsUpdate;
    }

    private void readAnnotation() {
        name = annotation.name();

        String[] fields = annotation.value();
        if (fields == null) {
            fields = new String[]{annotated.getName()};
        }

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            IndexColumn column = new IndexColumn(owner.getField(field), null, true);
            if (column.field.isUnique()) {
                types.add(Type.UNIQUE);
            }
            columns.add(column);
            fields[i] = Table.generateNameFromCamelCase(field);
        }

        if (name == null || name.length() == 0) {
            name = "idx_" + StringUtils.join(fields, "_");
        }
    }

    private void readDbIndex() throws Exception {
        owner.getConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet info = statement.executeQuery("SHOW INDEX FROM `" + owner.getName() + "` WHERE Key_name = '" + name + "'")) {
                    dbColumns = new ArrayList<>();
                    dbTypes = new HashSet<>();
                    while (info.next()) {
                        foundInDb = true;

                        if (info.getInt("non_unique") == 0) {
                            dbTypes.add(Type.UNIQUE);
                        }
                        Table.Field field = owner.getField(info.getString("column_name"));
                        boolean sorted = info.getString("collation") != null;
                        Integer length = info.getInt("sub_part");
                        Type type = Enum.valueOf(Type.class, info.getString("index_type"));
                        if (type != Type.BTREE) {
                            dbTypes.add(type);
                        }
                        dbColumns.add(new IndexColumn(field, length, sorted));
                    }

                    columnsToUpdate = new ArrayList<>();
                    for (IndexColumn column : columns) {
                        if (!dbColumns.contains(column)) {
                            columnsToUpdate.add(column);
                        }
                    }

                    typesToUpdate = new ArrayList<>();
                    for (Type type : types) {
                        if (!dbTypes.contains(type)) {
                            typesToUpdate.add(type);
                        }
                    }

                    needsUpdate = !(columnsToUpdate.size() == 0 && typesToUpdate.size() == 0);
                }
            } catch (SQLException e) {
                foundInDb = false;
                needsUpdate = true;
                columnsToUpdate = new ArrayList<IndexColumn>(columns);
                typesToUpdate = new ArrayList<Type>(types);
            }
        });
    }

    public String getName() {
        return name;
    }

    private static class IndexColumn {
        Table.Field field;
        Integer length;
        boolean sorted;

        public IndexColumn(Table.Field field, Integer length, boolean sorted) {
            this.field = field;
            this.length = length;
            this.sorted = sorted;
            if (field.getType() == MySqlType.TEXT && length == null) {
                this.length = 64;
            }
        }

        @Override
        public String toString() {
            String result = field.getName();
            if (length != null) {
                result += " (" + length + ")";
            }
            if (sorted) {
                result += " ASC";
            }

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof IndexColumn)) {
                return false;
            } else {
                IndexColumn other = (IndexColumn) obj;
                if (!other.field.equals(((IndexColumn) obj).field)) {
                    return false;
                } else if (length == null && other.length != null) {
                    return false;
                } else if (length != null && other.length == null) {
                    return false;
                } else if (length != null && length != other.length) {
                    return false;
                } else {
                    return sorted == other.sorted;
                }
            }
        }
    }
}
