package com.onkiup.jendri.access;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.access.oauth.OAuthAccessToken;
import com.onkiup.jendri.db.Query;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;

public class AuthServiceImpl implements AuthService.Implementation {

    @Override
    public User getUser(HttpServletRequest request, HttpServletResponse response, boolean setCookie) {
        User result = (User) request.getAttribute(AuthService.USER_ATTR);
        setCookie = setCookie || request.getParameter("set_cookie") != null;
        try {
            if (result == null) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(AuthService.COOKIE_NAME)) {
                            String value = cookie.getValue();
                            AuthCookie stored = Query.from(AuthCookie.class).where("value = ?", value).fetchOne();
                            if (stored != null && stored.validate(request)) {
                                setUser(stored.getOwner(), request, response, false);
                            }
                        }
                    }
                }

                if (result == null) {
                    // authorizing
                    String identifier = request.getParameter("user_identifier");

                    if (identifier != null) {
                        User candidate = Query.from(User.class).where("identifier = ?", identifier).fetchOne();
                        if (candidate != null) {
                            setUser(candidate, request, response, setCookie);
                        }
                    }
                }

                if (result == null) {
                    // OAuth?
                    try {
                        OAuthAccessToken token = OAuthAccessToken.from(new OAuthAccessResourceRequest(request));
                        if (token != null) {
                            setUser(token.getOwner(), request, response, false);
                        }
                    } catch (OAuthProblemException e) {
                        // there's actually nothing to do here :)
                    }
                    // cookies cannot be set for this auth method
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void setUser(User user, HttpServletRequest request, HttpServletResponse response, boolean setCookie) throws Exception {
        request.setAttribute(AuthService.USER_ATTR, user);
        if (setCookie) {
            AuthCookie authCookie = new AuthCookie(user, request, response);
            authCookie.saveImmediately();
        }
    }

    @Override
    public void dropUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Cookie[] cookies = request.getCookies();
        request.removeAttribute(AuthService.USER_ATTR);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AuthService.COOKIE_NAME)) {
                    String value = cookie.getValue();
                    AuthCookie stored = Query.from(AuthCookie.class).where("value = ?", value).fetchOne();
                    if (stored != null && stored.validate(request)) {
                        stored.delete();
                        return;
                    }
                }
            }
        }
        // OAuth?
        try {
            OAuthAccessToken token = OAuthAccessToken.from(new OAuthAccessResourceRequest(request));
            if (token != null) {
                token.delete();
            }
        } catch (OAuthProblemException e) {
            // there's actually nothing to do here :)
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
