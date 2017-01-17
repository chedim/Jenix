package com.onkiup.jendri.db;

import com.onkiup.jendri.type.TypeUtil;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;

public interface Fetchable extends PersistantObject {

    public @interface Mask {
        public Class<? extends Fetchable> value();
    }

    public void setId(Long id);

    public Long getId();

    public static <T extends Fetchable> void fetch (T into, HashMap<String, Object> from) {
        fetch(into, from, "");
    }

    public static <T extends Fetchable> T fetch(Class<T> into, HashMap<String, Object> from) {
        return fetch(into, from, "");
    }

    public static <T extends Fetchable> T fetch(Class<T> into, HashMap<String, Object> from, @NotNull String prefix) {
        try {
            T result = into.getConstructor().newInstance();
            fetch(result, from, prefix);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Fetchable> void fetch(T into, HashMap<String, Object> from, @NotNull String prefix) {
        Class<T> as = (Class<T>) into.getClass();
        Model.Prefix prefixAnnotation = as.getAnnotation(Model.Prefix.class);
        prefix = prefix + prefixAnnotation == null ? "" : prefixAnnotation.value();

        Field[] fields = as.getFields();
        for (Field field : fields) {
            try {
                Object value = readFieldValueFromSource(prefix, field, from);
                setField(into, field, value);
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    public static Object readFieldValueFromSource(@NotNull String prefix, Field field, HashMap<String, Object> source) {
        if (prefix == null) {
            prefix = "";
        }

        Model.Prefix prefAnn = field.getAnnotation(Model.Prefix.class);
        if (prefAnn != null) {
            prefix = prefAnn.value() + prefix;
        }

        Class fieldType = field.getType();

        if (Fetchable.class.isAssignableFrom(fieldType)) {
            Fetchable result = Fetchable.fetch(fieldType, source, prefix);
            return result;
        }

        String fieldName = prefix;
        Model.StoreAs storeAs = field.getAnnotation(Model.StoreAs.class);
        if (storeAs != null) {
            fieldName += storeAs.value();
        } else {
            fieldName += field.getName();
        }

        if (!source.containsKey(fieldName)) {
            throw new NoSuchElementException("fieldName");
        }

        Object val = source.get(fieldName);
        return TypeUtil.cast(val, field.getType());
    }

    public static <A extends Fetchable> void setField(A instance, Field field, Object value) {
        String fieldName = field.getName();
        if (value != null) {
            Class valueType = value.getClass();
            String setterName = "load"
                    + fieldName.substring(0, 1).toUpperCase()
                    + ((fieldName.length() > 1) ? fieldName.substring(1) : "");
            try {
                Method setter = instance.getClass().getMethod(setterName, valueType);
                setter.setAccessible(true);
                setter.invoke(instance, value);
                return;
            } catch (NoSuchMethodException e) {
                // nothing here
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
