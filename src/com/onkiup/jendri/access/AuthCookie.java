package com.onkiup.jendri.access;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.db.Column;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.structure.Table;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.util.StringUtils;

public class AuthCookie extends Record {
    @Column.Indexed
    private String value;

    private Date generated;
    private String userAgent;

    @Inject
    private static Long lifetime;

    public AuthCookie() {
    }

    public AuthCookie(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        setOwner(user);
        userAgent = getAgentKey(request);
        AuthCookie similar = Query.from(AuthCookie.class).where("owner = ?", user).and("userAgent = ?", userAgent).fetchOne();
        if (similar != null) {
            Table.forJavaClass(AuthCookie.class).populateObject(similar.getId(), this);
        } else {
            SecureRandom r = new SecureRandom();
            byte[] random = new byte[32];
            r.nextBytes(random);
            value = new String(Base64.getEncoder().encode(random));
            generated = new Date();
            response.addCookie(new Cookie(AuthService.COOKIE_NAME, value));
        }
    }

    public String getValue() {
        return value;
    }

    private String getAgentKey(HttpServletRequest request) {
        return request.getHeader("User-Agent") + " @ " + request.getRemoteAddr();
    }

    public boolean validate(HttpServletRequest request) {
        boolean alive = isAlive();
        if (!alive) {
            try {
                delete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return getAgentKey(request).equals(userAgent) && alive;
    }

    public boolean isAlive() {
        if (lifetime != null) {
            Long life = (new Date().getTime() - generated.getTime()) / 1000L;
            return life < lifetime;
        }
        return true;
    }
}
