package com.onkiup.daria.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.sun.tools.classfile.AccessFlags.Kind.Field;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DariaAnnotation(IndexAnnotationProcessor.class)
public @interface Index {
    String value() default "";
}
