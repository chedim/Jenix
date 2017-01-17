package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.Daria;
import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.AbstractBinaryOperator;
import com.onkiup.daria.parser.AbstractPrefixOperator;
import com.onkiup.daria.parser.Lexem;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;
import com.onkiup.daria.parser.UnknownLiteral;

@OperatorDefinition(
        priority = OperatorPriority.UNARY_HIGH,
        aliases = {"@"}
)
public class SubQueryOperator extends AbstractBinaryOperator<UnknownLiteral, GroupLexem, Lexem> implements Daria1Lexem {

    public String getTableName() {
        return getLeft().getLiteral();
    }

    @Override
    public Lexem evaluate() {
        return getRight().getGroup();
    }
}
