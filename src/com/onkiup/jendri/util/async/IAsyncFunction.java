package com.onkiup.jendri.util.async;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sun.istack.internal.NotNull;

public interface IAsyncFunction<X, R> extends IAsyncProcessor, Function<X, R> {
    R get();
}
