package com.onkiup.jendri.db.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;

import com.onkiup.jendri.db.Database;
import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.Order;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.QueryResult;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.mysql.structure.MySqlType;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.injection.Inject;

public class MySqlQueryBuilder<X extends Fetchable> implements QueryBuilder<X> {

    @Inject("10")
    private static Integer defaultBufferSize;

    private int skip, limit = defaultBufferSize;

    private Map<String, Order> order = new HashMap<>();
    private Stack<QueryBuilder.FilterGroup> filtersTree = new Stack<>();
    private QueryBuilder.FilterGroup mainFilterGroup = new FilterGroup();
    private QueryBuilder.FilterGroup currentFilterGroup = mainFilterGroup;

    private Class xClass;
    private Table from;

    public MySqlQueryBuilder(Class xClass, Database database) {
        this.xClass = xClass;
        try {
            from = Table.forJavaClass(xClass, database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryBuilder.FilterGroup getCurrentGroup() {
        return currentFilterGroup;
    }

    @Override
    public QueryBuilder.FilterGroup getMainGroup() {
        return mainFilterGroup;
    }

    @Override
    public QueryBuilder<X> join(Class<? extends Fetchable> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryBuilder<X> setPosition(int skip, int buffer) {
        this.skip = skip;
        this.limit = buffer;
        return this;
    }

    @Override
    public QueryBuilder<X> withBuffer(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public QueryBuilder<X> order(String field) {
        order.put(field, Order.ASC);
        return this;
    }

    @Override
    public QueryBuilder<X> order(String field, Order order) {
        this.order.put(field, order);
        return this;
    }

    @Override
    public QueryBuilder<X> open() {
        filtersTree.push(currentFilterGroup);
        QueryBuilder.FilterGroup newGroup = new FilterGroup();
        currentFilterGroup.add(newGroup);
        currentFilterGroup = newGroup;
        return this;
    }

    @Override
    public Database getSource() {
        return from.getSource();
    }

    @Override
    public QueryBuilder<X> close() {
        currentFilterGroup = filtersTree.pop();
        return this;
    }

    @Override
    public Class<X> getType() {
        return xClass;
    }

    @Override
    public Integer getBufferSize() {
        return limit;
    }

    @Override
    public Integer getSkip() {
        return skip;
    }

    @Override
    public QueryBuilder<X> addFilter(FilterJoiner joiner, String filterString, Object... params) {
        QueryBuilder.Filter filter = new Filter(filterString, params);
        filter.setJoiner(joiner);
        currentFilterGroup.add(filter);
        return this;
    }

    @Override
    public QueryResult<X> createResult() {
        return new MysqlQueryResult<X>(this, skip, limit);
    }

    private static class Filter implements QueryBuilder.Filter {
        private String filter;
        private Object[] params;
        private FilterJoiner joiner;
        private boolean negative;

        public Filter(String filter, Object... params) {
            this.filter = filter;
            this.params = params;
        }

        @Override
        public void setJoiner(FilterJoiner joiner) {
            this.joiner = joiner;
        }

        @Override
        public void negative() {
            negative = true;
        }

        @Override
        public Object[] getParams() {
            return params;
        }

        @Override
        public String getFilter() {
            return filter;
        }

        @Override
        public FilterJoiner getJoiner() {
            return joiner;
        }

        @Override
        public Boolean isNegative() {
            return negative;
        }
    }

    private static class FilterGroup implements QueryBuilder.FilterGroup {
        private List<QueryBuilder.FilterItem> filters = new ArrayList<>();
        private boolean negative;
        private FilterJoiner joiner;

        @Override
        public List<QueryBuilder.FilterItem> getGroupedFilters() {
            return filters;
        }

        @Override
        public void add(QueryBuilder.FilterItem filter) {
            filters.add(filter);
        }

        @Override
        public void remove(QueryBuilder.FilterItem filter) {
            filters.remove(filter);
        }

        @Override
        public void setJoiner(FilterJoiner joiner) {
            this.joiner = joiner;
        }

        @Override
        public FilterJoiner getJoiner() {
            return joiner;
        }

        @Override
        public Boolean isNegative() {
            return negative;
        }

        @Override
        public void negative() {
            this.negative = true;
        }
    }
}
