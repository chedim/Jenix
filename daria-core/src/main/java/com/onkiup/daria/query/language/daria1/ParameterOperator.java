package com.onkiup.daria.query.language.daria1;

import java.util.function.Supplier;

import com.onkiup.daria.parser.AbstractLexem;
import com.onkiup.daria.parser.Evaluatable;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        priority = OperatorPriority.LITERAL,
        aliases = {"?"}
)
public class ParameterOperator<X> extends AbstractLexem implements Daria1Lexem, Evaluatable<X> {

    private X value;

    public void setValue(Supplier<X> supplier) {
        value = supplier.get();
    }

    public void setValue(X value) {
        this.value = value;
    }

    @Override
    public X evaluate() {
        return value;
    }
}
