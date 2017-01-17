package com.onkiup.jendri.db;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface Modification<Of extends Modificable> {


    public default Class<? extends Modificable> getModificatedClass() {
        return getModificatedClass(getClass());
    }

    public static Class<? extends Modificable> getModificatedClass(Class<? extends Modification> of) {
        Type[] interfaces = of.getGenericInterfaces();
        ParameterizedType modification = null;
        for (int i=0; i<interfaces.length; i++) {
            Type inter = interfaces[i];
            if (inter instanceof ParameterizedType) {
                if (((ParameterizedType) inter).getRawType().equals(Modification.class)) {
                    modification = (ParameterizedType) inter;
                    break;
                }
            }
        }
        if (modification != null) {
            Class modified = (Class) modification.getActualTypeArguments()[0];
            return modified;
        }

        return null;
    }
}
