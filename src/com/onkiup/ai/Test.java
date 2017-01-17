package com.onkiup.ai;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
public @interface Test {
    Class<? extends ParamTest> value();
}
