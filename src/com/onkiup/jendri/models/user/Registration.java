package com.onkiup.jendri.models.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.onkiup.jendri.access.Ability;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.Unique;

@Ability.Owned
@Ability.Read(Ability.ADMIN)
public class Registration extends Record {
    private Date date;
    @Unique
    private String identifier;

    public Registration() {
    }

    @Override
    public boolean willBeCreated() throws Exception {
        User user = new User(identifier, Arrays.asList("user"));
        user.saveImmediately();
        return true;
    }
}
