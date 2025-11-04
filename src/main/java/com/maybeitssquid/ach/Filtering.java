package com.maybeitssquid.ach;

import java.util.function.IntFunction;

/**
 * Function to allow only characters that are in the ASCII subset of Unicode. This class implements
 * {@code IntFunction<char[]>} to transform input characters according to configurable rules. The input is a Unicode
 * code point, and the default output is either an array with a single character corresponding to the code point or
 * an empty array if the code point is not in the ASCII range. Encodings for specific ASCII values can be overridden
 * by the {@code encode} functions.
 *
 * <p>Key features:
 * <ul>
 *   <li>Filters non-ASCII characters (above 0x7F)
 *   <li>Allows custom character mappings within an ASCII range
 *   <li>Provides blocking capabilities for specific characters
 *   <li>Includes a utility method to block all control characters
 * </ul>
 *
 * Usage example:
 * {@snippet :
 * Filtering filter = new Filtering()
 *     .blockControls()           // Block all control characters
 *     .encode(0x40, 'X')        // Replace '@' with 'X'
 *     .block(0x24);             // Block '$' character
 *
 * char[] result = filter.apply(inputCodePoint);
 * }
 */
public class Filtering implements IntFunction<char[]> {
    /**
     * Represents an empty character array used when filtering out characters.
     */
    public static final char[] NOTHING = new char[0];

    /**
     * Maximum codepoint value for the ASCII character set.
     */
    public static final int ASCII_BOUNDARY = 0x80;

    /**
     * Internal array storing character mappings for ASCII range (0x00-0x7F).
     * Each index corresponds to an ASCII code point, containing its mapped output.
     */
    protected final char[][] ASCII = new char[ASCII_BOUNDARY][];

    /**
     * Creates a new Filtering instance with default one-to-one ASCII mappings.
     * All characters initially map to themselves within the ASCII range.
     */
    public Filtering() {
        for (char cp = 0; cp < ASCII_BOUNDARY; cp++) {
            ASCII[cp] = new char[]{cp};
        }
    }

    /**
     * Maps a specific ASCII code point to a single character.
     *
     * @param codepoint The ASCII code point to map (must be &lt; {@link #ASCII_BOUNDARY}).
     * @param as        The character to map it to
     * @return this instance for method chaining
     * @throws IllegalArgumentException if codepoint is &gt;= {@link #ASCII_BOUNDARY}
     */
    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final char as) {
        if (codepoint >= ASCII_BOUNDARY) {
            throw new IllegalArgumentException("Requested encoding of " + codepoint +
                    ", which exceeds ASCII range");
        } else {
            this.ASCII[codepoint] = new char[]{as};
        }
        return this;
    }

    /**
     * Maps a specific ASCII code point to a sequence of characters.
     *
     * @param codepoint The ASCII code point to map (must be &lt; {@link #ASCII_BOUNDARY})
     * @param as        The character sequence to map it to, or null to block
     * @return this instance for method chaining
     * @throws IllegalArgumentException if codepoint is &gt;= {@link #ASCII_BOUNDARY}
     */
    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final char[] as) {
        if (codepoint >= ASCII_BOUNDARY) {
            throw new IllegalArgumentException("Requested encoding of " + codepoint +
                    ", which exceeds ASCII range");
        } else {
            this.ASCII[codepoint] = as == null ? NOTHING : as;
        }
        return this;
    }

    /**
     * Configures mapping a specific ASCII code point to a string representation.
     *
     * @param codepoint The ASCII code point to map (must be &lt; {@link #ASCII_BOUNDARY})
     * @param as        The string to map it to, or null to block
     * @return this instance for method chaining
     * @throws IllegalArgumentException if codepoint is &gt;= {@link #ASCII_BOUNDARY}
     */
    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final String as) {
        return encode(codepoint, as == null ? NOTHING : as.toCharArray());
    }

    /**
     * Configures blocking a specific ASCII code point from output.
     *
     * @param codepoint The ASCII code point to block (must be &lt; {@link #ASCII_BOUNDARY})
     * @return this instance for method chaining
     * @throws IllegalArgumentException if codepoint is &gt;= {@link #ASCII_BOUNDARY}
     */
    public Filtering block(final int codepoint) {
        if (codepoint >= ASCII_BOUNDARY) {
            throw new IllegalArgumentException("Requested blocking of " + codepoint +
                    ", which exceeds ASCII range");
        } else {
            this.ASCII[codepoint] = NOTHING;
        }
        return this;
    }

    /**
     * Blocks all ASCII control characters (0x00-0x1F and 0x7F). This is a convenience method for blocking common
     * control characters. It is equivalent to invoking {@link #block(int)} on every codepoint from {@code 0x0000}
     * through {@code 0x001F} inclusive, and on {@code 0x7F}.
     *
     * @return this instance for method chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public Filtering blockControls() {
        for (int i = 0x00; i < 0x20; i++) {
            this.ASCII[i] = NOTHING;
        }
        this.ASCII[0x7F] = NOTHING;
        return this;
    }

    /**
     * Processes a Unicode code point according to configured mappings.
     *
     * @param value The Unicode code point to process
     * @return The mapped character sequence for ASCII characters, or empty array for non-ASCII
     */
    @Override
    public char[] apply(final int value) {
        return value < ASCII_BOUNDARY ? ASCII[value] : NOTHING;
    }
}
