package com.maybeitssquid.safeascii.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.Normalizer;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Decompose} class. The {@code process} method normalizes a single
 * Unicode codepoint based on the specified or default normalization form.
 */
class DecomposeTest extends AbstractChainableTest {

  private final IntFunction<CharSequence> delegate = Character::toString;

  @Override
  protected Decompose createProcessor() {
    return new Decompose(delegate);
  }

  @Test
  void normalized_input_returns_same_input() {
    test('A', "A", "Expected normalized input 'A' to be returned unchanged.");
  }

  @Test
  void default_form_nfkd_handles_composed_input_diacritic() {
    final int composed = '\u00C5'; // 'Å'
    final String expected = "A\u030A"; // "A" + "ring above"
    test(composed, expected, "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
  }

  @Test
  void default_form_nfkd_handles_input_ligature() {
    int ligature = '\uFB03'; // 'ﬃ' (Latin Small Ligature FFI)
    final String expected = "ffi";
    test(ligature, expected, "Expected ligature 'ﬃ' to be decomposed into 'f' 'f' and 'i'.");
  }

  @Test
  void default_form_nfkd_handles_vulgar_fraction() {
    int ligature = '\u00BC'; // '¼' (Vulgar Fraction One Quarter)
    final String expected = "1\u20444"; // U+2044 is the fraction slash.
    test(ligature, expected, "Expected fraction '¼' to be decomposed into '1' '⁄' and '4'.");
  }

  @Test
  void canonical_form_nfd_returns_same_input() {
    Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
    test(decompose, 'b', "b", "Expected normalized input 'b' to be returned unchanged.");
  }

  @Test
  void canonical_form_nfd_returns_same_input_ligature() {
    Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
    final int codepoint = '\uFB03'; // Ligature 'Convert to Basic Latin'
    test(decompose, codepoint, "\uFB03", "Expected ligature 'ﬃ' to be returned unchanged.");
  }

  @Test
  void canonical_form_nfd_handles_composed_input_diacritic() {
    Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
    int codepoint = '\u00C5'; // 'Å' (Latin Capital Letter A WITH RING ABOVE)
    test(
        decompose,
        codepoint,
        "A\u030A",
        "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
  }

  @Test
  void constructorFormNFCThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFC));
  }

  @Test
  void constructorFormNFKCThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFKC));
  }
}
