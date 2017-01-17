package com.onkiup.daria.parser;

public class IntegerLiteral extends UnknownLiteral<Integer>  {
    public IntegerLiteral() {
    }

    public IntegerLiteral(String literal) {
        super(literal);
    }

    @Override
    public Integer evaluate() {
        return Integer.valueOf(getLiteral());
    }

    @Override
    public boolean supports(String literal) {
        return literal.matches("^\\d+$");
    }

    @Override
    public int getPriority() {
        return OperatorPriority.LITERAL.getValue();
    }
}
