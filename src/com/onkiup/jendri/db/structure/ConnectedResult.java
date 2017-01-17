package com.onkiup.jendri.db.structure;

import java.sql.Connection;
import java.sql.ResultSet;

import com.onkiup.jendri.db.Database;

public class ConnectedResult {
    private Database connection;
    private ResultSet result;

    public ConnectedResult(Database connection, ResultSet result) {
        this.connection = connection;
        this.result = result;
    }

    public Database getDatabase() throws Exception {
        return connection;
    }

    public ResultSet getResult() {
        return result;
    }
}
