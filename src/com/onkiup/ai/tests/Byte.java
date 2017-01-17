package com.onkiup.ai.tests;

public @interface Byte {
    byte value();

    class Processor implements AnnotationProcessor<Byte,java.lang.Byte> {

        @Override
        public boolean process(Byte annotation, java.lang.Byte value) {
            return value.equals(annotation.value());
        }
    }
}
