package com.onkiup.jendri;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServlet;

public abstract class AbstractServlet extends HttpServlet{
    public abstract String getUrl();
}
