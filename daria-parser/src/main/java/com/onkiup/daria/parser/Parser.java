package com.onkiup.daria.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.ForwardingCache;
import com.onkiup.jendri.util.OopUtils;
import javassist.compiler.Lex;

public class Parser<T extends Lexem> {
    private OperatorInformation[] operators;

    private Cache<String, Lexem> builtItems = CacheBuilder.newBuilder().build();

    public Parser(Class<T> baseType) {
        List<Class<? extends T>> subs = new ArrayList<>(OopUtils.getSubClasses(baseType));
        operators = new OperatorInformation[subs.size()];
        for (int i = 0; i < operators.length; i++) {
            operators[i] = new OperatorInformation(subs.get(i));
        }
    }

    public Lexem compile(String statement) throws SyntaxException {
        Lexem result = builtItems.getIfPresent(statement);
        if (result == null) {
            final BuildingLiteral[] buildingLexem = {new BuildingLiteral(operators)};
            final Lexem[] prev = new Lexem[1];
            List<Lexem> lexems = Stream.concat(
                    statement.chars()
                            .filter(buildingLexem[0]::append)
                            .boxed()
                            .flatMap(buildingLexem[0]::getMatch)
                            .map(i -> {
                                if (i.lexem != null) {
                                    i.lexem.setPrevious(prev[0]);
                                }
                                if (prev[0] != null) {
                                    prev[0].setNext(i.lexem);
                                }
                                prev[0] = i.lexem;
                                return i;
                            }),
                    IntStream.range(0, 1).boxed().flatMap(buildingLexem[0]::finish)
                            .map(o -> {
                                prev[0].setNext(o.lexem);
                                o.lexem.setPrevious(prev[0]);
                                return o;
                            }))
//                .map(o -> o)
                    .map(o -> o.lexem)
                    .collect(Collectors.toList());

            if (lexems.size() == 0) {
                return null;
            }
            Lexem first = lexems.get(0);
            result = packGroup(first);
            builtItems.put(statement, result);
        }
        return result;
    }

    public static Lexem packGroup(Lexem firstLexem) throws SyntaxException {
        Lexem maxPriority = maxPriorityBoth(firstLexem), last = null;
        while (maxPriority != null) {
            maxPriority.pack();
            last = maxPriority;
            maxPriority = maxPriorityBoth(maxPriority, maxPriority.getPriority());
        }
        return last;
    }

    static class LexemInfo implements Comparable<LexemInfo> {
        int priority;
        Lexem lexem;

        public LexemInfo() {
        }

        public LexemInfo(int priority, Lexem lexem) {
            this.priority = priority;
            this.lexem = lexem;
        }

        @Override
        public int compareTo(LexemInfo o) {
            return o.priority - priority;
        }
    }

    static class OperatorInformation implements Comparable<OperatorInformation> {
        char[][] aliases;
        int priority;
        Class<? extends Lexem> operator;

        public OperatorInformation(Class<? extends Lexem> operator) {
            this.operator = operator;
            OperatorDefinition ann = operator.getAnnotation(OperatorDefinition.class);
            if (ann == null) {
                throw new RuntimeException("Operator does not have a proper @OperatorDefinition info: " + operator.getName());
            }
            aliases = new char[ann.aliases().length][];
            for (int i = 0; i < ann.aliases().length; i++) {
                String alias = ann.aliases()[i];
                aliases[i] = new char[alias.length()];
                alias.getChars(0, alias.length(), aliases[i], 0);
            }
            priority = ann.priority().getValue() + ann.priorityShift();
        }

        @Override
        public int compareTo(OperatorInformation o) {
            return priority - o.priority;
        }
    }

    static class OperatorPosition {
        int index;
        String operatorName;
        Class<? extends AbstractLexem> operator;

        public OperatorPosition(int index, String operatorName, Class<? extends AbstractLexem> operator) {
            this.index = index;
            this.operatorName = operatorName;
            this.operator = operator;
        }
    }

    private static Lexem maxPriorityBoth(Lexem lexem) throws SyntaxException {
        return maxPriorityBoth(lexem, Integer.MAX_VALUE);
    }

    private static Lexem maxPriorityBoth(Lexem lexem, int maxValue) throws SyntaxException {
        Lexem left =  maxPriorityLeft(lexem, maxValue);
        Lexem right = maxPriorityRight(lexem, maxValue);
        if (left != null && (right == null || left.getPriority() >= right.getPriority())) {
            return left;
        }

        return right;
    }

    public static Lexem maxPriorityRight(Lexem lexem, int ceiling) throws SyntaxException {
        int max = Integer.MIN_VALUE;
        Lexem result = null, current = lexem.getNext();
        while (current != null) {
            if (current.getPriority() > max && current.getPriority() <= ceiling && !current.isPacked()) {
                result = current;
                max = current.getPriority();
            }
            current = current.getNext();
        }

        return result;
    }

    public static Lexem maxPriorityLeft(Lexem lexem, int ceiling) throws SyntaxException {
        int max = Integer.MIN_VALUE;
        Lexem result = null, current = lexem.getPrevious();
        while (current != null) {
            if (current.getPriority() > max && current.getPriority() <= ceiling && !current.isPacked()) {
                result = current;
                max = current.getPriority();
            }
            current = current.getPrevious();
        }
        return result;
    }

}
