package com.onkiup.daria.parser;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import com.onkiup.jendri.util.OopUtils;

public abstract class UnknownLiteral<T> extends AbstractLexem implements Evaluatable<T> {

    private static final Set<UnknownLiteral> LITERALS = new HashSet<>();

    static {
        for (Class<? extends UnknownLiteral> literal : OopUtils.getSubClasses(UnknownLiteral.class)) {
            try {
                LITERALS.add(literal.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String literal;

    protected UnknownLiteral() {

    }

    protected UnknownLiteral(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    abstract boolean supports(String literal);

    public static <T extends UnknownLiteral> T parse(String value) {
        Class<T> resultType = null;
        for (UnknownLiteral literal : LITERALS) {
            if (literal.supports(value)) {
                if (resultType != null) {
                    throw new RuntimeException("Value " + value + " can be resolved to multiple types: " + resultType.getSimpleName() + ", " + literal.getClass().getSimpleName());
                }
                resultType = (Class<T>) literal.getClass();
            }
        }

        if (resultType == null) {
            throw new RuntimeException("Unable to resolve value: `" + value + "`");
        }

        try {
            Constructor<T> constructor = resultType.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate unknown literal " + resultType.getSimpleName(), e);
        }
    }

}
