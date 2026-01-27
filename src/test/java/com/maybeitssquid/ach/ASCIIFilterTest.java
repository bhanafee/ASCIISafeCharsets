package com.maybeitssquid.ach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ASCIIFilterTest {

  private static final int ASCII_LIMIT = 128;

  @Test
  @DisplayName("Default constructor should allow all ASCII characters (0-127)")
  void allowAllAsciiByDefault() {
    ASCIIFilter filter = new ASCIIFilter();

    for (int i = 0; i < ASCII_LIMIT; i++) {
      String input = Character.toString(i);
      CharSequence result = filter.apply(i);

      assertEquals(input, result.toString(), "Expected ASCII character " + i + " to be preserved");
    }
  }

  @Test
  @DisplayName("Filter should reject characters >= 128 regardless of configuration")
  void rejectNonAscii() {
    ASCIIFilter filter = new ASCIIFilter();

    // Test boundary and a few extended characters
    int[] testPoints = {128, 129, 255, 0x1F600}; // 128, 129, 255, Emoji

    for (int codepoint : testPoints) {
      CharSequence result = filter.apply(codepoint);
      assertEquals(
          "",
          result.toString(),
          "Expected non-ASCII character " + codepoint + " to be filtered out");
    }
  }

  @Test
  @DisplayName("Constructor with specific categories should block those characters")
  void blockSpecificCategories() {
    // Block ASCII Control characters (0-31, 127)
    ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);

    // Test a blocked control character (Newline)
    assertEquals("", filter.apply('\n').toString(), "Newline should be blocked");

    // Test a blocked control character (Delete)
    assertEquals("", filter.apply(127).toString(), "Delete (127) should be blocked");

    // Test an allowed character (Space is usually SPACE_SEPARATOR, not CONTROL, check specific
    // mappings)
    // 0x20 (Space) type is 12 (SPACE_SEPARATOR) or similar, usually not CONTROL (15).
    assertEquals(" ", filter.apply(' ').toString(), "Space should be allowed");

    // Test an allowed letter
    assertEquals("A", filter.apply('A').toString(), "Letter 'A' should be allowed");
  }

  @Test
  @DisplayName("Constructor should handle blocking multiple categories")
  void blockMultipleCategories() {
    // Block Digits and Uppercase Letters
    ASCIIFilter filter =
        new ASCIIFilter(Character.DECIMAL_DIGIT_NUMBER, Character.UPPERCASE_LETTER);

    assertEquals("", filter.apply('1').toString(), "Digit '1' should be blocked");
    assertEquals("", filter.apply('Z').toString(), "Uppercase 'Z' should be blocked");
    assertEquals("z", filter.apply('z').toString(), "Lowercase 'z' should be allowed");
    assertEquals("$", filter.apply('$').toString(), "Symbol '$' should be allowed");
  }

  @Test
  @DisplayName("Constructor handles null input gracefully by blocking nothing")
  void constructorNull() {
    ASCIIFilter filter = new ASCIIFilter((byte[]) null);
    assertEquals("A", filter.apply('A').toString());
  }

  @Test
  @DisplayName("Constructor handles empty array gracefully by blocking nothing")
  void constructorEmpty() {
    ASCIIFilter filter = new ASCIIFilter();
    assertEquals("A", filter.apply('A').toString());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 10, 127})
  @DisplayName("Boundary testing for ASCII range")
  void boundaries(int codepoint) {
    ASCIIFilter filter = new ASCIIFilter(); // No blocks
    assertEquals(Character.toString(codepoint), filter.apply(codepoint).toString());
  }
}
