package com.onkiup.jendri.service;

public interface ServiceStub {
    void start() throws Exception;
    void stop() throws Exception;

    default StartPoint getStartPoint() {
        return StartPoint.START;
    }
}
