package com.onkiup.daria.parser;

import java.util.Objects;

import static org.junit.Assert.*;
import org.junit.Test;

public class ParserTest {

    public interface TestLexem extends Lexem {

    }

    @OperatorDefinition(
            aliases = "==",
            priority = OperatorPriority.COMPARATOR_LOW
    )
    public static class EqualsOperator extends AbstractBinaryOperator<Evaluatable, Evaluatable, Boolean> implements TestLexem {

        @Override
        public Boolean evaluate() {
            return Objects.equals(getLeft().evaluate(), getRight().evaluate());
        }
    }

    @OperatorDefinition(
            aliases = "+",
            priority = OperatorPriority.MATH_LOW
    )
    public static class SumOperator extends AbstractBinaryOperator<Evaluatable<Integer>, Evaluatable<Integer>, Integer> implements TestLexem {

        @Override
        public Integer evaluate() {
            return getLeft().evaluate() + getRight().evaluate();
        }
    }

    @OperatorDefinition(
            aliases = {" ", "\t"},
            priority = OperatorPriority.WHITESPACE
    )
    public static class WhiteSpace extends AbstractLexem implements TestLexem, com.onkiup.daria.parser.WhiteSpace {

    }

    @OperatorDefinition(
            aliases = {")"},
            priority = OperatorPriority.GROUPING
    )
    public static class GroupEnd extends AbstractGroupEnd<GroupOpen> implements TestLexem {

    }

    @OperatorDefinition(
            aliases = {"("},
            priority = OperatorPriority.GROUPING
    )
    public static class GroupOpen extends AbstractGroupStart<GroupEnd> implements TestLexem {

    }

    @OperatorDefinition(
            aliases = {"*"},
            priority = OperatorPriority.MATH_HIGH
    )
    public static class Multiply extends AbstractBinaryOperator<Evaluatable<Integer>, Evaluatable<Integer>, Integer> implements TestLexem {

        @Override
        public Integer evaluate() {
            return getLeft().evaluate() * getRight().evaluate();
        }
    }

    @OperatorDefinition(
            aliases = {"++"},
            priority = OperatorPriority.UNARY_LOW
    )
    public static class Increment extends AbstractPrefixOperator<Evaluatable<Integer>, Integer> implements TestLexem {
        @Override
        public Integer evaluate() {
            return getOperand().evaluate() + 1;
        }
    }

    private static final Parser parser = new Parser(TestLexem.class);

    @Test
    public void compile() throws Exception {
        Lexem result = parser.compile("++25==++1+2*(3+4+5)");
        assert result instanceof EqualsOperator;
        EqualsOperator equals = (EqualsOperator) result;
        assertEquals(26, equals.getLeft().evaluate());
        assertEquals(26, equals.getRight().evaluate());
        assert equals.evaluate();
    }


    @Test
    public void recompile() throws Exception {
        compile();
    }
}