package com.onkiup.ai.lang;

import com.onkiup.ai.ParamTest;
import com.onkiup.ai.Expression;
import com.onkiup.ai.Test;
import com.onkiup.ai.tests.Byte;
import com.onkiup.ai.tests.ByteRange;

public @interface Char {

    @Expression
    public static class Dot {
        public Dot(@Byte(46) Byte value) {

        }
    }

    @Expression
    public static class AsciiLetter {
        private Character value;
        private boolean isUpperCase;

        public AsciiLetter(@ByteRange({65, 90, 97, 122})java.lang.Byte value) {
            this.value = (char) value.byteValue();
            isUpperCase = value < 91;
        }
    }
}
