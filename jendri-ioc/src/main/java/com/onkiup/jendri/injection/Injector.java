package com.onkiup.jendri.injection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.onkiup.jendri.config.JendriConfig;
import com.onkiup.jendri.service.Service;
import com.onkiup.jendri.service.StartPoint;
import com.onkiup.jendri.util.OopUtils;
import com.onkiup.jendri.util.Streaming;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

public class Injector implements Service {

    private static Map<String, String> injectables = new HashMap<>();
    private static Map<String, Set<String>> reverse = new HashMap<>();

    private static Map<String, Set<Field>> injectPoints = new HashMap<>();

    private static Map<String, Set<Consumer>> consumers = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger(Injector.class.getName());

    private static final Reflections REFLECTIONS = new Reflections("", new SubTypesScanner());

    static {
        Reflections reflections = new Reflections("", new FieldAnnotationsScanner()
//                .addClassLoader(Thread.currentThread().getContextClassLoader())
        );

        Set<Field> fields = reflections.getFieldsAnnotatedWith(Inject.class);
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new RuntimeException("Injection cannot be performed on a non-static field '" + field.getName() + "' of class " + field.getDeclaringClass().getName());
            }

            registerPoint(field);
            inject(field);
        }
    }

    public static void inject(Field field) {
        try {
            field.setAccessible(true);
            if (field.get(null) != null) {
                // injection was already implemented for this field
                return;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(false);
        }

        Class type = field.getType();
        if (Class.class.equals(type) || Enum.class.equals(type)) {
            injectClass(field);
        } else if (type.isPrimitive() || type.equals(String.class) || type.equals(Integer.class)) {
            injectConfig(field);
        } else {
            injectObject(field);
        }
    }

    private static Stream<String> getKeys(Field field) {
        Class type = field.getType();
        boolean addFieldName = false;
        if (Class.class.equals(type)) {
            type = OopUtils.getWildcardUpperBound(field);
        } else if (type.isPrimitive() || type.equals(String.class)) {
            type = field.getDeclaringClass();
            addFieldName = true;
        }
        Stream<String> keys = getKeys(type);

        if (addFieldName) {
            keys = keys.flatMap(k -> Stream.of(k, k + "[" + convertCamelPart(field.getName()) + "]"));
        }

        return keys;
    }

    private static Stream<String> getKeys(Class type) {
        return getKeys(type.getName());
    }

    public static Stream<String> getKeys(String name) {
        return getKeys(name, true, false);
    }

    private static Stream<String> getKeys(String name, boolean forward, boolean reversed) {
        final Integer[] lastIndex = {-3};
        return Streaming.nonNull(i -> {
            String result = null;
            synchronized (lastIndex[0]) {
                if (lastIndex[0] == -3) {
                    lastIndex[0] = 0;
                    return name;
                }
            }
            if (forward) {
                int nextIndex = -1;
                synchronized (lastIndex[0]) {
                    if (lastIndex[0] > -1) {
                        nextIndex = name.indexOf('.', lastIndex[0] + 1);
                        lastIndex[0] = nextIndex;
                    }
                }
                if (nextIndex > -1) {
                    result = name.substring(nextIndex + 1);
                } else if (reversed) {
                    synchronized (lastIndex[0]) {
                        if (lastIndex[0] > -2) {
                            nextIndex = name.lastIndexOf('.');
                            if (nextIndex > -1) {
                                result = name.substring(0, nextIndex) + ".*";
                            }
                            lastIndex[0] = -2;
                        }
                    }
                }
            }
            return result;
        }, false).map(Injector::convertCamel);

//        Set<String> keys = new LinkedHashSet<>();
//        String[] parts = name.split("\\.");
//        for (int i = 0; i < parts.length; i++) {
//            parts[i] = convertCamelPart(parts[i]);
//        }
//
//        if (forward) {
//            for (int i = 0; i < parts.length; i++) {
//                if (forward) {
//                    String[] key = ArrayUtils.subarray(parts, i, parts.length);
//                    if (key.length > 1) {
//                        String result = StringUtils.join(key, ".");
//                        keys.add(result);
//                    }```
//                }
//            }
//        }
//
//        if (reversed) {
//            String[] key = ArrayUtils.subarray(parts, 0, parts.length - 1);
//            if (key.length > 0) {
//                keys.add(StringUtils.join(key, ".") + ".*");
//            }
//        }
//        return keys.stream();
    }

    static String convertCamel(String key) {
        return Arrays.asList(key.split("\\.")).stream().map(Injector::convertCamelPart).collect(Collectors.joining("."));
    }

    static String convertCamelPart(String camel) {
        return Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(camel)).stream().filter(s -> !s.equals("_")).map(String::toLowerCase).collect(Collectors.joining("_"));
    }

    public static String get(Stream<String> keys) {
        return keys.map(k -> get(k)).filter(o -> o != null).findFirst().orElse(null);
    }

    public static Stream<String> match(String key) {
        return getKeys(key).flatMap(prefix -> JendriConfig.matchLevels(prefix, 1)).map(s -> {
            injectables.put(s, JendriConfig.getConfigValue(s));
            return injectables.get(s);
        });
    }

    public static String get(String key) {
        if (!injectables.containsKey(key)) {
            String result = JendriConfig.getConfigValue(key);
            if (result != null) {
                set(key, result);
            }
        }

        return injectables.get(key);
    }

    public static void set(Class type, Class injection) {
        set(type, injection.getName());
    }

    public static void set(Class type, String injection) {
        set(type.getName(), injection);
    }

    public static void set(String key, Class injection) {
        injectables.put(key, injection.getName());
    }

    public static void set(String name, String injection) {
        set(getKeys(name), injection);
        LOGGER.info(name + " = " + injection);

        getKeys(name, false, true).forEach(key -> {
            reverse.putIfAbsent(key, new HashSet<>());
            reverse.get(key).add(injection);

            if (consumers.containsKey(key)) {
                for (Consumer consumer : consumers.get(key)) {
                    Class type = OopUtils.getBoundClass(consumer.getClass(), Consumer.class, "T");
                    Object param = make(type, injection);
                    consumer.accept(param);
                }
            }
        });
    }

    public static <T> T make(Class<T> type, String source) {
        try {
            T result = null;
            if (source != null) {
                if (Class.class.equals(type) || Enum.class.equals(type)) {
                    result = (T) Class.forName(source);
                } else if (type.isPrimitive() || type.equals(String.class) || type.equals(Integer.class)) {
                    if (type.isPrimitive() && type != Character.class) {
                        Constructor<T> c = type.getConstructor(String.class);
                        result = c.newInstance(source);
                    } else if (type == String.class) {
                        result = (T) source;
                    } else if (type == Character.class) {
                        if (source.length() > 0) {
                            result = (T) new Character(source.charAt(0));
                        } else {
                            result = null;
                        }
                    }
                } else {
                    Class<? extends T> clazz = (Class<? extends T>) Class.forName(source);
                    result = clazz.newInstance();
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Make of " + type.toString() + " from '" + source + "' failed", e);
        }
    }

    public static void set(Stream<String> keys, String injection) {
        keys.forEach(key -> {
            injectables.put(key, injection);

            if (injectPoints.containsKey(key)) {
                Set<Field> fields = injectPoints.get(key);
                for (Field field : fields) {
                    inject(field);
                }
            }
        });
    }

    private static void injectObject(Field field) {
        String className = get(getKeys(field));

        try {
            Class clazz = null;
            if (className != null) {
                clazz = Class.forName(className);
            } else {
                Inject ann = field.getAnnotation(Inject.class);
                Class def = ann.defaultType();
                if (def != Object.class) {
                    clazz = def;
                } else {
                    // if only one class can handle this... inject it!
                    Set subTypes = REFLECTIONS.getSubTypesOf(field.getType());
                    if (subTypes.size() == 1) {
                        clazz = (Class) subTypes.iterator().next();
                    }
                }
            }

            if (clazz != null) {
                Object value = clazz.newInstance();
                setField(field, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Injection of " + className + " into " + field + " failed", e);
        }
    }

    private static void injectClass(Field field) {
        String className = get(getKeys(field));

        try {
            if (className != null) {
                Class inject = Class.forName(className);
                setField(field, inject);
            } else {
                Inject ann = field.getAnnotation(Inject.class);
                Class def = ann.defaultType();
                if (def != Object.class) {
                    setField(field, def);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Injection of " + className + " into " + field + " failed", e);
        }
    }

    private static void setField(Field field, Object value) throws IllegalAccessException, NoSuchFieldException {
        LOGGER.info("setting field " + field.getDeclaringClass().getSimpleName() + "::" + field.getName() + " to " + value.toString());
        int modifiers = accessField(field);
        field.set(null, value);
        closeField(field, modifiers);
    }

    private static int accessField(Field field) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        int modifiers = field.getModifiers();
        return modifiers;
    }

    public static void closeField(Field f, int modifiers) throws IllegalAccessException, NoSuchFieldException {
        if (!Modifier.isPublic(modifiers)) {
            f.setAccessible(false);
        }
    }

    private static void injectConfig(Field field) {
        Class type = field.getType();
        Object value = get(getKeys(field));
        if (value == null) {
            Inject ann = field.getAnnotation(Inject.class);
            value = ann.value();
            if (value == null) {
                return;
            }
        }
        try {
            if (type.getName().startsWith("java.lang.") && type != Character.class) {
                Constructor c = type.getConstructor(String.class);
                value = c.newInstance(value);
            } else if (type == Character.class) {
                if (((String) value).length() > 0) {
                    value = ((String) value).charAt(0);
                } else {
                    value = null;
                }
            }
            setField(field, value);
        } catch (Exception e) {
            throw new RuntimeException("Injection for " + field + " failed", e);
        }
    }

    private static void registerPoint(Field field) {
        getKeys(field).forEach(key -> {
            if (!injectPoints.containsKey(key)) {
                injectPoints.put(key, new HashSet<>());
            }

            injectPoints.get(key).add(field);
        });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public StartPoint getStartPoint() {
        return StartPoint.BOOT;
    }

    public static void unset(Class type, Class injection) {
        injectables.remove(type);
    }

    public static void on(String initialKey, Consumer handler) {
        getKeys(initialKey).forEach(key -> {
            consumers.putIfAbsent(key, new HashSet<>());
            consumers.get(key).add(handler);
        });
    }

    public static String getFirst(String key) {
        return get(getKeys(key));
    }
}
