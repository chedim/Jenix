package com.onkiup.jendri.api;

import javax.servlet.http.HttpServletRequest;

import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.QueryBuilder;

public class RequestParserServiceImpl implements RequestParserService.Implementation {
    @Override
    public QueryBuilder parse(String servletPath, String pkg, HttpServletRequest request) {
        ApiRequestParser parser = new ApiRequestParser(servletPath, pkg, request);
        try {
            return parser.getQuery();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Fetchable> QueryBuilder<T> parse(Class<T> type, String query) throws Exception {
        ApiRequestParser parser = new ApiRequestParser(type, query);
        return parser.getQuery();
    }

    @Override
    public boolean hasId(HttpServletRequest request) {
        return ApiRequestParser.ID_PATTERN.matcher(request.getRequestURI()).matches();
    }

    @Override
    public Class getRequestedClass(String servletPath, String pkg, HttpServletRequest request) throws Exception {
        ApiRequestParser parser = new ApiRequestParser(servletPath, pkg, request);
        return parser.getResultType();
    }

    @Override
    public String getEndpoint(String pkg, Class type) {
        String className = type.getName();
        if (className.startsWith(pkg)) {
            return className.substring(pkg.length()).replaceAll("\\.", "/");
        }
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
