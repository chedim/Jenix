package com.onkiup.daria.triggers;

import java.lang.reflect.Field;

public interface BeforeUnreferencedTrigger {
    void beforeUnreferenced(Object referencee);
}
