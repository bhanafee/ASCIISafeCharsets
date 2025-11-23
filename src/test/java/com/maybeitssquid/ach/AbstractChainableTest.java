package com.maybeitssquid.ach;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract public class AbstractChainableTest {

    abstract protected Chainable createProcessor();

    protected void test(final int codepoint, final CharSequence expected, final String message) {
        test(createProcessor(), codepoint, expected, message);
    }

    protected void test(final Chainable processor, final int codepoint, final CharSequence expected, final String message) {
        final CharSequence result = processor.apply(codepoint);
        assertEquals(expected, result, message);
    }

}
