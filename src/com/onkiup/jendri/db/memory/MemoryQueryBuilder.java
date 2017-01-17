package com.onkiup.jendri.db.memory;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.Order;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.QueryResult;

public class MemoryQueryBuilder implements QueryBuilder<MemoryFetchable> {
    @Override
    public FilterGroup getCurrentGroup() {
        return null;
    }

    @Override
    public FilterGroup getMainGroup() {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> join(Class<? extends Fetchable> collection) {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> setPosition(int skip, int buffer) {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> withBuffer(int limit) {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> order(String field) {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> order(String field, Order order) {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> open() {
        return null;
    }

    @Override
    public Database getSource() {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> close() {
        return null;
    }

    @Override
    public Class<MemoryFetchable> getType() {
        return null;
    }

    @Override
    public Integer getBufferSize() {
        return null;
    }

    @Override
    public Integer getSkip() {
        return null;
    }

    @Override
    public QueryBuilder<MemoryFetchable> addFilter(FilterJoiner joiner, String filterString, Object... params) {
        return null;
    }

    @Override
    public QueryResult<MemoryFetchable> createResult() {
        return null;
    }

    @Override
    public boolean hasMoreThan(int amount) {
        return false;
    }

}
