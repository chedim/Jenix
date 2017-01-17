package com.onkiup.ai.lang;

import com.onkiup.ai.Expression;

@Expression
public class Integer {
    private int value;
    public Integer(Digit... digits) {
        int pow = digits.length;
        for (Digit digit : digits) {
            value += digit.getValue() * Math.pow(10, pow--);
        }
    }

    public int getValue() {
        return value;
    }
}
