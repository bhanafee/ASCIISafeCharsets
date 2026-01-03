package com.maybeitssquid.ach;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract public class AbstractChainableTest {

    abstract protected Chainable createProcessor();

    protected void testUnchanged(final int codepoint) {
        test(codepoint, Character.toString(codepoint));
    }

    protected void test(final int codepoint, final CharSequence expected) {
        final String message = String.format("Expected %s (%04X) to be returned as '%s'.", Character.getName(codepoint), codepoint, expected);
        test(codepoint, expected, message);
    }

    protected void test(final int codepoint, final CharSequence expected, final String message) {
        test(createProcessor(), codepoint, expected, message);
    }

    protected void test(final Chainable processor, final int codepoint, final CharSequence expected, final String message) {
        final CharSequence result = processor.apply(codepoint);
        assertEquals(expected, result, message);
    }

}
