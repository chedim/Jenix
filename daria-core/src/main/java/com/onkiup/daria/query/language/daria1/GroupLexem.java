package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.parser.AbstractGroupStart;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        aliases = {"("},
        priority = OperatorPriority.GROUPING
)
public class GroupLexem extends AbstractGroupStart<GroupCloseLexem> implements Daria1Lexem {
}
