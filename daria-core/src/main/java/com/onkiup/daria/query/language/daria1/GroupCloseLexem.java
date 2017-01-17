package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.AbstractGroupEnd;
import com.onkiup.daria.parser.AbstractLexem;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;

@OperatorDefinition(
        aliases = {")"},
        priority = OperatorPriority.GROUPING
)
public class GroupCloseLexem extends AbstractGroupEnd implements Daria1Lexem {

}
