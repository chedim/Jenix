package com.onkiup.jendri.db.mysql.structure;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.annotations.Index;
import com.onkiup.jendri.db.annotations.Indexes;
import com.onkiup.jendri.db.structure.Table;
import org.junit.AfterClass;
import org.junit.Test;

public class ASuperDatabaseTest extends JendriTestCase {

    private static Logger log = Logger.getLogger(ASuperDatabaseTest.class.getName());

    @Indexes({
            @Index({"aShort"})
    })
    public static class TestModel extends Record {
        protected Short aShort;
        protected TestModel test;
        protected String text;
    }

    public static class TestModelModification extends TestModel {
        protected Date date;
    }

    @Test
    public void testGetTypeClass() throws Exception {
        assertEquals(MySqlType.TEXT, MySqlType.forType(String.class));
    }

    @Test
    public void testNameConversion() {
        String lodash = Table.generateNameFromCamelCase(getClass().getSimpleName());
        log.info("lodash: " + lodash);
        assertEquals(getClass().getSimpleName(), lodash);
        String restored = Table.generateNameFromLoDash(lodash, true);
        log.info("restored: " + restored);
        assertEquals(getClass().getSimpleName(), restored);
    }

    @Test
    public void testGetDeclaration() throws Exception {
        Table me = Table.forJavaClass(TestModel.class, database);
        Table.Field field = me.getField("id");

        assertEquals("`id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE PRIMARY KEY", field.getDeclaration());

        field = me.getField("a_short");
        assertEquals("a_short SMALLINT", field.getDeclaration());

        field = me.getField("test");
        assertEquals("test BIGINT", field.getDeclaration());

        assertEquals("CREATE TABLE `test_model` (a_short SMALLINT, " +
                "test BIGINT, id BIGINT NOT NULL AUTO_INCREMENT UNIQUE PRIMARY KEY, text TEXT, INDEX idx_a_short(a_short ASC), " +
                "CONSTRAINT `ref____test_model_test____to____test_model_id` FOREIGN KEY (`test`) REFERENCES `test_model` (`id`) ON DELETE SET NULL ON UPDATE CASCADE)",
                me.getDeclaration());
    }

    @Test
    public void testModifications() throws Exception {
        TestModelModification modification = new TestModelModification();
        modification.text = UUID.randomUUID().toString();
        modification.date = new Date();
        Database.getInstance().saveImmediately(modification);
        assertNotNull(modification.getId());

        TestModelModification loaded = Database.getInstance().get(TestModelModification.class, modification.getId());
        assertNotNull(loaded);
        assertFalse(loaded == modification);
        assertEquals(modification.date.getTime(), loaded.date.getTime());
        assertEquals(modification.text, loaded.text);

        TestModel base = Database.getInstance().get(TestModel.class, modification.getId());
        assertNotNull(base);
        assertFalse(base == loaded);
        assertTrue(modification.equals(base));
        assertEquals(modification.hashCode(), base.hashCode());
        assertEquals(modification.text, base.text);

        TestModelModification as = base.as(TestModelModification.class);
        assertNotNull(as);
        assertEquals(modification.getId(), as.getId());
        assertEquals(modification.date.getTime(), as.date.getTime());

        base.deleteImmediately();
        loaded = Database.getInstance().get(TestModelModification.class, base.getId());
        assertNull(loaded);
    }


    @Test
    public void testStoring() throws Exception {
        TestModel testModel = new TestModel();
        testModel.aShort = 10;
        testModel.text = UUID.randomUUID().toString();
        Database.getInstance().saveImmediately(testModel);

        TestModel testModel1 = new TestModel();
        testModel1.test = testModel;
        Database.getInstance().saveImmediately(testModel1);

        TestModel loaded = Database.getInstance().get(TestModel.class, testModel.getId());
        assertFalse(loaded == testModel);
        assertEquals(testModel.aShort, loaded.aShort);
        assertEquals(testModel.text, loaded.text);
        assertEquals(testModel, loaded);
        assertEquals(testModel.hashCode(), loaded.hashCode());

        loaded = Database.getInstance().get(TestModel.class, testModel1.getId());
        assertFalse(testModel1 == loaded);
        assertEquals(testModel1, loaded);
        assertEquals(testModel, loaded.test);
        assertEquals(testModel.text, loaded.test.text);
        loaded.delete();
        testModel.delete();
    }

    private interface TEST<T> {

    }

    private static class TEST_TEST implements TEST<List> {

    }

    @Test
    public void testGetTypeParameter() {
        List<Class> params = Table.getTypeClasses(TEST_TEST.class);
        assertEquals(1, params.size());
        assertEquals(List.class, params.get(0));
    }

    @AfterClass
    public static void dropTable() throws Exception {
        Table me = Table.forJavaClass(TestModelModification.class, Database.getInstance());
        me.drop();
        me = Table.forJavaClass(TestModel.class, Database.getInstance());
        me.drop();
        assertTrue(true);
    }
}