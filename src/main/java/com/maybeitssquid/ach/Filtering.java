package com.maybeitssquid.ach;

import java.util.function.IntFunction;

/**
 * Function to allow only characters that are in the ASCII subset of Unicode. The input is a Unicode code point, and
 * the default output is either an array with a single character corresponding to the code point or an empty array
 * if the code point is not in the ASCII range. Encodings for specific ASCII values can be overridden by the
 * {@code encode} functions.
 */
public class Filtering implements IntFunction<char[]> {
    public static final char[] NOTHING = new char[0];

    protected final char[][] ASCII = new char[0x80][];

    public Filtering() {
        for (char cp = 0; cp < 0x80; cp++) {
            ASCII[cp] = new char[]{cp};
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final char as) {
        if (codepoint >= 0x80) {
            throw new IllegalArgumentException("Requested encoding of " + codepoint +
                    ", which exceeds 0x80 so outside ASCII range");
        } else {
            this.ASCII[codepoint] = new char[]{as};
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final char[] as) {
        if (codepoint >= 0x80) {
            throw new IllegalArgumentException("Requested encoding of " + codepoint +
                    ", which exceeds 0x80 so outside ASCII range");
        } else {
            this.ASCII[codepoint] = as == null ? NOTHING : as;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Filtering encode(final int codepoint, final String as) {
        return encode(codepoint, as == null ? NOTHING: as.toCharArray());
    }

    public Filtering block(final int codepoint) {
        if (codepoint >= 0x80) {
            throw new IllegalArgumentException("Requested blocking of " + codepoint +
                    ", which exceeds 0x80 so outside ASCII range");
        } else {
            this.ASCII[codepoint] = NOTHING;
        }
        return this;
    }

    /**
     * Shorthand equivalent to invoking {@link #block(int)} on every codepoint from {@code 0x0000} through
     * {@code 0x001F} inclusive, and on {@code 0x7F}.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Filtering blockControls() {
        for (int i = 0x00; i < 0x20; i++) {
            this.ASCII[i] = NOTHING;
        }
        this.ASCII[0x7F] = NOTHING;
        return this;
    }

    @Override
    public char[] apply(final int value) {
        return value < 0x80 ? ASCII[value] : NOTHING;
    }
}
