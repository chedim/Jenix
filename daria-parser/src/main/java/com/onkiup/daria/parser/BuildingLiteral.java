package com.onkiup.daria.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import static com.sun.tools.doclint.Entity.le;
import static com.sun.tools.doclint.Entity.nu;
import javassist.compiler.Lex;

public class BuildingLiteral extends AbstractLexem {
    private MatchBuilder builder = new MatchBuilder();
    private String literal;
    private Parser.OperatorInformation[] operators;
    private List<Parser.LexemInfo> match = new ArrayList<>();
    private HashMap<char[], Parser.OperatorInformation> previousMatches;

    public BuildingLiteral(Parser.OperatorInformation[] operators) {
        this.operators = operators;
    }

    public String getLiteral() {
        return literal;
    }

    private Comparator<MatchInfo> comparator = (o1, o2) -> o2.matchLen - o1.matchLen;

    public boolean append(int c) {
        builder.append(c);
        List<MatchInfo> matches = new ArrayList<>();
        boolean itemsNotFound = false, first = true;
        String literal = "";
        while (!itemsNotFound && builder.length() > 0) {
            for (Parser.OperatorInformation operator : operators) {
                for (char[] alias : operator.aliases) {
                    int matchLen = builder.test(alias);
                    if (matchLen > 0 && (first || matchLen == alias.length)) {
                        matches.add(new MatchInfo(alias, matchLen, operator));
                    }
                }
            }
            final int[] count = {0};
            boolean finalFirst = first;
            MatchInfo best = matches.stream()
                    .filter(i -> finalFirst || i.alias.length == i.matchLen)
                    .map(i -> {
                        count[0]++;
                        return i;
                    })
                    .sorted(comparator)
                    .findFirst().orElse(null);
            if (!(itemsNotFound = best == null || ((count[0] != 1 || best.matchLen != best.alias.length) && first))) {
                if (literal.length() != 0) {
                    match.add(0, new Parser.LexemInfo(-1, UnknownLiteral.parse(literal)));
                    literal = "";
                }
                try {
                    match.add(0, new Parser.LexemInfo(best.operator.priority, best.operator.operator.newInstance()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                builder.dropRight(best.alias.length);
            } else if (!first && builder.length() > 0) {
                // no items found at middle round
                literal = String.valueOf(builder.dropRight(1)) + literal;
                itemsNotFound = false;
            }
            first = false;
            matches.clear();
        }

        if (literal.length() != 0) {
            match.add(0, new Parser.LexemInfo(-1, UnknownLiteral.parse(literal)));
        }

        if (match.size() > 0) {
            builder.clear();
            return true;
        }

        return false;
    }

    public Stream<Parser.LexemInfo> finish(int i) {
        literal = builder.toString();
        if (literal == null || literal.length() == 0) {
            return Stream.empty();
        }
        return Stream.of(new Parser.LexemInfo(-1, UnknownLiteral.parse(literal)));
    }

    public Stream<Parser.LexemInfo> getMatch(int c) {
        try {
            Stream<Parser.LexemInfo> result = Stream.empty();
            if (literal != null && literal.length() > 0) {
                result = Stream.concat(result, Stream.of(new Parser.LexemInfo(-1, UnknownLiteral.parse(literal))));
                literal = null;
            }
            if (match.size() > 0) {
                result = Stream.concat(result, match.stream());
                match = new ArrayList<>();
            }
            return result.filter(i -> !(i.lexem instanceof WhiteSpace));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class MatchInfo {
        private char[] alias;
        private int matchLen;
        private Parser.OperatorInformation operator;

        public MatchInfo(char[] alias, int matchLen, Parser.OperatorInformation operator) {
            this.alias = alias;
            this.matchLen = matchLen;
            this.operator = operator;
        }
    }
}
