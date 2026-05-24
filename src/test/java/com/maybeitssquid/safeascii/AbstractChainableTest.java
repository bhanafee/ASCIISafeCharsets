package com.maybeitssquid.safeascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractChainableTest {

  protected abstract Chainable createProcessor();

  protected void testUnchanged(final int codepoint) {
    test(codepoint, Character.toString(codepoint));
  }

  protected void test(final int codepoint, final CharSequence expected) {
    final String message =
        "Expected %s (%04X) to be returned as '%s'."
            .formatted(Character.getName(codepoint), codepoint, expected);
    test(codepoint, expected, message);
  }

  protected void test(final int codepoint, final CharSequence expected, final String message) {
    test(createProcessor(), codepoint, expected, message);
  }

  protected void test(
      final Chainable processor,
      final int codepoint,
      final CharSequence expected,
      final String message) {
    final CharSequence result = processor.apply(codepoint);
    assertEquals(expected, result, message);
  }
}
