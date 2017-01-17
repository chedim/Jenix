package com.onkiup.jendri.util.async;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.sun.istack.internal.NotNull;

public interface IAsyncSupplier<R> extends IAsyncProcessor, Supplier<R> {
    public R poll();
    R poll(long timeout, @NotNull TimeUnit unit) throws InterruptedException;
}
