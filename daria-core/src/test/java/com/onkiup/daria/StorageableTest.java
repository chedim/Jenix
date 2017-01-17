package com.onkiup.daria;

import junit.framework.TestCase;

public class StorageableTest extends TestCase {
    public static class TestStorageable implements Storageable {

        @Override
        public <T> T as(Class<T> type) {
            return null;
        }

        @Override
        public void save() {
            System.out.println("Save: " + this);
        }

        @Override
        public void saveEventually() {

        }

        protected void finalize() throws Throwable {
            System.out.println("Finalize: " + this);
            save();
            super.finalize();
        }
    }

    public void testFinalize() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            createStorageable();
            Thread.sleep(100);
            System.gc();
        }
    }

    private void createStorageable() {
        new TestStorageable();
    }
}