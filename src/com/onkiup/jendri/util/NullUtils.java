package com.onkiup.jendri.util;

public class NullUtils {
    public static boolean orNull(Object... objects) {
        for(Object object : objects) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }

    public static boolean orNotNull(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean andNull(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                return false;
            }
        }

        return true;
    }

    public static boolean xnorNull(Object... objects) {
        Boolean isNull = null;
        for (Object object : objects) {
            if (isNull == null) {
                isNull = object == null;
            } else {
                if (isNull != (object == null)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean notNull(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return false;
            }
        }

        return true;
    }
}
