package com.onkiup.daria;

import com.onkiup.daria.parser.Lexem;

public abstract class QueryOperation implements StorageOperation {

    private Lexem operator;

    public QueryOperation(Lexem operator) {
        this.operator = operator;
    }

    public Lexem getOperator() {
        return operator;
    }

    @Override
    public boolean hasResults() {
        return true;
    }
}
