package com.onkiup.jendri.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Set;

import com.onkiup.jendri.service.Service;
import com.onkiup.jendri.service.StartPoint;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

public class Generator implements Service {

    private static HashMap<Class, HashMap<String, Object>> generated = new HashMap<>();

    static {
        Reflections reflections = new Reflections("", new FieldAnnotationsScanner(), new SubTypesScanner());

        Set<Field> fields = reflections.getFieldsAnnotatedWith(Generate.class);
        for (Field field : fields) {
            if (HashMap.class.isAssignableFrom(field.getType())) {
                if (Modifier.isStatic(field.getModifiers())) {
                    ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
                    Class type = (Class) fieldType.getActualTypeArguments()[1];
                    if (!generated.containsKey(type)) {
                        generated.put(type, new HashMap<>());
                        Injector.match(type.getName()).forEach(key -> {
                            try {
                                Object instance = type.newInstance();
                                for (Field typeField : type.getDeclaredFields()) {
                                    boolean isAccessible = typeField.isAccessible();
                                    typeField.setAccessible(true);
                                    String optKey = type.getName() + "." + key + "." + Injector.convertCamelPart(typeField.getName());
                                    String value = Injector.getFirst(optKey);
                                    typeField.set(instance, Injector.make(typeField.getType(), value));
                                    typeField.setAccessible(isAccessible);
                                }
                                generated.get(type).put(key, instance);
                            } catch (Exception e) {
                                throw new RuntimeException("Unable to generate " + type.getName(), e);
                            }
                        });
                    }

                    try {
                        boolean isAccessible = field.isAccessible();
                        field.setAccessible(true);
                        field.set(null, generated.get(type));
                        field.setAccessible(isAccessible);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to set generated field " + field.getName(), e);
                    }
                } else {
                    throw new RuntimeException("Unable to generate non-static field " + field.getName());
                }
            } else {
                throw new RuntimeException("Unable to generate field " + field.getName() + ": should be declared as HashMap<String, ?>");
            }
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public StartPoint getStartPoint() {
        return StartPoint.BOOT;
    }
}
