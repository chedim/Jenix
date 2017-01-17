package com.onkiup.jendri.access.oauth;

import com.onkiup.jendri.access.oauth.OAuthToken;
import com.onkiup.jendri.access.oauth.scope.OAuthScope;
import com.onkiup.jendri.db.Record;

public class TokenScope extends Record {
    private OAuthToken token;
    private Class<? extends OAuthScope> scope;

    public TokenScope() {
    }

    public TokenScope(OAuthToken token, Class<? extends OAuthScope> scope) {
        this.token = token;
        this.scope = scope;
    }

    public OAuthToken getToken() {
        return token;
    }

    public void setToken(OAuthToken token) {
        this.token = token;
    }

    public Class<? extends OAuthScope> getScope() {
        return scope;
    }

    public void setScope(Class<? extends OAuthScope> scope) {
        this.scope = scope;
    }
}
