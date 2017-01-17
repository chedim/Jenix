package com.onkiup.daria.parser;

public abstract class AbstractBinaryOperator<LEFT extends Lexem, RIGHT extends Lexem, RESULT> extends AbstractLexem implements Evaluatable<RESULT> {
    private LEFT left;
    private RIGHT right;

    @Override
    protected void grabOperands() throws SyntaxException {
        left = (LEFT) grabLeftOperand();
        right = (RIGHT) grabRightOperand();
    }

    public LEFT getLeft() {
        return left;
    }

    public RIGHT getRight() {
        return right;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getLeft() +", " + getRight() + "]";
    }
}
