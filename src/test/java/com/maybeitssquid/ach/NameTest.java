package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for the {@link Name} class.
 */
class NameTest extends AbstractChainableTest {

    @Override
    protected Chainable createProcessor() {
        return new Name();
    }

    @ValueSource(ints = {0x0041, 0x00C0, 0x0100, 0x0102, 0x01E0, 0x1EB6, 0xFF21})
    @ParameterizedTest
    void processUppercaseLetter(final int codepoint) {
        test(codepoint, "A");
    }

    @Test
    void processLowercaseLetter() {
        testUnchanged(0x0061);
    }

    @Test
    void processTitlecaseLetter() {
        test(0x01C5, "Dz", "Expected titlecase letter 'ǅ' to be returned as 'Dz'.");
        test(0x01C8, "Lj", "Expected titlecase letter 'ǈ' to be returned as 'Lj'.");
        test(0x01CB, "Nj", "Expected titlecase letter 'ǋ to be returned as 'Nj'.");
        test(0x01F2, "Dz", "Expected titlecase letter 'ǅ' to be returned as 'Dz'.");
        test(0x1F88, "", "Expected titlecase letter 'ᾈ' to be returned as empty string.");
    }


    @ValueSource(ints = {'[', 0x2045, 0x27E6, 0x298B, 0x298D, 0x298F, 0x301A, 0xFE47, 0xFF3B})
    @ParameterizedTest
    void processStartPunctuationSquareBracket(int codepoint) {
        test(codepoint, "[");
    }

    @ValueSource(ints = {'{', 0x2774, 0x2983, 0xFE37, 0xFE5B, 0xFF5B})
    @ParameterizedTest
    void processStartPunctuationCurlyBracket(int codepoint) {
        test(codepoint, "{");
    }

    @ValueSource(ints = {'<', 0x2329, 0x276C, 0x276E, 0x2770, 0x27E8, 0x27EA, 0x2991, 0x29FC, 0x3008, 0x300A, 0xFE3D, 0xFE3F})
    @ParameterizedTest
    void processStartPunctuationAngleBracket(int codepoint) {
        test(codepoint, "<");
    }

    @ValueSource(ints = {'(', 0x0F3A, 0x0F3C, 0x207D, 0x208D, 0x2768, 0x276A})
    @ParameterizedTest
    void processStartPunctuationParenthesis(int codepoint) {
        test(codepoint, "(");
    }

    @ValueSource(ints = {']', 0x2046, 0x27E7, 0x298C, 0x298E, 0x2990, 0x301B, 0xFE48, 0xFF3D})
    @ParameterizedTest
    void processEndPunctuationSquareBracket(int codepoint) {
        test(codepoint, "]");
    }

    @ValueSource(ints = {'}', 0x2775, 0x2984, 0xFE38, 0xFE5C, 0xFF5D})
    @ParameterizedTest
    void processEndPunctuationCurlyBracket(int codepoint) {
        test(codepoint, "}");
    }

    @ValueSource(ints = {'>', 0x232A, 0x276D, 0x276F, 0x2771, 0x27E9, 0x27EB, 0x2992, 0x29FD, 0x3009, 0x300B, 0xFE3E, 0xFE40})
    @ParameterizedTest
    void processEndPunctuationAngleBracket(int codepoint) {
        test(codepoint, ">");
    }

    @ValueSource(ints = {')', 0x0F3B, 0x0F3D, 0x207E, 0x208E, 0x2769, 0x276B})
    @ParameterizedTest
    void processEndPunctuationParenthesis(int codepoint) {
        test(codepoint, ")");
    }

    @Test
    void processQuotePunctuationDoubleQuote() {
        test(0x0022, "\"", "Expected quote punctuation '\"' to be returned unchanged.");
    }

    @Test
    void processModifierSymbolAmpersand() {
        testUnchanged(0x0026);
    }

    @Test
    void processUnicodeReplacementCharacter() {
        test(0xFFFD, "?");
    }

    @ValueSource(ints = {0x24B6, 0x1F110, 0x1F130, 0x1F150, 0x1F170})
    @ParameterizedTest
    void processOtherSymbolUppercaseLetter(final int codepoint) {
        test(codepoint, "A");
    }

    @ValueSource(ints = {0x249C, 0x249C, 0x24D0, 0x2C65, 0xFF41})
    @ParameterizedTest
    void processOtherSymbolLowercaseLetter(final int codepoint) {
        test(codepoint, "a");
    }

    @Test
    void processOtherSymbolLowercaseSpecialLetter() {
        test(0xA733, "aa");
        test(0xA735, "ao");
        test(0xA737, "au");
        test(0xA739, "av");
        test(0xA73B, "av");
        test(0xA73D, "ay");
    }

    @Test
    void processOtherSymbolUppercaseSpecialLetter() {
        test(0xA732, "AA");
        test(0xA734, "AO");
        test(0xA736, "AU");
        test(0xA738, "AV");
        test(0xA73A, "AV");
        test(0xA73C, "AY");
    }

    @Test
    void processOtherSymbolReplacementCharacter() {
        testUnchanged(0x00A6);
    }

    @Test
    void processColonEqualsSpecialCase() {
        test(0x2254, ":=");
    }

    @Test
    void processEqualsColonSpecialCase() {
        test(0x2255, "=:");
    }

    @Test
    void processMathSymbolPlus() {
        testUnchanged(0x002B);
    }

    @Test
    void processUnknownCharacter() {
        testUnchanged(0x1F601);
    }
}