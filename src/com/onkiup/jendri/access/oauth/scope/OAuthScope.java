package com.onkiup.jendri.access.oauth.scope;

import com.onkiup.jendri.access.oauth.OAuth;

@OAuth.Scope(UserScopes.Credentials.class)
public interface OAuthScope {
    static String getName(Class<? extends OAuthScope> from) {
        return from.getName();
    }

    default String getHumanName() {
        return getClass().getSimpleName();
    }

    String getExplanation();
}