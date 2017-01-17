package com.onkiup.jendri.util;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static String trim(String from, String what) {
        if (from.startsWith(what)) {
            from = from.substring(what.length());
        }

        if (from.endsWith(what)) {
            from = from.substring(0, from.length() - what.length());
        }

        return from;
    }
}
