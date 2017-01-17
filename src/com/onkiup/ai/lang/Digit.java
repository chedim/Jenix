package com.onkiup.ai.lang;

import com.onkiup.ai.ParamTest;
import com.onkiup.ai.Expression;
import com.onkiup.ai.Test;
import com.onkiup.ai.tests.ByteRange;

@Expression
public class Digit {

    public static final int CHAR_ZERO = 48;
    public static final int CHAR_NINE = 57;

    private byte value;

    public Digit(@ByteRange({CHAR_ZERO, CHAR_NINE}) Byte value) {
        this.value = (byte) (value - CHAR_ZERO);
    }

    public byte getValue() {
        return value;
    }
}
