package com.onkiup.jendri;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import com.onkiup.jendri.injection.Injector;
import com.onkiup.jendri.service.ServiceLoader;
import com.onkiup.jendri.util.OopUtils;
import com.onkiup.jendri.util.ThreadUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class ContextListener implements ServletContextListener {
    private static final Reflections REFLECTIONS = new Reflections("", new TypeAnnotationsScanner(), new SubTypesScanner());
    private static final Logger LOG = LogManager.getLogger(ContextListener.class);

    private static final List<Client> clients = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Set<Class<? extends Client>> clientClasses = OopUtils.getSubClasses(Client.class);
        for (Class<? extends Client> clientClass : clientClasses) {
            try {
                Client client = clientClass.newInstance();
                clients.add(client);
                client.contextInitialized(servletContextEvent);
            } catch (Exception e) {
                LOG.error("Unable to deliver context to " + clientClass.getName(), e);
            }
        }

        try {
            LOG.info("Loading services...");
            ServiceLoader.load();
            LOG.info("Services are loaded.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to initialize services", e);
        }

        ServletContext context = servletContextEvent.getServletContext();
        if (context.getMajorVersion() >= 3) {
            Set<Class<? extends AbstractServlet>> servlets = REFLECTIONS.getSubTypesOf(AbstractServlet.class);
            for (Class<? extends AbstractServlet> servletClass : servlets) {
                if (!Modifier.isAbstract(servletClass.getModifiers())) {
                    try {
                        LOG.info("Instantiating servlet " + servletClass.getName());
                        AbstractServlet servlet = servletClass.newInstance();
                        String baseUrl = servlet.getUrl();
                        LOG.info("Registering " + servletClass.getSimpleName() + " with baseUrl = " + baseUrl);
                        ServletRegistration registration = context.addServlet(servletClass.getSimpleName(), servlet);
                        registration.addMapping(servlet.getUrl());
                        LOG.info("Registered " + servletClass.getSimpleName() + " with baseUrl = " + baseUrl);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            REFLECTIONS.getSubTypesOf(AbstractFilter.class).stream().map(o -> {
                try {
                    return o.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).sorted((o1, o2) -> o1.getPriority().compareTo(o2.getPriority())).forEach(filter -> {
                FilterRegistration.Dynamic registration = context.addFilter(filter.getClass().getSimpleName(), filter);
                registration.addMappingForUrlPatterns(null, true, filter.getUrl());
                LOG.info("Registered filter: " + filter.getClass().getName());
            });
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        for (Client client : clients) {
            try {
                client.contextDestroyed(servletContextEvent);
            } catch (Exception e) {
                LOG.error("Unable to deliver contextDestroyed event to " + client.getClass().getName(), e);
            }
        }
    }

    public interface Client extends ServletContextListener {

    }
}
