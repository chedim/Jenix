package com.onkiup.daria.parser;

import java.io.BufferedReader;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

public class MatchBuilder {
    private char[] value = new char[16];
    private int length;

    public void append(int c) {
        ensureCapacity(length + 1);
        value[length++] = (char) c;
    }

    private void ensureCapacity(int l) {
        if (value.length < l) {
            value = Arrays.copyOf(value, l * 2);
        }
    }

    public int test(char[] chars) {
        if (length == 0) {
            return 0;
        }
        int matched = 0;
        for (int i = 0; i < Math.min(length, chars.length); i++) {
            if (chars[chars.length - i - 1] != value[length - i - 1]) {
                break;
            }
            matched++;
        }
        return matched;
    }

    public char[] dropRight(int len) {
        length = length - len;
        if (length < 0) {
            length = 0;
        }
        char[] result = Arrays.copyOfRange(value, length, value.length);
        value = Arrays.copyOf(value, length);
        return result;
    }

    public void clear() {
        length = 0;
    }

    @Override
    public String toString() {
        return String.valueOf(Arrays.copyOf(value, length));
    }

    public int length() {
        return length;
    }
}
