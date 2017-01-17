package com.onkiup.daria.query.language.daria1;

import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.AbstractLexem;
import com.onkiup.daria.parser.OperatorDefinition;
import com.onkiup.daria.parser.OperatorPriority;
import com.onkiup.daria.parser.WhiteSpace;
import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

@OperatorDefinition(
        aliases = {" ", "\t", "\n"},
        priority = OperatorPriority.WHITESPACE
)
public class WhiteSpaceLexem extends AbstractLexem implements Daria1Lexem, WhiteSpace {
}
