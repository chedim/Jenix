package com.onkiup.jendri.access.oauth.scope;

import java.security.SecureRandom;
import java.util.Date;

import com.onkiup.jendri.access.oauth.OAuthClient;
import com.onkiup.jendri.access.oauth.OAuthToken;
import com.onkiup.jendri.db.Record;

public class OAuthTempToken extends Record {
    private OAuthClient client;
    private String value;
    private Date created;

    public OAuthTempToken() {
    }

    public OAuthTempToken(OAuthClient client) {
        this.client = client;
        created = new Date();
        SecureRandom r = new SecureRandom();
        byte[] random = new byte[32];
        r.nextBytes(random);
        value = new String(random);
    }

    public OAuthClient getClient() {
        return client;
    }

    public String getValue() {
        return value;
    }

    public Date getCreated() {
        return created;
    }
}
