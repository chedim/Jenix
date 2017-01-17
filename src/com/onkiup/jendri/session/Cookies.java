package com.onkiup.jendri.session;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Cookies {

    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();
    private static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<>();
    private static ThreadLocal<HashMap<String, String>> cookies = new ThreadLocal<>();

    static void start(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, String> cookies = new HashMap<>();
        if (request != null && response != null) {
            currentRequest.set(request);
            currentResponse.set(response);
            Cookie[] requestCookie = request.getCookies();
            if (requestCookie != null) {
                for (Cookie cookie : requestCookie) {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            }
        }
        Cookies.cookies.set(cookies);
    }

    static void clear() {
        currentRequest.remove();
        currentResponse.remove();
    }

    public static String get(String key) {
        HttpServletRequest request = currentRequest.get();
        if (request != null) {
            return cookies.get().get(key);
        }
        throw new RuntimeException("No request is served");
    }

    public static void set(String key, String value) {
        HttpServletRequest request = currentRequest.get();
        if (request != null) {
            if (cookies.get().containsKey(key)) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals(key)) {
                        cookie.setValue(value);
                    }
                }
            } else {
                Cookie cookie = new Cookie(key, value);
                currentResponse.get().addCookie(cookie);
            }
            cookies.get().put(key, value);
        } else {
            throw new RuntimeException("No request is served");
        }
    }

    public static HttpServletRequest getRequest() {
        return currentRequest.get();
    }

    public static HttpServletResponse getResponse() {
        return currentResponse.get();
    }

    public static HashMap<String, Object> getAll() {
        HttpSession session = getRequest().getSession();
        Enumeration<String> attributes = session.getAttributeNames();
        HashMap<String, Object> result = new HashMap<>();
        while (attributes.hasMoreElements()) {
            String name = attributes.nextElement();
            result.put(name, session.getAttribute(name));
        }
        return result;
    }
}
