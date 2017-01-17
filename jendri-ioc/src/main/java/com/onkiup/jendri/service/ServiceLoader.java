package com.onkiup.jendri.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.onkiup.jendri.injection.Injector;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class ServiceLoader {
    private static final Reflections REFLECTIONS = new Reflections("", new SubTypesScanner());
    private static Map<Class, Object> services = new HashMap<>();

    public static void load() throws ClassNotFoundException {
        Reflections reflections = new Reflections();
        Set<Class<? extends ServiceStub>> services = reflections.getSubTypesOf(ServiceStub.class);
        HashMap<StartPoint, Set<ServiceStub>> loadOrder = new HashMap<>();
        for (Class serviceBaseClass : services) {
            Class<? extends Service> serviceClass = null;
            if (Modifier.isAbstract(serviceBaseClass.getModifiers())) {
                String serviceImplClassName = Injector.getFirst(serviceBaseClass.getName());
                if (serviceImplClassName != null) {
                    serviceClass = (Class<? extends Service>) Class.forName(serviceImplClassName);
                } else {
                    Set<Class> implementations = REFLECTIONS.getSubTypesOf(serviceBaseClass);
                    if (implementations.size() == 1) {
                        serviceClass = implementations.iterator().next();
                        if (Modifier.isAbstract(serviceClass.getModifiers())) {
                            serviceClass = null;
                        }
                    }
                }
            } else if (Service.class.isAssignableFrom(serviceBaseClass)) {
                serviceClass = serviceBaseClass;
            }

            if (serviceClass != null) {
                ServiceStub service = load(serviceClass);
                if (service != null) {
                    StartPoint startPoint = service.getStartPoint();
                    if (!loadOrder.containsKey(startPoint)) {
                        loadOrder.put(startPoint, new HashSet<>());
                    }

                    loadOrder.get(startPoint).add(service);
                }
            }
        }

        for (StartPoint on : StartPoint.values()) {
            if (loadOrder.containsKey(on)) {
                for (ServiceStub service : loadOrder.get(on)) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        throw new RuntimeException("Service " + service.getClass().getName() + " failed to start", e);
                    }
                }
            }
        }
    }

    private static <T extends ServiceStub> T load(Class<T> service) {
        Constructor<T> c = null;
        boolean isAccessible = false;
        try {
            c = service.getConstructor();
            isAccessible = c.isAccessible();
            c.setAccessible(true);
            T result = c.newInstance();
            ServiceLoader.services.put(service, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load service " + service, e);
        } finally {
            if (c != null) {
                c.setAccessible(isAccessible);
            }
        }
    }

}
