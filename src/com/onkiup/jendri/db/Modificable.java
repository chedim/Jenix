package com.onkiup.jendri.db;

import com.onkiup.jendri.db.structure.Table;

public interface Modificable extends PersistantObject {

    default <T extends Modificable> T as(Class<T> as) {
        if (!is(as)) {
            throw new RuntimeException(getClass().getSimpleName() + " is not a " + as.getSimpleName());
        }

        return as(this, as);
    }

    default <T extends Modificable> boolean is(Class<T> is) {
        return is(this, is); // that :-D
    }

    public static <A extends Modificable, B extends Modificable> boolean is(A object, Class<B> test) {
        Class me = object.getClass();
        return me.isAssignableFrom(test) || test.isAssignableFrom(me);
    }

    public static <A extends Modificable, T extends Modificable> T as(A object, Class<T> as) {
        if (as.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        if (!object.getClass().isAssignableFrom(as)) {
            throw new ClassCastException(object.getClass().getSimpleName() + " cannot be converted to " + as.getSimpleName());
        }

        try {
            Table<A> tableA = Table.forJavaClass((Class<A>) object.getClass(), Database.getInstance());
            Table<T> tableT = Table.forJavaClass(as, Database.getInstance());
            T result = tableT.createInstance();
            tableA.cloneInto(object, (A) result);
            Table.Field pk = tableT.getPrimaryKey();
            tableT.populateObject(pk.get(result), result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
