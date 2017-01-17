package com.onkiup.jendri.util.async;

import java.util.function.Consumer;

public interface IAsyncConsumer<X> extends Consumer<X>, IAsyncProcessor {
}
