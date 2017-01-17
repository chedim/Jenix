package com.onkiup.jendri.util;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streaming {
    public static <T> Stream<T> nonNull(Function<Integer, T> generator, boolean parallel, int... characteristics) {
        Iterator<T> it = new Iterator<T>() {
            private T nextItem;
            private int nextPosition;
            @Override
            public boolean hasNext() {
                nextItem = generator.apply(nextPosition++);
                return nextItem != null;
            }

            @Override
            public T next() {
                return nextItem;
            }
        };
        int chars = 0;
        for (int characteristic : characteristics) {
            chars &= characteristic;
        }

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, chars), parallel);
    }
}
