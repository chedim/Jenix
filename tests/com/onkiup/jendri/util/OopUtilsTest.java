package com.onkiup.jendri.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

public class OopUtilsTest {

    private interface TEST<T> {

    }

    private interface TEST2<X, Y> {

    }

    private static class TEST_TEST implements TEST<List>, TEST2<String, Boolean> {
        private Class<? extends TEST> test;
    }


    @Test
    public void testGetTypeClasses() throws Exception {
        Map<String, Class> params = OopUtils.getTypeArguments(TEST_TEST.class, TEST.class);
        assertEquals(1, params.size());
        assertEquals(List.class, params.get("T"));
        params = OopUtils.getTypeArguments(TEST_TEST.class, TEST2.class);
        assertEquals(2, params.size());
        assertEquals(String.class, params.get("X"));
        assertEquals(Boolean.class, params.get("Y"));
    }

    @Test
    public void testGetSuperclassBind() throws Exception {
        Field test = TEST_TEST.class.getDeclaredField("test");
        Class sb = OopUtils.getWildcardUpperBound(test);
        assertEquals(TEST.class, sb);
    }

}