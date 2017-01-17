package com.onkiup.jendri.db.mysql.structure;

import java.util.List;

import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.Table;

public class ReferenceListImpl implements Table.ReferenceList {
    @Override
    public Table getReferenceTable() {
        Table referenceTable = new TableImpl<>();
        return null;
    }

    @Override
    public Table getTarget() {
        return null;
    }

    @Override
    public Table getSource() {
        return null;
    }

    @Override
    public List<Table.Field> getFields() {
        return null;
    }

    @Override
    public void setHard(boolean b) {

    }

    @Override
    public boolean isHard() {
        return false;
    }

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
}
