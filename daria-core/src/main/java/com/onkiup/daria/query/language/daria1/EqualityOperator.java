package com.onkiup.daria.query.language.daria1;

import java.util.Objects;

import com.onkiup.daria.parser.AbstractBinaryOperator;
import com.onkiup.daria.parser.Evaluatable;
import com.onkiup.daria.parser.Lexem;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        priority = OperatorPriority.COMPARATOR_LOW,
        aliases = {"=="}
)
public class EqualityOperator extends AbstractBinaryOperator<Evaluatable, Evaluatable, Boolean> implements Daria1Lexem {
    @Override
    public Boolean evaluate() {
        return Objects.equals(getLeft().evaluate(), getRight().evaluate());
    }
}
