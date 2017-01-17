package com.onkiup.streams;

@FunctionalInterface
public interface UnsafeFunction<I, O> {
    O apply(I item) throws Operations.ExcludeMessage;
}
