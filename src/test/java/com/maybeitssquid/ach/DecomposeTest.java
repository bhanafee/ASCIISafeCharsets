package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Decompose} class, specifically the {@code process} method.
 * The {@code process} method normalizes a single Unicode codepoint based on the specified or default normalization form.
 */
class DecomposeTest {

    @Test
    void testProcess_NormalizedInput_ReturnsSameInput() {
        // Arrange
        IntFunction<CharSequence> delegate = Character::toString;
        Decompose decompose = new Decompose(delegate);
        int codepoint = 'A'; // 'A' is already normalized

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("A", result, "Expected normalized input to be returned unchanged.");
    }

    @Test
    void testProcess_DefaultFormNFKD_HandlesUnnormalizedInput() {
        // Arrange
        IntFunction<CharSequence> delegate = Character::toString;
        Decompose decompose = new Decompose(delegate); // Default uses Normalizer.Form.NFKD
        int codepoint = '\uFB01'; // 'ﬁ' (Latin Small Ligature FI)

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("fi", result, "Expected ligature 'ﬁ' to be decomposed into 'f' and 'i'.");
    }

    @Test
    void testProcess_DefaultFormNFKD_NormalizedInput() {
        // Arrange
        IntFunction<CharSequence> delegate = Character::toString;
        Decompose decompose = new Decompose(delegate); // Default uses Normalizer.Form.NFKD
        int codepoint = 'b'; // 'b' is already normalized

        // Act
        CharSequence result = decompose.apply(codepoint);

        // Assert
        assertEquals("b", result, "Expected normalized input 'b' to be returned unchanged.");
    }

    @Test
    void testConstructor_FormNFC_ThrowsException() {
        // Arrange
        IntFunction<CharSequence> delegate = Character::toString;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFC));
    }

    @Test
    void testConstructor_FormNFKC_ThrowsException() {
        // Arrange
        IntFunction<CharSequence> delegate = Character::toString;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Decompose(delegate, Normalizer.Form.NFKC));
    }
}