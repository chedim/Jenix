package com.onkiup.ai.tests;

public @interface Regexp {
    String value();

    class $ implements AnnotationProcessor<Regexp,String> {
        @Override
        public boolean process(Regexp annotation, String value) {
            return value.matches(annotation.value());
        }
    }
}
