package com.onkiup.daria.parser;

import java.util.stream.Stream;

public interface Lexem {
    int getPriority();
    Lexem getPrevious();
    Lexem getNext();

    void setPrevious(Lexem previous);
    void setNext(Lexem next);

    Lexem pack() throws SyntaxException;

    boolean isPacked();
}
