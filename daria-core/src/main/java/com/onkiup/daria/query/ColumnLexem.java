package com.onkiup.daria.query;

import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.parser.AbstractLexem;
import com.onkiup.daria.query.language.daria1.Daria1Lexem;

public class ColumnLexem extends AbstractLexem implements Daria1Lexem {

    private StorageColumn column;

    public ColumnLexem(StorageColumn column) {
        super();
        this.column = column;
    }

    public StorageColumn getColumn() {
        return column;
    }

}
