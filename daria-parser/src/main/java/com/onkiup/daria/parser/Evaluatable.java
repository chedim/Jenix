package com.onkiup.daria.parser;

public interface Evaluatable<RESULT> extends Lexem {
    RESULT evaluate();
}
