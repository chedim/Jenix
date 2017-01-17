package com.onkiup.jendri.db.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.db.QueryResult;
import com.onkiup.jendri.db.Storageable;

public class MysqlQueryResult<X extends Fetchable> extends ArrayList<X> implements QueryResult<X> {
    Integer skip, limit;
    QueryBuilder<X> creator;

    public MysqlQueryResult(QueryBuilder creator, int skip, int limit) {
        super(limit);
        this.skip = skip;
        this.limit = limit;
        this.creator = creator;
    }

    public MysqlQueryResult(QueryBuilder creator, int skip) {
        super();
        this.skip = skip;
        this.creator = creator;
    }

    public MysqlQueryResult(QueryBuilder creator, int skip, Collection<? extends X> c) {
        super(c);
        this.skip = skip;
        this.creator = creator;
    }

    @Override
    public QueryBuilder<X> getCreator() {
        return creator;
    }

    @Override
    public QueryResult<X> next() throws Exception {
        return creator.next();
    }

    @Override
    public QueryResult<X> prev() throws Exception {
        return creator.prev();
    }

    @Override
    public void saveAll(Consumer<X> trigger) throws Exception {
        for (X item : this) {
            if (trigger != null) {
                trigger.accept(item);
            }
            creator.getSource().saveImmediately((Storageable) item);
        }
    }
}
