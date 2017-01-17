package com.onkiup.ai.tests;

import java.lang.*;
import java.lang.Byte;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
public @interface ByteRange {
    byte[] value();

    class $ implements AnnotationProcessor<ByteRange, java.lang.Byte> {

        @Override
        public boolean process(ByteRange annotation, Byte value) {
            byte[] range = annotation.value();
            int len = range.length;
            if (len % 2 != 0) {
                throw new RuntimeException("Invalid arguments length: should be even");
            }
            for (int i=0; i < len; i += 2) {
                if (value >= range[i] && value <= range[i+1]) {
                    return true;
                }
            }
            return false;
        }
    }
}
