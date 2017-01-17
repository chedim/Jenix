package com.onkiup.jendri.session;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractFilter;
import com.onkiup.jendri.injection.Inject;
import org.apache.commons.lang3.StringUtils;

public class SessionFilter extends AbstractFilter {

    @Inject("Jendri-Session")
    private static String headerName;

    @Inject("/*")
    private static String url;

    @Override
    public Integer getPriority() {
        return Integer.MIN_VALUE + 1000;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            Cookies.start((HttpServletRequest) req, (HttpServletResponse) resp);
            filterChain.doFilter(req, resp);
            Cookies.clear();
        } else {
            filterChain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {

    }
}
