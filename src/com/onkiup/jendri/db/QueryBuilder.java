package com.onkiup.jendri.db;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.onkiup.jendri.bus.Data;
import com.onkiup.jendri.db.structure.DataType;
import com.onkiup.jendri.db.structure.Table;

/**
 * Example:
 * Query.from(Class.class)
 * .where("id = ?", 1).fetch();
 */
public interface QueryBuilder<X extends Fetchable> extends Query<X> {

    public FilterGroup getCurrentGroup();

    public FilterGroup getMainGroup();

    public QueryBuilder<X> join(Class<? extends Fetchable> collection);

    public QueryBuilder<X> setPosition(int skip, int buffer);

    public QueryBuilder<X> withBuffer(int limit);

    public QueryBuilder<X> order(String field);

    public QueryBuilder<X> order(String field, Order order);

    public QueryBuilder<X> open();

    public default QueryBuilder<X> openNot() {
        open();
        getCurrentGroup().negative();
        return this;
    }

    public Database getSource();

    public QueryBuilder<X> close();

    public default QueryBuilder<X> where(String filter, Object... args) {
        addFilter(FilterJoiner.AND, filter, args);
        return this;
    }

    public Class<X> getType();

    /**
     * Adds where-clause with AND in front of it
     *
     * @param filter
     * @param args
     * @return this
     */
    public default QueryBuilder<X> and(String filter, Object... args) {
        addFilter(FilterJoiner.AND, filter, args);
        return this;
    }

    /**
     * Adds where-clause with OR in front of it
     *
     * @param filter
     * @param args
     * @return
     */
    public default QueryBuilder<X> or(String filter, Object... args) {
        addFilter(FilterJoiner.OR, filter, args);
        return this;
    }

    /**
     * Closes previous parenthesis and opens new with AND joiner
     *
     * @return this
     */
    public default QueryBuilder<X> and() {
        close();
        open();
        getCurrentGroup().setJoiner(FilterJoiner.AND);
        return this;
    }

    /**
     * Closes previous parenthesis and opens new with OR joiner
     *
     * @return this
     */
    public default QueryBuilder<X> or() {
        close();
        open();
        getCurrentGroup().setJoiner(FilterJoiner.OR);
        return this;
    }

    default void matches(Fetchable item) {
        Long id = item.getId();
        this.and("id = ?", id);
    }

    public default String build() throws Exception {
        Table from = Table.forJavaClass(getType());
        String where = getMainGroup().getStatement();
        String statement = "SELECT * FROM " + from.getName() + " WHERE " + where;
        statement += " LIMIT " + getSkip();
        statement += ", " + getBufferSize();

        return statement;
    }

    Integer getBufferSize();

    Integer getSkip();

    public default QueryResult<X> fetch() throws Exception {
        return Database.getInstance().get(this);
    }

    public default X fetchOne() {
        Integer oldLimit = getBufferSize();
        withBuffer(1);
        X result = stream().findFirst().orElse(null);
        withBuffer(oldLimit);
        return result;
    }

    public default X id(Object id) throws Exception {
        Table from = Table.forJavaClass(getType());
        Table.Field pk = from.getPrimaryKey();
        addFilter(FilterJoiner.AND, pk.getName() + " = ?", id);
        return fetchOne();
    }

    public QueryBuilder<X> addFilter(FilterJoiner joiner, String filterString, Object... params);

    public default QueryResult<X> next() throws Exception {
        setPosition(getSkip() + getBufferSize(), getBufferSize());
        return fetch();
    }

    public default QueryResult<X> prev() throws Exception {
        setPosition(getSkip() - getBufferSize(), getBufferSize());
        return fetch();
    }

    QueryResult<X> createResult();

    default boolean hasMoreThan(int amount) {
        Integer oldLimit = getBufferSize();
        withBuffer(amount + 1);
        try {
            long count = Database.getInstance().count(this);
            return count > amount;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            withBuffer(oldLimit);
        }
    }

    default long count() {
        try {
            return Database.getInstance().count(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum FilterJoiner {
        AND, OR
    }

    public static interface FilterItem {
        public String getStatement();

        public FilterJoiner getJoiner();

        public Boolean isNegative();

        public void setJoiner(FilterJoiner joiner);

        public void negative();
    }

    public static interface Filter extends FilterItem {
        public Object[] getParams();

        public String getFilter();

        @Override
        public default String getStatement() {
            Object[] params = getParams();
            String filter = getFilter();
            if (params.length == 0) {
                return filter;
            }

            String[] parts = filter.split("\\?");
            String result = "";
            int paramNum = 0;
            for (String part : parts) {
                int partLen = part.length();
                if (partLen == 0 || part.substring(partLen - 1) != "\\") {
                    Object param = params[paramNum++];
                    if (param != null) {
                        DataType type = DataType.forType(param.getClass());
                        Object stored = type.store(param);
                        String val = (stored instanceof String ? "'" + stored + "'" : stored.toString());
                        result += part + val;
                    } else {
                        result += part + "NULL";
                    }
                }
            }
            return result;
        }

    }

    public static interface FilterGroup extends FilterItem {
        public List<FilterItem> getGroupedFilters();

        public void add(FilterItem filter);

        public void remove(FilterItem filter);

        @Override
        public default String getStatement() {
            List<FilterItem> filters = getGroupedFilters();

            if (filters == null || filters.size() == 0) {
                return "(TRUE)";
            }

            StringBuilder where = new StringBuilder("( TRUE ");

            for (QueryBuilder.FilterItem filter : filters) {
                String filterStatement = filter.getStatement();
                FilterJoiner joiner = filter.getJoiner();
                if (filter.isNegative()) {
                    filterStatement = "!( " + filterStatement + " )";
                }
                where.append(" ");
                if (joiner != null) {
                    where.append(joiner.toString() + " ");
                }
                where.append(filterStatement);
            }

            where.append(" )");

            return where.toString();
        }
    }

    /**
     * Creates a stream of ALL items (not just buffered, but all items in db) that match
     * query filters
     * Use {@link Stream#limit(long)} to specify the amount of selected items
     * @return
     */
    @Override
    default Stream<X> stream() {
        try {
            Iterator<X> it = new ResultIterator<X>(fetch());
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void delete() {
        try {
            Database.getInstance().deleteImmediately(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    class ResultIterator<X extends Fetchable> implements Iterator<X> {
        int position;
        QueryResult<X> page;

        public ResultIterator(QueryResult<X> page) {
            this.page = page;
        }

        @Override
        public boolean hasNext() {
            if (position < page.size()) {
                return true;
            } else {
                try {
                    page = page.next();
                    position = 0;
                    return page.size() > 0;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public X next() {
            return page.get(position++);
        }
    }
}
