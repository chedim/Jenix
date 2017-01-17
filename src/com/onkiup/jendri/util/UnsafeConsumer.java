package com.onkiup.jendri.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface UnsafeConsumer<X> {
    void accept(X x) throws Exception;
}
