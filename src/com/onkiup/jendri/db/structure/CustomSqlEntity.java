package com.onkiup.jendri.db.structure;

public interface CustomSqlEntity<T> extends SqlEntity<T> {
    public String getDeclaration(T represented);
}
