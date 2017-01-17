package com.onkiup.daria.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorDefinition {
    String[] aliases();
    OperatorPriority priority() default OperatorPriority.LITERAL;
    int priorityShift() default 0;
    String inStatus() default ProcessorStatus.GENERAL;
    String outStatus() default ProcessorStatus.GENERAL;
}
