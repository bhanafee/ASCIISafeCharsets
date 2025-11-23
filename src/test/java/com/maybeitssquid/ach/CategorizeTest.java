package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link Categorize} class.
 */
public class CategorizeTest extends AbstractChainableTest{
    @Override
    protected Categorize createProcessor() {
        return new Categorize();
    }

    @Test
    public void testProcess_DecimalDigitNumber() {
        test('5', "5", "Expected decimal digit '5' to be returned unchanged.");
        test('\uA9D5', "5", "Expected Javanese decimal digit '5' to be returned as '5'.");
    }

    @Test
    public void testProcess_SpaceSeparator() {
        test(' ', " ", "Expected SPACE to be returned unchanged.");
        test('\u2003', " ", "Expected EM SPACE to be returned as ' '.");
    }

    @Test
    public void testProcess_LineSeparator() {
        test('\n', "\n", "Expected LF to be returned unchanged.");
        test('\u2028', System.lineSeparator(), "Expected LINE SEPARATOR to be returned as system line separator.");
    }

    @Test
    public void testProcess_ParagraphSeparator() {
        test('\u2029', System.lineSeparator(), "Expected PARAGRAPH SEPARATOR to be returned as system line separator.");
    }

    @Test
    public void testProcess_Control_NEL() {
        test(Categorize.UNICODE_NEL, System.lineSeparator(), "Expected NEL to be returned as system line separator.");
    }

    @Test
    public void testProcess_Control_NotNEL() {
        test('\u0001', "\u0001", "Expected Non-NEL control character to be returned unchanged.");
    }

    @Test
    public void testProcess_DashPunctuation() {
        test('-', "-", "Expected DASH to be returned unchanged.");
        test('\u2014', "-", "Expected EM DASH to be returned as '-'.");
    }

    @Test
    public void testProcess_StartPunctuation() {
        test('(', "(", "Expected LEFT PARENTHESIS to be returned unchanged.");
        test('\u2768', "(", "Expected Unicode MEDIUM LEFT PARENTHESIS ORNAMENT to be returned as '('.");
    }

    @Test
    public void testProcess_EndPunctuation() {
        test(')', ")", "Expected RIGHT PARENTHESIS to be returned unchanged.");
        test('\u2769', ")", "Expected Unicode MEDIUM RIGHT PARENTHESIS ORNAMENT to be returned as '('.");
    }

    @Test
    public void testProcess_ConnectorPunctuation() {
        test('_', "_", "Expected UNDERSCORE to be returned unchanged.");
        test('\u203F', "_", "Expected Unicode UNDERTIE to be returned as '_'.");
    }

    @Test
    public void testProcess_InitialQuotePunctuation() {
        test('\"', "\"", "Expected quote character to be returned unchanged.");
        test('\u201C', "\"", "Expected Unicode LEFT DOUBLE QUOTATION MARK to be returned as '\"'");
    }

    @Test
    public void testProcess_FinalQuotePunctuation() {
        test('\u201D', "\"", "Expected Unicode RIGHT DOUBLE QUOTATION MARK to be returned as '\"'");
    }

    @Test
    public void testProcess_OtherSymbol_UnicodeReplacement() {
        test('\uFFFD', "?", "Expected Unicode REPLACEMENT CHARACTER to be returned as '?'.");
    }

    @Test
    public void testProcess_OtherSymbol_NotUnicodeReplacement() {
        test('\u2605', "\u2605", "Expected Unicode BLACK STAR to be returned unchanged.");
    }

    @Test
    public void testProcess_DefaultCase() {
        test(0x1F680, "\uD83D\uDE80", "Expected Unicode ROCKET to be returned as surrogate pair string.");
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