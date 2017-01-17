package com.onkiup.daria.parser;

import com.onkiup.jendri.util.OopUtils;
import static com.sun.tools.doclint.Entity.ne;

public abstract class AbstractGroupEnd<X extends AbstractGroupStart> extends AbstractLexem {
    @Override
    public Lexem pack() throws SyntaxException {
        if (!this.isPacked()) {
            this.setPacked(true);
            Class<X> startType = getStartLexem();
            Lexem current = getPrevious();
            while (current != null && !startType.isAssignableFrom(current.getClass())) {
                if (current instanceof AbstractGroupEnd) {
                    ((AbstractGroupStart) current).pack();
                }
                current = current.getPrevious();
            }
            if (current == null) {
                throw new SyntaxException("Unclosed group");
            }

            X start = (X) current;
            start.pack();
            return start;
        }
        return null;
    }

    private Class<X> getStartLexem() {
        return OopUtils.getBoundClass(getClass(), AbstractGroupEnd.class, "X");
    }
}
