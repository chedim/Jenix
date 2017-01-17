package com.onkiup.daria.parser;

public enum OperatorPriority {
    LITERAL,
    ASSIGNMENT,
    INLINE_CONDITION,
    LOGICAL_LOW,
    LOGICAL_HIGH,
    BITWISE_LOW,
    BITWSE_MEDIUM,
    BITWISE_HIGH,
    COMPARATOR_LOW,
    BITSHIFT,
    MATH_LOW,
    MATH_HIGH,
    UNARY_LOW,
    UNARY_HIGH,
    GROUPING,
    COMPARATOR_HIGH,
    WHITESPACE;
    
    public Integer getValue() {
        return ordinal() * 10;
    }
}
