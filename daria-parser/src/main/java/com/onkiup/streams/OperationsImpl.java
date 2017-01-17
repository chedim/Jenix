package com.onkiup.streams;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import sun.misc.Unsafe;

public class OperationsImpl<I, O> implements Operations<I, O> {

    private Supplier<I> supplier;
    private List<UnsafeFunction> steps = new LinkedList<>();

    @Override
    public <I1> Operations<I1, I1> filter(Predicate<I> filter) {
        steps.add(o -> {
            if (!filter.test((I) o)) {
                throw EXCLUDE_MESSAGE;
            }
            return o;
        });

        return (Operations<I1, I1>) this;
    }

    @Override
    public <I1, O1> Operations<I1, O1> map(Function<I1, O1> mapper) {
        return null;
    }

    @Override
    public O get() {
        return null;
    }
}
