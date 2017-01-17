package com.onkiup.jendri.access.oauth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onkiup.jendri.access.oauth.scope.OAuthScope;

public interface OAuth {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Scope {
        Class<? extends OAuthScope> value();
    }
}
