package com.onkiup.jendri.access.oauth;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.onkiup.jendri.access.User;
import com.onkiup.jendri.db.Column;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;

public abstract class OAuthToken extends Record {
    @Column.Indexed
    private String value;
    private Date created;
    private Date expires;

    public OAuthToken() {
    }

    public OAuthToken(User user) throws Exception {
        this.setOwner(user);
        SecureRandom r = new SecureRandom();
        byte[] random = new byte[32];
        r.nextBytes(random);
        value = new String(random);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public List<TokenScope> getScopes() throws Exception {
        return Query.from(TokenScope.class).where("token = ?", this).fetch();
    }
}
