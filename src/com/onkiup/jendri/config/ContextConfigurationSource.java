package com.onkiup.jendri.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.onkiup.jendri.ContextListener;
import com.onkiup.jendri.injection.Injector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ContextConfigurationSource implements ConfigurationSource, ContextListener.Client {

    private static final Logger LOG = LogManager.getLogger(ContextConfigurationSource.class);
    private final static Map<String, String> entries = new HashMap<>();

    @Override
    public Map<String, String> getEntries() {
        return entries;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = context.getInitParameter(name);
            LOG.info("Config: " + name + " = " + value);
            entries.put(name, value);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
