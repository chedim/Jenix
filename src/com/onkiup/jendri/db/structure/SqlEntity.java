package com.onkiup.jendri.db.structure;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;

public interface SqlEntity<T> {
    public String getName();
    public String getDeclaration();
    public void update() throws Exception;
    public boolean existsInDatabase();
    public boolean matchesDatabase();
}
