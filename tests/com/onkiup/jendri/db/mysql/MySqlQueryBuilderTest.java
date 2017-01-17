package com.onkiup.jendri.db.mysql;

import java.util.UUID;

import com.onkiup.jendri.db.Modification;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.mysql.structure.JendriTestCase;
import org.junit.Test;

public class MySqlQueryBuilderTest extends JendriTestCase {
    public static class QueryTestModel extends Record {
        protected String text;
    }

    @Test
    public void testQuery() throws Exception {
        QueryTestModel obj = new QueryTestModel();
        obj.text = UUID.randomUUID().toString();

        obj.saveImmediately();

        Query<QueryTestModel> query = Query.from(QueryTestModel.class).where("text = ?", obj.text).withBuffer(10);
        long count = query.count();
        assertEquals(1, count);
        QueryTestModel loaded = query.fetchOne();
        assertEquals(obj, loaded);
        assertFalse(loaded == obj);
        assertEquals(obj.text, loaded.text);
        loaded.saveImmediately();

        loaded = Query.from(QueryTestModel.class).id(obj.getId());
        assertEquals(obj, loaded);
        assertFalse(loaded == obj);
        assertEquals(obj.text, loaded.text);
    }

    public static class QueryModification implements Modification<QueryTestModel> {
        public Integer test;
    }

    @Test
    public void testExtension() {
        QueryModification mod = new QueryModification();
        mod.test = 15;
    }
}