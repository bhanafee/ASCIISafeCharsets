package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link Chainable} class with a focus on its {@link Chainable#apply(int)} method.
 * This tests the core functionality of chaining character transformations using custom processing logic.
 */
class ChainableTest {

    static class UppercaseChainable extends Chainable {
        public UppercaseChainable(IntFunction<CharSequence> delegate) {
            super(delegate);
        }

        @Override
        protected CharSequence process(int codepoint) {
            return Character.isLetter(codepoint) ? Character.toString(codepoint).toUpperCase() : Character.toString(codepoint);
        }
    }

    static class DoublingChainable extends Chainable {
        public DoublingChainable(IntFunction<CharSequence> delegate) {
            super(delegate);
        }

        @Override
        protected CharSequence process(int codepoint) {
            return new StringBuilder().appendCodePoint(codepoint).appendCodePoint(codepoint).toString();
        }
    }

    @Test
    void testApplyWithEmptyProcessResult() {
        Chainable chainable = new Chainable(delegate -> "ignored") {
            @Override
            protected CharSequence process(int codepoint) {
                return ""; // test case for empty String
            }
        };

        CharSequence result = chainable.apply('a'); // Pass any codepoint
        assertEquals("", result, "Expected empty result when process method returns an empty string");
    }

    @Test
    void testApplyWithSingleCharacterResult() {
        Chainable chainable = new Chainable(delegate -> "X") {
            @Override
            protected CharSequence process(int codepoint) {
                return "A"; // Single character result
            }
        };

        CharSequence result = chainable.apply('b');
        assertEquals("X", result, "Expected delegate output to be returned for single-character process result");
    }

    @Test
    void testApplyWithMultipleCharacterResult() {
        Chainable chainable = new Chainable(codepoint -> String.valueOf((char) (codepoint + 1))) {
            @Override
            protected CharSequence process(int codepoint) {
                return "ab"; // Multiple characters to chain through delegate
            }
        };

        CharSequence result = chainable.apply('x');
        assertEquals("bc", result, "Expected delegated transformation for each character in the processed result");
    }

    @Test
    void testApplyWithUppercaseChainable() {
        Chainable chainable = new UppercaseChainable(codepoint -> String.valueOf((char) codepoint));

        CharSequence result = chainable.apply('a');
        assertEquals("A", result, "Expected uppercase transformation for input lowercase letter");
    }

    @Test
    void testApplyWithMultipleDelegates() {
        Chainable doublingChainable = new DoublingChainable(Character::toString);
        Chainable uppercaseChainable = new UppercaseChainable(doublingChainable);

        CharSequence result = uppercaseChainable.apply('x');
        assertEquals("XX", result, "Expected uppercase transformation followed by doubling processing");
    }

    @Test
    void testApplyWithUnicodeCodePoints() {
        Chainable unicodeChainable = new Chainable(Character::toString) {
            @Override
            protected CharSequence process(int codepoint) {
                return "\uD83D\uDE00"; // Smiling face (requires surrogate pair handling)
            }
        };

        CharSequence result = unicodeChainable.apply('a');
        assertEquals("\uD83D\uDE00", result, "Expected surrogate pair to be handled correctly in buffering");
    }
}