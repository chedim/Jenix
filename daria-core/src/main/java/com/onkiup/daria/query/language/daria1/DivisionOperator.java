package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.parser.AbstractBinaryOperator;
import com.onkiup.daria.parser.Evaluatable;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        aliases = {"/"},
        priority = OperatorPriority.MATH_HIGH
)
public class DivisionOperator extends AbstractBinaryOperator<Evaluatable<Integer>, Evaluatable<Integer>, Integer> implements Daria1Lexem {
    @Override
    public Integer evaluate() {
        return getLeft().evaluate() / getRight().evaluate();
    }
}
