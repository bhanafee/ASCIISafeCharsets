package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Decompose} class.
 * The {@code process} method normalizes a single Unicode codepoint based on the specified or default normalization form.
 */
class DecomposeTest {

    private final IntFunction<CharSequence> delegate = Character::toString;

    @Test
    void test_NormalizedInput_ReturnsSameInput() {
        // Arrange
        Decompose decompose = new Decompose(delegate);
        int codepoint = 'A'; // 'A' is already normalized

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("A", result, "Expected normalized input to be returned unchanged.");
    }

    @Test
    void test_DefaultFormNFKD_HandlesComposedInputDiacritic() {
        // Arrange
        Decompose decompose = new Decompose(delegate);
        int codepoint = '\u00C5'; // 'Å' (Latin Capital Letter A WITH RING ABOVE)

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("A\u030A", result, "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
    }

    @Test
    void test_DefaultFormNFKD_HandlesInputLigature() {
        // Arrange
        Decompose decompose = new Decompose(delegate);
        int codepoint = '\uFB03'; // 'ﬃ' (Latin Small Ligature FFI)

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("ffi", result, "Expected ligature 'ﬃ' to be decomposed into 'f' 'f' and 'i'.");
    }

    @Test
    void test_CanonicalFormNFD_ReturnsSameInput() {
        // Arrange
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        int codepoint = 'b'; // 'b' is already normalized

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("b", result, "Expected normalized input 'b' to be returned unchanged.");
    }

    @Test
    void test_CanonicalFormNFD_ReturnsSameInputLigature() {
        // Arrange
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        int codepoint = '\uFB03'; // 'ﬃ' (Latin Small Ligature FFI)

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("\uFB03", result, "Expected ligature 'ﬃ' to be returned unchanged.");
    }

    @Test
    void test_CanonicalFormNFD_HandlesComposedInputDiacritic() {
        // Arrange
        Decompose decompose = new Decompose(delegate, Normalizer.Form.NFD);
        int codepoint = '\u00C5'; // 'Å' (Latin Capital Letter A WITH RING ABOVE)

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("A\u030A", result, "Expected 'Å' to be decomposed into 'A' and 'U+030A' (ring above).");
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