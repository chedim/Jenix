package com.onkiup.jendri.util.async;

public interface IAsyncProcessor {
    void close();
    void closeAndWait();
    void spawnProcessors();
    void spawnProcessors(int number);
    void killProcessors(int number);
}
