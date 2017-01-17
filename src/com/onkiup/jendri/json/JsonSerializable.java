package com.onkiup.jendri.json;

import com.fasterxml.jackson.core.JsonGenerator;

public interface JsonSerializable {

    default void serialize(JsonGenerator generator) {
        Class me = getClass();

    }
}
