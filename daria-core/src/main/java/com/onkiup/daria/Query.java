package com.onkiup.daria;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Filter;

import com.onkiup.daria.internal.InstancesHolder;

public class Query<T extends Storageable> {

    private StorageTable<T> from;
    private List<ColumnFilter> filters = new ArrayList<>();

    private Query(StorageTable<T> from) {
        this.from = from;
    }

    public static Query from(Class from) {
        return new Query(Daria.getTable(from));
    }

    public Query<T> where(String filter, Object... args) {
//        ColumnFilter columnFilter = new ColumnFilter(from.getColumn(fieldName), filter);
//        filters.add(columnFilter);
        return this;
    }

    public Query<T> and(String filter, Object... args) {
        return where(filter, args);
    }

    static class ColumnFilter {
        private StorageColumn column;
        private Filter filter;

        public ColumnFilter(StorageColumn column, Filter filter) {
            this.column = column;
            this.filter = filter;
        }
    }
}
