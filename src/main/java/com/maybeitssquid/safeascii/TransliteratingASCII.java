package com.maybeitssquid.safeascii;

import static com.maybeitssquid.safeascii.internal.Chainable.ASCII;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * A custom charset implementation that transliterates Unicode code points to ASCII characters using
 * a configurable transliteration function.
 *
 * <p>This charset provides bidirectional encoding and decoding between Unicode text and a
 * restricted ASCII-based byte representation. It uses a supplied transliterator function to map
 * Unicode code points to their ASCII equivalents (which may be zero or more characters).
 */
public class TransliteratingASCII extends Charset {

  /**
   * Default upper bound on the number of ASCII bytes a single input character may transliterate to,
   * used by the convenience constructor. The aggressive pipeline can expand one character into
   * several bytes; the longest known case is U+33AF SQUARE RAD OVER S SQUARED, which maps to {@code
   * "rad/s2"} (6 bytes). A transliterator that can expand further must be paired with a larger
   * bound via {@link #TransliteratingASCII(IntFunction, float, String...)}.
   */
  public static final float DEFAULT_MAX_BYTES_PER_CHAR = 6F;

  private final IntFunction<CharSequence> transliterator;

  private final float maxBytesPerChar;

  private static String[] aliases(final String[] names) {
    if (names.length > 1) {
      final String[] aliases = new String[names.length - 1];
      System.arraycopy(names, 1, aliases, 0, aliases.length);
      return aliases;
    } else {
      return new String[0];
    }
  }

  /**
   * Initializes a new charset with the given canonical name and alias set, declaring the maximum
   * number of ASCII bytes any single input code point may transliterate to.
   *
   * <p>The bound is what {@link java.nio.charset.CharsetEncoder#maxBytesPerChar()} reports, so it
   * must not be underestimated: {@link String#getBytes(Charset)} sizes its output buffer from it
   * and would otherwise overflow. It must be at least {@code 1} (the encoder's average bytes per
   * char).
   *
   * @param transliterator The function to convert a code point into zero or more characters
   * @param maxBytesPerChar The largest number of ASCII bytes the transliterator emits for one input
   *     code point; must be finite and {@code >= 1}
   * @param names The canonical name of this charset followed by any aliases
   */
  protected TransliteratingASCII(
      final IntFunction<CharSequence> transliterator,
      final float maxBytesPerChar,
      final String... names) {
    super(names[0], aliases(names));
    this.transliterator = Objects.requireNonNull(transliterator, "transliterator");
    if (!Float.isFinite(maxBytesPerChar) || maxBytesPerChar < 1F) {
      throw new IllegalArgumentException(
          "maxBytesPerChar must be finite and at least 1: " + maxBytesPerChar);
    }
    this.maxBytesPerChar = maxBytesPerChar;
  }

  /**
   * Initializes a new charset with the given canonical name and alias set, using {@link
   * #DEFAULT_MAX_BYTES_PER_CHAR} as the maximum bytes-per-character bound.
   *
   * @param transliterator The function to convert a code point into zero or more characters
   * @param names The canonical name of this charset followed by any aliases
   * @see #TransliteratingASCII(IntFunction, float, String...)
   */
  protected TransliteratingASCII(
      final IntFunction<CharSequence> transliterator, final String... names) {
    this(transliterator, DEFAULT_MAX_BYTES_PER_CHAR, names);
  }

