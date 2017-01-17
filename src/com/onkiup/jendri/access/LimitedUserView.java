package com.onkiup.jendri.access;

import com.onkiup.jendri.db.Fetchable;

@Fetchable.Mask(User.class)
public class LimitedUserView implements Fetchable {

    @Override
    public void setId(Long id) {

    }

    @Override
    public Long getId() {
        return null;
    }
}
