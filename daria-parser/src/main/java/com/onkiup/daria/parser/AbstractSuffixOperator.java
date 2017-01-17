package com.onkiup.daria.parser;

public abstract class AbstractSuffixOperator<OPERAND extends Lexem, RESULT> extends AbstractLexem implements Evaluatable<RESULT> {
    private OPERAND operand;

    @Override
    protected void grabOperands() throws SyntaxException {
        operand = (OPERAND) grabLeftOperand();
    }

    public OPERAND getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getOperand() + "]";
    }
}
