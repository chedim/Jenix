package com.onkiup.jendri.db.sqlite.structure;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.mysql.exceptions.ItemNotFound;
import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.ConnectedResult;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.util.UnsafeConsumer;

public class SqliteTable<X extends PersistantObject> implements Table<X> {



    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDeclaration() {
        return null;
    }

    @Override
    public void update() throws UpdateFailedException {

    }

    @Override
    public boolean existsInDatabase() {
        return false;
    }

    @Override
    public boolean matchesDatabase() {
        return false;
    }

    @Override
    public Field getField(String id) {
        return null;
    }

    @Override
    public Field getPrimaryKey() {
        return null;
    }

    @Override
    public void populateObject(Object primaryKey, X instance) throws SQLException, ItemNotFound {

    }

    @Override
    public List<String> getAllTableNames() {
        return null;
    }

    @Override
    public void fromResultSet(ConnectedResult result, X instance) throws ItemNotFound {

    }

    @Override
    public Class<X> getRepresentedClass() {
        return null;
    }

    @Override
    public void save(X content) throws SQLException {

    }

    @Override
    public void getConnection(UnsafeConsumer<Connection> consumer) throws Exception {
        getSource().getConnection(consumer);
    }

    @Override
    public void close() {
        getSource().destruct();
    }

    @Override
    public void drop() throws SQLException {

    }

    @Override
    public X createInstance() {
        return null;
    }

    @Override
    public void cloneInto(X source, X target) {

    }

    @Override
    public List<Index> getIndexes() {
        return null;
    }

    @Override
    public String getCreateScript() {
        return null;
    }

    @Override
    public Database getSource() {
        return null;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void setRepresented(Class<X> jClass) {

    }

    @Override
    public void setSource(Database source) {

    }
}
