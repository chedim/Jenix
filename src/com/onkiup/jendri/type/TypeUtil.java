package com.onkiup.jendri.type;

import com.onkiup.jendri.db.Fetchable;
import com.onkiup.jendri.db.Query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TypeUtil {

    public static <T> T cast(Object source, Class<T> to) {
        if (source == null) return null;

        T result = null;
        if (Fetchable.class.isAssignableFrom(to)) {
            if (source instanceof Number) {
                try {
                    result = (T) Query.from((Class<? extends Fetchable>) to).id(source);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (result == null) {
            result = castByConstructor(source, to);
        }

        if (result == null) {
            result = castByValueOf(source, to);
        }

        if (result == null) {
            throw new ClassCastException("Unable convert "+source.getClass().getSimpleName()+" to "+to.getSimpleName());
        }

        return result;
    }

    private static <T> T castByConstructor(Object source, Class<T> to) {
        try {
            Constructor<T> constructor = to.getConstructor(source.getClass());
            constructor.setAccessible(true);
            return constructor.newInstance(source);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T castByValueOf(Object source, Class<T> to) {
        try {
            Method valueOf = to.getMethod("valueOf", source.getClass());
            valueOf.setAccessible(true);
            Constructor<T> constructor = to.getConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            valueOf.invoke(instance, source);
            return instance;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            return null;
        }
    }

}
