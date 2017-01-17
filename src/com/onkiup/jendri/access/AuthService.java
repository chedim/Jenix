package com.onkiup.jendri.access;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractFilter;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;

interface Methods {
    default User getUser(HttpServletRequest request, HttpServletResponse response) {
        return getUser(request, response, false);
    }
    User getUser(HttpServletRequest request, HttpServletResponse response, boolean setCookie);

    void setUser(User user, HttpServletRequest request, HttpServletResponse response, boolean setCookie) throws Exception;

    void dropUser(HttpServletRequest request, HttpServletResponse response) throws Exception;
}

public final class AuthService {

    @Inject
    private static Implementation implementation;

    @Inject("user")
    public static String USER_ATTR;
    @Inject("juac")
    public static String COOKIE_NAME;

    public static User getUser(HttpServletRequest request, HttpServletResponse response) {
        return implementation.getUser(request, response);
    }

    public static User getUser(HttpServletRequest request, HttpServletResponse response, boolean setCookie) {
        return implementation.getUser(request, response, setCookie);
    }

    public static void setUser(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        implementation.setUser(user, request, response, false);
    }

    public static void setUser(User user, HttpServletRequest request, HttpServletResponse response, boolean setCookie) throws Exception {
        implementation.setUser(user, request, response, setCookie);
    }

    public static void dropUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        implementation.dropUser(request, response);
    }

    public interface Implementation extends Methods, ServiceStub {

    }
}
