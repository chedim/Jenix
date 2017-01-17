package com.onkiup.jendri.models.user;

import java.util.Date;

import com.onkiup.jendri.access.Ability;
import com.onkiup.jendri.access.AuthCookie;
import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.Record;

@Ability.Owned
@Ability.Read(Ability.ADMIN)
@Ability.Write(Ability.ADMIN)
public class Session extends Record {
    private Date startDate;
    private String ip;
    private String client;
    private User user;
    private AuthCookie authCookie;

    private transient String loginKey;

    public Session() {
    }


}
