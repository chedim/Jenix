package com.onkiup.jendri.db.mysql.exceptions;

import com.onkiup.jendri.db.structure.SqlEntity;

public class UpdateFailedException extends com.onkiup.jendri.db.exceptions.UpdateFailedException {
    private SqlEntity failedItem;
    private String failedQuery;

    public UpdateFailedException(SqlEntity failedItem, String failedQuery, Throwable reason) {
        super("Failed to update "+failedItem.getClass().getSimpleName() + "[" + failedItem.getName()+ "]; Query: \n\n"+failedQuery+"\n\n Reason: ", reason);
        this.failedItem = failedItem;
        this.failedQuery = failedQuery;
    }

    public SqlEntity getFailedItem() {
        return failedItem;
    }

    public String getFailedQuery() {
        return failedQuery;
    }
}
