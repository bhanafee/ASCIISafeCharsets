package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link Categorize} class.
 */
public class CategorizeTest extends AbstractChainableTest{

    @Override
    protected Categorize createProcessor() {
        return new Categorize();
    }

    @ValueSource(ints = { 'A', 'Z', 'a', 'z', '0', '9', '\n', 1, 0x2605, 0x1F680 })
    @ParameterizedTest
    public void testPassthrough(final int codepoint) {
        testUnchanged(codepoint);
    }

    @ValueSource(ints = { '5', 0x0665, 0x06F5, 0x07C5, 0xA9D5, 0x1E955, 0x1FBF5})
    @ParameterizedTest
    public void testProcess_DecimalDigitNumber(final int codepoint) {
        test(codepoint, "5");
    }

    @ValueSource(ints = { ' ', 0x00A0, 0x1680, 0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x02005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A, 0x202F, 0x205F, 0x3000})
    @ParameterizedTest
    public void testProcess_SpaceSeparator(final int codepoint) {
        test(codepoint, " ");
    }

    @ValueSource(ints = { '\n', 0x2028, 0x2029, Categorize.UNICODE_NEL})
    @ParameterizedTest
    public void testProcess_LineSeparators(final int codepoint) {
        test(codepoint, System.lineSeparator());
    }

    @ValueSource(ints = { '-', 0x058A, 0x2010, 0x2011, 0x2012, 0x2013, 0x2014, 0x2015, 0xFE58, 0xFE63, 0xFF0D })
    @ParameterizedTest
    public void testProcess_DashPunctuation(final int codepoint) {
        test(codepoint, "-");
    }

    @ValueSource(ints={'(', 0x0F3A, 0x2045, 0x2329, 0x2768, 0x2774, 0x27E6, 0x2983, 0x298B, 0x298D, 0x298F, 0x301A, 0xFE37, 0xFE47, 0xFE5B, 0xFF3B, 0xFF5B})
    @ParameterizedTest
    public void testProcess_StartPunctuation(final int codepoint) {
        test(codepoint, "(");
    }

    @ValueSource(ints={')', 0x0F3B, 0x2046, 0x232A, 0x2769, 0x2775, 0x27E7, 0x2984, 0x298C, 0x298E, 0x2990, 0x301B, 0xFE38, 0xFE48, 0xFE5C})
    @ParameterizedTest
    public void testProcess_EndPunctuation(final int codepoint) {
        test(codepoint, ")");
     }

    @ValueSource(ints={'_', 0x203F, 0x2040, 0x2054, 0xFE33, 0xFE34, 0xFE4D, 0xFE4E, 0xFE4F, 0xFF3F})
    @ParameterizedTest
    public void testProcess_ConnectorPunctuation(final int codepoint) {
        test(codepoint, "_");
    }

    @ValueSource(ints={'\"', 0x201C})
    @ParameterizedTest
    public void testProcess_InitialQuotePunctuation(final int codepoint) {
        test(codepoint, "\"");
    }

    @Test
    public void testProcess_FinalQuotePunctuation() {
        test('\u201D', "\"", "Expected Unicode RIGHT DOUBLE QUOTATION MARK to be returned as '\"'");
    }

    @Test
    public void testProcess_OtherSymbol_UnicodeReplacement() {
        test('\uFFFD', "?");
    }

    @Test
    public void testApply_ASCII_Optimization() {
        final String[] result = new String[]{"default"};
        Categorize categorize = new Categorize(value -> {
            result[0] = "optimized";
            return "optimized";
        });
        assertEquals("optimized", categorize.apply('A')); // ASCII character
        assertEquals("optimized", result[0]);
    }
}