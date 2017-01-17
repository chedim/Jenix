package com.onkiup.daria.query.language;

import com.onkiup.daria.StorageTable;
import com.onkiup.daria.parser.Lexem;
import com.onkiup.daria.parser.Parser;
import com.onkiup.daria.parser.SyntaxException;
import com.onkiup.daria.query.language.daria1.Daria1Lexem;
import com.onkiup.jendri.service.ServiceStub;

public interface QueryParser extends ServiceStub {

    class Implementation implements QueryParser {

        private static final Parser<Daria1Lexem> PARSER = new Parser<Daria1Lexem>(Daria1Lexem.class);

        @Override
        public void start() throws Exception {

        }

        @Override
        public void stop() throws Exception {

        }

        @Override
        public Lexem parse(String statement) throws SyntaxException {
            return PARSER.compile(statement);
        }
    }

    Lexem parse(String statement) throws SyntaxException;
}
