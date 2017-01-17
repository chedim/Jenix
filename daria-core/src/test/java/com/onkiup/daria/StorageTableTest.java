package com.onkiup.daria;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import com.onkiup.daria.annotations.StoreIn;
import com.onkiup.daria.query.language.daria1.Daria1Lexem;
import junit.framework.TestCase;

public class StorageTableTest extends TestCase {
    @StoreIn(StorageImpl.class)
    private static class A {
        private int id;
    }

    private static class B extends A {
        private String label;
    }

    public static class StorageImpl implements Storage {

        @Override
        public Object getIdentifier(Object o) {
            return null;
        }

        @Override
        public <T extends Storageable> T getOriginal(T o) {
            return null;
        }

        @Override
        public <T extends Storageable> T fetch(Class<T> from, Object identifier) {
            return null;
        }

        @Override
        public <T extends Storageable> Stream<T> query(Class<T> from, Daria1Lexem filter, long skip, long limit) {
            return Stream.empty();
        }

        @Override
        public void store(Storageable o) {

        }

        @Override
        public <T extends Storageable> T instantiate(Class<T> type) {
            return null;
        }

        @Override
        public StorageTable getTable(Class type) {
            return null;
        }

        @Override
        public StorageColumn getColumn(StorageTable table, Field field) {
            return new AbstractStorageColumn(table, field) {
                @Override
                public List<StorageOperation> getSchemaUpdateOperations() {
                    return null;
                }
            };
        }

        @Override
        public StorageOperationResult execute(StorageOperation operation) {
            return null;
        }

        @Override
        public <T> StorageType<T, ?> getType(Class<T> type) {
            return null;
        }
    }

    private static class TableImpl<X extends Storageable> extends AbstractStorageTable<X> {

        public TableImpl(Class<X> javaClass) {
            super(javaClass);
        }

        @Override
        public List<StorageOperation> getSchemaUpdateOperations() {
            return null;
        }

        @Override
        public StorageIndex getIndex(String indexName) {
            return null;
        }

        @Override
        public List<StorageOperation> getStoreOperations(X object) {
            return null;
        }

        @Override
        public List<StorageOperation> getDeleteOperations(X object) {
            return null;
        }
    }

    public void testA() {
        StorageTable table = new TableImpl(A.class);
        assertEquals(A.class, table.getJavaClass());
        assertNotNull(table.getColumn("id"));
        Exception thrown = null;
        try {
            table.getColumn(null);
        } catch (Exception e) {
            thrown = e;
        } finally {
            assertNotNull(thrown);
        }
    }
}