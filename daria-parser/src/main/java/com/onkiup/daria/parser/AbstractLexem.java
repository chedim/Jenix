package com.onkiup.daria.parser;

import static com.sun.tools.doclint.Entity.le;
import static com.sun.tools.doclint.Entity.ne;
import static com.sun.tools.doclint.Entity.nu;

public abstract class AbstractLexem implements Lexem {

    private Lexem previous;
    private Lexem next;
    private int priority;
    private boolean packed;

    protected static String tabs = "";

    public AbstractLexem() {
        OperatorDefinition annotation = getClass().getAnnotation(OperatorDefinition.class);
        if (annotation != null) {
            priority = annotation.priority().getValue() + annotation.priorityShift();
        }
    }

    @Override
    public final void setPrevious(Lexem previous) {
        this.previous = previous;
    }

    @Override
    public Lexem getPrevious() {
        return previous;
    }

    @Override
    public final void setNext(Lexem next) {
        this.next = next;
    }

    @Override
    public Lexem getNext() {
        return next;
    }

    protected void grabOperands() throws SyntaxException {

    }

    protected final Lexem grabRightOperand() throws SyntaxException {
        if (next == null) {
            throw new RuntimeException("Right operand for " + getClass().getSimpleName() + " is missing");
        }
        Lexem operand = next;
        this.next = operand.getNext();
        if (this.next != null) {
            this.next.setPrevious(this);
        }
        operand.setPrevious(null);
        operand.setNext(null);

        return operand;
    }

    protected final Lexem grabLeftOperand() throws SyntaxException {
        if (previous == null) {
            throw new RuntimeException("Left operand for " + getClass().getSimpleName() + " is missing");
        }
        Lexem operand = previous;
        this.previous = operand.getPrevious();
        if (this.previous != null) {
            this.previous.setNext(this);
        }
        operand.setNext(null);
        operand.setPrevious(null);
        return operand;
    }

    public Lexem pack() throws SyntaxException {
        if (!packed) {
            packed = true;
            tabs += "\t";
            grabOperands();
            tabs = tabs.substring(1);
            return this;
        }
        return this;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + (this instanceof Evaluatable ? "[" + ((Evaluatable) this).evaluate() + "]" : "");
    }
}
