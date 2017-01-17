package com.onkiup.jendri.db;

public class AbstractClone<X extends Storageable> implements Clone<X>, Fetchable {

    private Long id;

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
