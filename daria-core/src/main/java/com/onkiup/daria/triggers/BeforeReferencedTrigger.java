package com.onkiup.daria.triggers;

import java.lang.reflect.Field;

public interface BeforeReferencedTrigger {
    void beforeReferenced(Object referencee);
}
