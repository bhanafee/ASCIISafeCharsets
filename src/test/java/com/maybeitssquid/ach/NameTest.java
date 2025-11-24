package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the {@link Name} class.
 */
public class NameTest extends AbstractChainableTest {

    @Override
    protected Chainable createProcessor() {
        return new Name();
    }

    @Test
    public void testProcessUppercaseLetter() {
        testUnchanged(0x0041);
    }

    @Test
    public void testProcessLowercaseLetter() {
        testUnchanged(0x0061);
    }

    @ValueSource(ints = {'[', 0x2045, 0x27E6, 0x298B, 0x298D, 0x298F, 0x301A, 0xFE47, 0xFF3B})
    @ParameterizedTest
    public void testProcessStartPunctuationSquareBracket(int codepoint) {
        test(codepoint, "[");
    }

    @ValueSource(ints = {'{', 0x2774, 0x2983, 0xFE37, 0xFE5B, 0xFF5B})
    @ParameterizedTest
    public void testProcessStartPunctuationCurlyBracket(int codepoint) {
        test(codepoint, "{");
    }

    @ValueSource(ints = {'<', 0x2329, 0x276C, 0x276E, 0x2770, 0x27E8, 0x27EA, 0x2991, 0x29FC, 0x3008, 0x300A, 0xFE3D, 0xFE3F})
    @ParameterizedTest
    public void testProcessStartPunctuationAngleBracket(int codepoint) {
        test(codepoint, "<");
    }

    @ValueSource(ints = {'(', 0x0F3A, 0x0F3C, 0x207D, 0x208D, 0x2768, 0x276A})
    @ParameterizedTest
    public void testProcessStartPunctuationParenthesis(int codepoint) {
        test(codepoint, "(");
    }

    @ValueSource(ints = {']', 0x2046, 0x27E7, 0x298C, 0x298E, 0x2990, 0x301B, 0xFE48, 0xFF3D})
    @ParameterizedTest
    public void testProcessEndPunctuationSquareBracket(int codepoint) {
        test(codepoint, "]");
    }

    @ValueSource(ints = {'}', 0x2775, 0x2984, 0xFE38, 0xFE5C, 0xFF5D})
    @ParameterizedTest
    public void testProcessEndPunctuationCurlyBracket(int codepoint) {
        test(codepoint, "}");
    }

    @ValueSource(ints = {'>', 0x232A, 0x276D, 0x276F, 0x2771, 0x27E9, 0x27EB, 0x2992, 0x29FD, 0x3009, 0x300B, 0xFE3E, 0xFE40})
    @ParameterizedTest
    public void testProcessEndPunctuationAngleBracket(int codepoint) {
        test(codepoint, ">");
    }

    @ValueSource(ints = {')', 0x0F3B, 0x0F3D, 0x207E, 0x208E, 0x2769, 0x276B})
    @ParameterizedTest
    public void testProcessEndPunctuationParenthesis(int codepoint) {
        test(codepoint, ")");
    }

    @Test
    public void testProcessQuotePunctuationDoubleQuote() {
        test(0x0022, "\"", "Expected quote punctuation '\"' to be returned unchanged.");
    }

    @Test
    public void testProcessModifierSymbolAmpersand() {
        testUnchanged(0x0026);
    }

    @Test
    public void testProcessUnicodeReplacementCharacter() {
        test(0xFFFD, "?");
    }

    @Test
    public void testProcessOtherSymbolReplacementCharacter() {
        testUnchanged(0x00A6);
    }

    @Test
    public void testProcessColonEqualsSpecialCase() {
        test(0x2254, ":=");
    }

    @Test
    public void testProcessEqualsColonSpecialCase() {
        test(0x2255, "=:");
    }

    @Test
    public void testProcessSlashExclusionRule() {
        testUnchanged(Name.UNICODE_CIRCLED_ZERO_WITH_SLASH);
    }

    @Test
    public void testProcessMathSymbolPlus() {
        testUnchanged(0x002B);
    }

    @Test
    public void testProcessUnknownCharacter() {
        testUnchanged(0x1F601);
    }
}