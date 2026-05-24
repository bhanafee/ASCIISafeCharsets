package com.maybeitssquid.safeascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for the {@link Categorize} class. */
class CategorizeTest extends AbstractChainableTest {

  @Override
  protected Categorize createProcessor() {
    return new Categorize();
  }

  @ValueSource(ints = {'A', 'Z', 'a', 'z', '0', '9', '\n', '\"', 1, 0x2605, 0x1F680})
  @ParameterizedTest
  void passthrough(final int codepoint) {
    testUnchanged(codepoint);
  }

  @ValueSource(ints = {'5', 0x0665, 0x06F5, 0x07C5, 0xA9D5, 0x1E955, 0x1FBF5})
  @ParameterizedTest
  void processDecimalDigitNumber(final int codepoint) {
    test(codepoint, "5");
  }

  @ValueSource(
      ints = {
        ' ', 0x00A0, 0x1680, 0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x02005, 0x2006, 0x2007,
        0x2008, 0x2009, 0x200A, 0x202F, 0x205F, 0x3000
      })
  @ParameterizedTest
  void processSpaceSeparator(final int codepoint) {
    test(codepoint, " ");
  }

  @ValueSource(ints = {'\n', 0x2028, 0x2029, Categorize.UNICODE_NEL})
  @ParameterizedTest
  void processLineParagraphSeparators(final int codepoint) {
    test(codepoint, System.lineSeparator());
  }

  @ValueSource(
      ints = {'-', 0x058A, 0x2010, 0x2011, 0x2012, 0x2013, 0x2014, 0x2015, 0xFE58, 0xFE63, 0xFF0D})
  @ParameterizedTest
  void processDashPunctuation(final int codepoint) {
    test(codepoint, "-");
  }

  @ValueSource(
      ints = {
        '(', 0x0F3A, 0x2045, 0x2329, 0x2768, 0x2774, 0x27E6, 0x2983, 0x298B, 0x298D, 0x298F, 0x301A,
        0xFE37, 0xFE47, 0xFE5B, 0xFF3B, 0xFF5B
      })
  @ParameterizedTest
  void processStartPunctuation(final int codepoint) {
    test(codepoint, "(");
  }

  @ValueSource(
      ints = {
        ')', 0x0F3B, 0x2046, 0x232A, 0x2769, 0x2775, 0x27E7, 0x2984, 0x298C, 0x298E, 0x2990, 0x301B,
        0xFE38, 0xFE48, 0xFE5C
      })
  @ParameterizedTest
  void processEndPunctuation(final int codepoint) {
    test(codepoint, ")");
  }

  @ValueSource(ints = {'_', 0x203F, 0x2040, 0x2054, 0xFE33, 0xFE34, 0xFE4D, 0xFE4E, 0xFE4F, 0xFF3F})
  @ParameterizedTest
  void processConnectorPunctuation(final int codepoint) {
    test(codepoint, "_");
  }

  @ValueSource(
      ints = {
        0x00AB, 0x2018, 0x201B, 0x201C, 0x201F, 0x2039, 0x2E02, 0x2E04, 0x2E09, 0x2E0C, 0x2E20
      })
  @ParameterizedTest
  void processInitialQuotePunctuation(final int codepoint) {
    test(codepoint, "\"");
  }

  @ValueSource(
      ints = {0x00BB, 0x2019, 0x201D, 0x203A, 0x2E03, 0x2E05, 0x2E0A, 0x2E0D, 0x2E1D, 0x2E21})
  @ParameterizedTest
  void processFinalQuotePunctuation(final int codepoint) {
    test(codepoint, "\"");
  }

  @Test
  void processOtherSymbolUnicodeReplacement() {
    test('\uFFFD', "?");
  }

  @Test
  void applyASCIIOptimization() {
    final String[] result = new String[] {"default"};
    Categorize categorize =
        new Categorize(
            value -> {
              result[0] = "optimized";
              return "optimized";
            });
    assertEquals("optimized", categorize.apply('A')); // ASCII character
    assertEquals("optimized", result[0]);
  }
}
