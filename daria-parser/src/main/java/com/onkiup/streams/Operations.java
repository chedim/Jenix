package com.onkiup.streams;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Operations<T, R> extends Supplier<R> {

    ExcludeMessage EXCLUDE_MESSAGE = new ExcludeMessage();

    <I> Operations<I, I> filter(Predicate<T> filter);

    <I, O> Operations<I, O> map(Function<I, O> mapper);

    class ExcludeMessage extends Throwable {
        private ExcludeMessage() {
            super("", null, true, false);
        }
    }
}