  /**
   * Determines whether this charset provides identity mapping for all ASCII characters.
   *
   * <p>This method verifies that the transliterator preserves every character in the ASCII range
   * (0x00-0x7F) without modification. For each ASCII code point, it checks that:
   *
   * <ul>
   *   <li>The transliterator returns a non-null result
   *   <li>The result contains exactly one character
   *   <li>That character is identical to the input character
   * </ul>
   *
   * <p>This method is used internally by {@link #contains(Charset)} to determine whether this
   * charset can be considered to contain {@link StandardCharsets#US_ASCII}. According to the {@code
   * Charset} specification, a charset <em>C</em> contains charset <em>D</em> if every character
   * representable in <em>D</em> is also representable in <em>C</em> with the same byte sequence.
   *
   * @return {@code true} if all ASCII characters (0x00-0x7F) are mapped to themselves by the
   *     transliterator; {@code false} otherwise
   * @see #contains(Charset)
   * @see StandardCharsets#US_ASCII
   */
  public boolean containsASCII() {
    for (char ch = 0; ch < ASCII; ch++) {
      CharSequence encoding = transliterator.apply(ch);
      if (encoding == null || encoding.length() != 1 || encoding.charAt(0) != ch) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether this charset contains the given charset.
   *
   * <p>Behavior: returns {@code true} if {@code cs} is this instance; {@code false} if {@code cs}
   * is {@code null}; delegates to {@link #containsASCII()} when {@link
   * java.nio.charset.StandardCharsets#US_ASCII} is supplied; otherwise {@code false}.
   *
   * @param cs the charset to test, may be {@code null}
   * @return {@code true} if this charset contains {@code cs} per {@link
   *     java.nio.charset.Charset#contains(Charset)}
   * @see java.nio.charset.Charset#contains(Charset)
   * @see #containsASCII()
   */
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

  /**
   * Creates a decoder that transliterates single bytes (0x00–0x7F) to characters. Only characters
   * that are allowed by the configured transliteration function are returned.
   *
   * <p>Recommendation: prefer standard decoders (e.g., UTF-8, US-ASCII, ISO-8859-1, or
   * windows-1252) for general-purpose ASCII decoding; they handle a wider range of inputs. Use this
   * decoder only when input processing must be extremely rigid and only exactly compliant input is
   * allowed.
   *
   * @return a {@link java.nio.charset.CharsetDecoder} that applies the transliterator to single
   *     bytes
   * @see java.nio.charset.CharsetDecoder
   * @see java.nio.charset.StandardCharsets
   */
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
            } else if (out.remaining() >= 1) {
              in.position(in.position() + 1);
              out.put((char) b);
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

  /**
   * Creates an encoder that maps Unicode code points to ASCII bytes using the transliterator.
   *
   * <p>Behavior: valid UTF-16 input is passed to the transliterator one Unicode code point at a
   * time. Isolated surrogate code units are reported as malformed before the transliterator runs.
   * If the result is empty, the input is reported unmappable for its length (1 or 2 for
   * supplementary code points). If any resulting character is > 0x7F the input is unmappable. If
   * the output buffer lacks space the encoder returns {@link
   * java.nio.charset.CoderResult#OVERFLOW}. Valid transliterations are written as their low-7-bit
   * byte values.
   *
   * <p>The encoder consumes the correct input length for supplementary code points and uses {@code
   * '?'} as the replacement byte for unmappable input.
   *
   * @return a {@link java.nio.charset.CharsetEncoder} that emits ASCII bytes per the transliterator
   */
  @Override
  public CharsetEncoder newEncoder() {
    return new CharsetEncoder(this, 1F, maxBytesPerChar, new byte[] {(byte) '?'}) {
      @Override
      protected CoderResult encodeLoop(final CharBuffer in, final ByteBuffer out) {
        while (in.hasRemaining()) {
          final int position = in.position();
          final char first = in.get(position);
          final int codepoint;
          final int length;
          if (Character.isHighSurrogate(first)) {
            if (in.remaining() < 2) {
              return CoderResult.UNDERFLOW;
            }
            final char second = in.get(position + 1);
            if (!Character.isLowSurrogate(second)) {
              return CoderResult.malformedForLength(1);
            }
            codepoint = Character.toCodePoint(first, second);
            length = 2;
          } else if (Character.isLowSurrogate(first)) {
            return CoderResult.malformedForLength(1);
          } else {
            codepoint = first;
            length = 1;
          }

          final CharSequence transliterated = transliterator.apply(codepoint);
          if (transliterated == null || transliterated.isEmpty()) {
            return CoderResult.unmappableForLength(length);
          } else if (transliterated.length() > out.remaining()) {
            return CoderResult.OVERFLOW;
          } else {
            final int mark = out.position();
            final int len = transliterated.length();
            for (int i = 0; i < len; i++) {
              final char c = transliterated.charAt(i);
              if (c >= ASCII) {
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
