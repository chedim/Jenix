package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.AbstractPrefixOperator;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;
import com.onkiup.daria.parser.UnknownLiteral;

@OperatorDefinition(
        aliases = {"~"},
        priority = OperatorPriority.UNARY_LOW
)
public class LinkedTableLexem extends AbstractPrefixOperator<UnknownLiteral, String> {

    @Override
    public String evaluate() {
        UnknownLiteral tableNameOperand = getOperand();
        return tableNameOperand.getLiteral();
    }
}
