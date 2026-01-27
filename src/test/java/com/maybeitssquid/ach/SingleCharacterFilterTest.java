package com.maybeitssquid.ach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SingleCharacterFilter}. The apply method filters out results that are not
 * single characters, returning an empty string for inputs that resolve to multi-character or empty
 * outputs.
 */
class SingleCharacterFilterTest {

  @Test
  void applyWithSingleCharacterOutput() {
    // Arrange: A delegate that returns a single character for the given integer
    IntFunction<CharSequence> delegate = value -> "A";
    SingleCharacterFilter filter = new SingleCharacterFilter(delegate);

    // Act: Apply the filter with any integer (output is controlled by delegate)
    CharSequence result = filter.apply(42);

    // Assert: Expect the same single character to pass through
    assertEquals("A", result, "Expected single character result to pass through unchanged.");
  }

  @Test
  void applyWithEmptyOutput() {
    // Arrange: A delegate that returns an empty string
    IntFunction<CharSequence> delegate = value -> "";
    SingleCharacterFilter filter = new SingleCharacterFilter(delegate);

    // Act: Apply the filter with any integer
    CharSequence result = filter.apply(42);

    // Assert: Expect an empty result
    assertEquals("", result, "Expected empty result to remain unchanged.");
  }

  @Test
  void applyWithMultiCharacterOutput() {
    // Arrange: A delegate that returns multiple characters
    IntFunction<CharSequence> delegate = value -> "AB";
    SingleCharacterFilter filter = new SingleCharacterFilter(delegate);

    // Act: Apply the filter with any integer
    CharSequence result = filter.apply(42);

    // Assert: Expect an empty string since the output has more than one character
    assertEquals("", result, "Expected multi-character result to be filtered out.");
  }

  @Test
  void applyWithNullOutput() {
    // Arrange: A delegate that returns null
    IntFunction<CharSequence> delegate = value -> null;
    SingleCharacterFilter filter = new SingleCharacterFilter(delegate);

    // Act: Apply the filter with any integer
    CharSequence result = filter.apply(42);

    // Assert: Expect a NullPointerException or empty string (will depend on requirements)
    assertEquals("", result, "Expected null result to be treated as empty.");
  }
}
