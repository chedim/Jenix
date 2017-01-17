package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.parser.AbstractPrefixOperator;
import com.onkiup.daria.parser.Evaluatable;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        aliases = {"!"},
        priority = OperatorPriority.UNARY_LOW
)
public class NegationLexem extends AbstractPrefixOperator<Evaluatable<Boolean>, Boolean> implements Daria1Lexem {
    @Override
    public Boolean evaluate() {
        return !getOperand().evaluate();
    }
}
