package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Decompose} class.
 * The {@code process} method normalizes a single Unicode codepoint based on the specified or default normalization form.
 */
class DecomposeTest extends AbstractChainableTest{

    private final IntFunction<CharSequence> delegate = Character::toString;

    @Override
    protected Decompose createProcessor() {
        return new Decompose(delegate);
    }

    @Test
    void test_NormalizedInput_ReturnsSameInput() {
        test('A', "A", "Expected normalized input 'A' to be returned unchanged.");
    }

    @Test
    void test_DefaultFormNFKD_HandlesComposedInputDiacritic() {
        final int composed = '\u00C5';      // 'Å'
        final String expected = "A\u030A";  // "A" + "ring above"
        test(composed, expected, "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
    }

    @Test
    void test_DefaultFormNFKD_HandlesInputLigature() {
        int ligature = '\uFB03'; // 'ﬃ' (Latin Small Ligature FFI)
        final String expected = "ffi";
        test(ligature, expected, "Expected ligature 'ﬃ' to be decomposed into 'f' 'f' and 'i'.");
    }

    @Test
    void test_DefaultFormNFKD_HandlesVulgarFraction() {
        int ligature = '\u00BC'; // '¼' (Vulgar Fraction One Quarter)
        final String expected = "1\u20444";  // U+2044 is the fraction slash.
        test(ligature, expected, "Expected fraction '¼' to be decomposed into '1' '⁄' and '4'.");
    }

    @Test
    void test_CanonicalFormNFD_ReturnsSameInput() {
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        test(decompose, 'b', "b", "Expected normalized input 'b' to be returned unchanged.");
    }

    @Test
    void test_CanonicalFormNFD_ReturnsSameInputLigature() {
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        final int codepoint = '\uFB03';   // Ligature 'Convert to Basic Latin'
        test(decompose, codepoint, "\uFB03", "Expected ligature 'ﬃ' to be returned unchanged.");
    }

    @Test
    void test_CanonicalFormNFD_HandlesComposedInputDiacritic() {
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        int codepoint = '\u00C5'; // 'Å' (Latin Capital Letter A WITH RING ABOVE)
        test(decompose, codepoint, "A\u030A", "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
    }

    @Test
    void testConstructor_FormNFC_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFC));
    }

    @Test
    void testConstructor_FormNFKC_ThrowsException() {
         assertThrows(IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFKC));
    }
}