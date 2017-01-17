package com.onkiup.jendri.access;

import com.onkiup.jendri.db.Column;
import com.onkiup.jendri.db.Record;

public class UserAbility extends Record {
    @Column.Indexed
    private String value;

    public UserAbility() {
    }

    public UserAbility(User user, String ability) throws Exception {
        setOwner(user);
        this.value = ability;
    }

    public String getAbility() {
        return value;
    }

    public void setAbility(String ability) {
        this.value = ability;
    }
}
