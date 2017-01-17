package com.onkiup.daria.parser;

public class NameLiteral extends UnknownLiteral<String> {
    public NameLiteral() {
    }

    public NameLiteral(String literal) {
        super(literal);
    }

    @Override
    public String evaluate() {
        return getLiteral();
    }

    @Override
    boolean supports(String literal) {
        return literal.matches("^[^\\d][^\\s]*$");
    }
}
