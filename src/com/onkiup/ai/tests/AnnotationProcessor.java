package com.onkiup.ai.tests;

import java.lang.annotation.Annotation;

public interface AnnotationProcessor<X extends Annotation, Y> {
    boolean process(X annotation, Y value);
}
