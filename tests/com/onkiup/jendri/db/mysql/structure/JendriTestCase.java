package com.onkiup.jendri.db.mysql.structure;

import java.sql.Connection;

import com.mysql.fabric.xmlrpc.base.Data;
import com.onkiup.jendri.config.JendriConfig;
import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.service.ServiceLoader;
import junit.framework.TestCase;
import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public abstract class JendriTestCase extends TestCase {

    protected static Database database;

    static  {
        JendriConfig.setConfigValue("db.database", "com.onkiup.jendri.db.mysql.Driver");
        JendriConfig.setConfigValue("mysql.default", "default");
        JendriConfig.setConfigValue("mysql.default.url", "mysql://localhost:3306/jendri?useSSL=false");
        JendriConfig.setConfigValue("mysql.default.user", "root");
        JendriConfig.setConfigValue("mysql.default.password", "");
        try {
            ServiceLoader.load();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to initialize services", e);
        }
        database = Database.getInstance();
    }

    @Before
    public void assertDbConnected() {
        Assume.assumeNotNull(database);
    }
}
