package com.onkiup.daria.query.language.daria1;

import java.util.function.BinaryOperator;

import com.onkiup.daria.StorageColumn;
import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.AbstractBinaryOperator;
import com.onkiup.daria.parser.AbstractLexem;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;
import com.onkiup.daria.parser.UnknownLiteral;

@OperatorDefinition(
        aliases = {"."},
        priority = OperatorPriority.GROUPING,
        priorityShift = -1
)
public class MemberSelectionLexem extends AbstractBinaryOperator<LinkedTableLexem, UnknownLiteral, String> {

    public String getTableName() {
        return getLeft().evaluate();
    }

    @Override
    public String evaluate() {
        UnknownLiteral columnName = getRight();
        return columnName.getLiteral();
    }
}
