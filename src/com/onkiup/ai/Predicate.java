package com.onkiup.ai;

@FunctionalInterface
public interface Predicate<X> {
    X test();

    static Predicate get(Class predicate, Object... arguments) {
        return null;
    }
}
