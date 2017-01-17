package com.onkiup.jendri.access.oauth;

import com.onkiup.jendri.access.User;

public class OAuthRefreshToken extends OAuthToken {
    private OAuthAccessToken pair;
    private OAuthRefreshToken previous;
    private OAuthRefreshToken next;

    public OAuthRefreshToken() {
        super();
    }

    public OAuthRefreshToken(User user) throws Exception {
        super(user);
        pair = new OAuthAccessToken(user);
    }

    public OAuthRefreshToken(User user, OAuthRefreshToken previous) throws Exception {
        this(user);
        this.previous = previous;
    }

    public OAuthAccessToken getPair() {
        return pair;
    }

    public OAuthRefreshToken getPrevious() {
        return previous;
    }

    public OAuthRefreshToken getNext() {
        return next;
    }

    public void setNext(OAuthRefreshToken next) {
        this.next = next;
    }
}
