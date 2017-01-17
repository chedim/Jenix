package com.onkiup.jendri.api;

import javax.servlet.http.HttpServletRequest;

import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.QueryBuilder;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;

interface Methods {
    QueryBuilder parse(String servletPath, String pkg, HttpServletRequest request);

    <T extends Fetchable> QueryBuilder<T> parse(Class<T> type, String query) throws Exception;

    boolean hasId(HttpServletRequest request);

    Class getRequestedClass(String servletPath, String pkg, HttpServletRequest request) throws Exception;

    String getEndpoint(String pkg, Class type);

}


public final class RequestParserService  {

    @Inject
    private static RequestParserService.Implementation implementation;

    public static QueryBuilder parse(String servletPath, String pkg, HttpServletRequest request) {
        return implementation.parse(servletPath, pkg, request);
    }

    public static <T extends Fetchable> QueryBuilder<T> parse(Class<T> type, String query) throws Exception {
        return implementation.parse(type, query);
    }

    public static boolean hasId(HttpServletRequest request) {
        return implementation.hasId(request);
    }

    public static Class getRequestedClass(String servletPath, String pkg, HttpServletRequest request) throws Exception {
        return implementation.getRequestedClass(servletPath, pkg, request);
    }

    public static String getEndpoint(String pkg, Class type) {
        return implementation.getEndpoint(pkg, type);
    }

    public interface Implementation extends ServiceStub, Methods {
    }
}