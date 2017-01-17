package com.onkiup.jendri.managment;

import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.managment.password.Md5Policy;
import com.onkiup.jendri.managment.password.PasswordPolicy;

public class User extends Record {
    protected String login;
    protected String password;

    @Inject(defaultType = Md5Policy.class)
    private static PasswordPolicy passwordPolicy;

    public User() {
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
