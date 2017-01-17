package com.onkiup.jendri.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class OopUtils {
    private static final Map<Class, Map<Class, Map<String, Class>>> BIND_CACHE = new HashMap<>();
    private static final Reflections REFLECTIONS = new Reflections("", new SubTypesScanner(true));

    public static Map<Class, Map<String, Class>> getBinds(Class from) {
        if (!BIND_CACHE.containsKey(from)) {
            Map<Class, Map<String, Class>> result = new HashMap<>();
            Method getTypeParameters = null;
            try {
                getTypeParameters = Class.class.getMethod("getTypeParameters");
                getTypeParameters.setAccessible(true);

                Type parent = from.getSuperclass();
                Type generic = from.getGenericSuperclass();
                TypeVariable[] vars = (TypeVariable[]) getTypeParameters.invoke(parent);
                if (parent != null) {
                    if (generic instanceof ParameterizedType) {
                        ParameterizedType p = (ParameterizedType) generic;
                        Type[] params = p.getActualTypeArguments();
                        if (params != null && params.length > 0) {
                            for (int i = 0; i < params.length; i++) {
                                if (!result.containsKey(parent)) {
                                    result.put((Class) parent, new HashMap<>());
                                }
                                if (params[i] instanceof Class) {
                                    result.get(parent).put(vars[i].getName(), (Class) params[i]);
                                }
                            }
                        }
                    }
                    if (!(Object.class.equals(parent))) {
                        result.putAll(getBinds((Class) parent));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            java.lang.reflect.Type[] types = from.getGenericInterfaces();
            for (java.lang.reflect.Type t : types) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) t;
                    Class superClass = (Class) pt.getRawType();
                    TypeVariable[] typeVariables = superClass.getTypeParameters();
                    Type[] arguments = pt.getActualTypeArguments();
                    for (int i = 0; i < typeVariables.length; i++) {
                        TypeVariable var = typeVariables[i];
                        Type argument = arguments[i];
                        if (!result.containsKey(superClass)) {
                            result.put(superClass, new HashMap<>());
                        }
                        if (argument instanceof TypeVariable) {
                            argument.toString();
                        } else {
                            result.get(superClass).put(var.getName(), (Class) argument);
                        }
                    }
                }
            }
            BIND_CACHE.put(from, result);
        }

        return BIND_CACHE.get(from);
    }

    public static Map<String, Class> getTypeArguments(Class from, Class forSuperClass) {
        return getBinds(from).get(forSuperClass);
    }

    public static Class getBoundClass(Class from, Class fur, String argument) {
        return getTypeArguments(from, fur).get(argument);
    }

    public static Class getWildcardUpperBound(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] args = pType.getActualTypeArguments();
            for (Type arg : args) {
                if (arg instanceof WildcardType && ((WildcardType) arg).getUpperBounds().length != 0) {
                    return (Class) ((WildcardType) arg).getUpperBounds()[0];
                }
            }
        }

        return null;
    }

    public static List<Field> getFields(Class from) {
        List<Field> result = new ArrayList<>();
        for (Field field : from.getDeclaredFields()) {
            result.add(field);
        }

        Class parent = from.getSuperclass();
        if (!Object.class.equals(parent)) {
            result.addAll(getFields(parent));
        }

        return result;
    }

    public static Map<String, Field> getFieldsMap(Class<?> aClass) {
        Map<String, Field> fields = new HashMap<>();
        for (Field field : getFields(aClass)) {
            fields.put(field.getName(), field);
        }
        return fields;
    }

    public static <T> Set<Class<? extends T>> getSubClasses(Class<T> of) {
        return REFLECTIONS.getSubTypesOf(of);
    }
}
