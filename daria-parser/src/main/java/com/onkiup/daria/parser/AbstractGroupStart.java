package com.onkiup.daria.parser;

import com.onkiup.jendri.util.OopUtils;

public abstract class AbstractGroupStart<X extends AbstractGroupEnd> extends AbstractLexem implements Evaluatable {
    private Evaluatable group;

    @Override
    public Lexem pack() throws SyntaxException {
        if (!this.isPacked()) {
            this.setPacked(true);
            tabs += "\t";
            Class<X> endType = getEndLexem();
            Lexem current = getNext();
            while (current != null && !endType.isAssignableFrom(current.getClass())) {
                if (current instanceof AbstractGroupStart) {
                    ((AbstractGroupStart) current).pack();
                }
                current = current.getNext();
            }
            if (current == null) {
                throw new SyntaxException("Unclosed group");
            }

            X end = (X) current;
            if (end.getPrevious() != null) {
                end.getPrevious().setNext(null);
            }
            if (getNext() != null) {
                AbstractLexem next = (AbstractLexem) getNext();
                next.setPrevious(null);
                group = (Evaluatable) Parser.packGroup(next);
            }
            this.setNext(end.getNext());
            if (end.getNext() != null) {
                end.getNext().setPrevious(this);
            }
//            setPacked(false);
            tabs = tabs.substring(1);
            return this;
        }
        return null;
    }

    private Class<X> getEndLexem() {
        return OopUtils.getBoundClass(getClass(), AbstractGroupStart.class, "X");
    }

    public Evaluatable getGroup() {
        return group;
    }

    @Override
    public Object evaluate() {
        return group == null ? null : group.evaluate();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + group + "]";
    }
}
