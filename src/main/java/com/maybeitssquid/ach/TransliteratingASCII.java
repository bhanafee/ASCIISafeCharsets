package com.maybeitssquid.ach;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.function.IntFunction;

/**
 * A custom charset implementation that transliterates Unicode code points to ASCII characters
 * using a configurable transliteration function.
 * <p>
 * This charset provides bidirectional encoding and decoding between Unicode text and a restricted
 * ASCII-based byte representation. It uses a supplied transliterator function to map Unicode code
 * points to their ASCII equivalents (which may be zero or more characters).
*/
public class TransliteratingASCII extends Charset {

    private final IntFunction<CharSequence> transliterator;

    /**
     * Initializes a new charset with the given canonical name and alias
     * set.
     *
     * @param canonicalName  The canonical name of this charset
     * @param aliases        An array of this charset's aliases, or null if it has no aliases
     * @param transliterator The function to convert a code point into zero or more characters
     */
    protected TransliteratingASCII(final String canonicalName, final String[] aliases, final IntFunction<CharSequence> transliterator) {
        super(canonicalName, aliases);
        this.transliterator = transliterator;
    }

    /**
     * Determines whether this charset provides identity mapping for all ASCII characters.
     * <p>
     * This method verifies that the transliterator preserves every character in the ASCII range
     * (0x00-0x7F) without modification. For each ASCII code point, it checks that:
     * </p>
     * <ul>
     *   <li>The transliterator returns a non-null result</li>
     *   <li>The result contains exactly one character</li>
     *   <li>That character is identical to the input character</li>
     * </ul>
     * <p>
     * This method is used internally by {@link #contains(Charset)} to determine whether
     * this charset can be considered to contain {@link StandardCharsets#US_ASCII}.
     * According to the {@code Charset} specification, a charset <em>C</em> contains
     * charset <em>D</em> if every character representable in <em>D</em> is also
     * representable in <em>C</em> with the same byte sequence.
     * </p>
     *
     * @return {@code true} if all ASCII characters (0x00-0x7F) are mapped to themselves
     *         by the transliterator; {@code false} otherwise
     * @see #contains(Charset)
     * @see StandardCharsets#US_ASCII
     */
    public boolean containsASCII() {
        for (char ch = 0; ch < 0x0080; ch++) {
            CharSequence encoding = transliterator.apply(ch);
            if (encoding == null || encoding.length() != 1 || encoding.charAt(0) != ch) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(final Charset cs) {
        if (this.equals(cs)) {
            return true;
        } else if (cs == null) {
            return false;
        } else if (StandardCharsets.US_ASCII.equals(cs)) {
            return containsASCII();
        } else {
            return false;
        }
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new CharsetDecoder(this, 1F, 1F) {
            @Override
            protected CoderResult decodeLoop(final ByteBuffer in, final CharBuffer out) {
                while (in.hasRemaining()) {
                    final byte b = in.get(in.position());
                    if (b >= 0) {
                        final CharSequence transliterated = transliterator.apply(b);
                        if (transliterated == null || transliterated.isEmpty()) {
                            return CoderResult.unmappableForLength(1);
                        } else if (transliterated.length() <= out.remaining()) {
                            in.position(in.position() + 1);
                            out.put(transliterated.toString());
                        } else {
                            return CoderResult.OVERFLOW;
                        }
                    } else {
                        return CoderResult.malformedForLength(1);
                    }
                }
                return CoderResult.UNDERFLOW;
            }
        };
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new CharsetEncoder(this, 1F, 1F, new byte[]{(byte) '?'}) {
            @Override
            protected CoderResult encodeLoop(final CharBuffer in, final ByteBuffer out) {
                while (in.hasRemaining()) {
                    final int codepoint = Character.codePointAt(in, 0);
                    final int length = Character.isSupplementaryCodePoint(codepoint) ? 2 : 1;

                    final CharSequence transliterated = transliterator.apply(codepoint);
                    if (transliterated.isEmpty()) {
                        return CoderResult.unmappableForLength(length);
                    } else if (transliterated.length() > out.remaining()) {
                        return CoderResult.OVERFLOW;
                    } else {
                        final int mark = out.position();
                        for (final char c : transliterated.toString().toCharArray()) {
                            if (c > 0x007F) {
                                out.position(mark);
                                return CoderResult.unmappableForLength(length);
                            } else {
                                out.put((byte) c);
                            }
                        }
                        in.position(in.position() + length);
                    }
                }
                return CoderResult.UNDERFLOW;
            }
        };
    }
}
