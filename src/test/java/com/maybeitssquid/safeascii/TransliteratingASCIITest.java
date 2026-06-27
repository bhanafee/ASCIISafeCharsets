package com.maybeitssquid.safeascii;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link TransliteratingASCII} charset. */
class TransliteratingASCIITest {

  /**
   * Identity mapping for ASCII code points; drops everything else. {@code containsASCII()} true.
   */
  private static final IntFunction<CharSequence> ASCII_IDENTITY =
      cp -> cp < Chainable.ASCII ? Character.toString((char) cp) : "";

  private static TransliteratingASCII charset(
      final IntFunction<CharSequence> transliterator, final String... names) {
    return new TransliteratingASCII(transliterator, names);
  }

  private static CoderResult encodeReporting(final TransliteratingASCII cs, final String input) {
    final CharsetEncoder encoder =
        cs.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
    return encoder.encode(CharBuffer.wrap(input), ByteBuffer.allocate(64), true);
  }

  private static CoderResult decodeReporting(
      final TransliteratingASCII cs, final byte[] input, final int outSize) {
    final CharsetDecoder decoder =
        cs.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
    return decoder.decode(ByteBuffer.wrap(input), CharBuffer.allocate(outSize), true);
  }

  // ----- name and aliases -----

  @Test
  void canonicalNameAndAliases() {
    final TransliteratingASCII cs = charset(ASCII_IDENTITY, "Canonical", "Alias1", "Alias2");
    assertEquals("Canonical", cs.name());
    assertTrue(cs.aliases().contains("Alias1"));
    assertTrue(cs.aliases().contains("Alias2"));
    assertEquals(2, cs.aliases().size());
  }

  @Test
  void noAliases() {
    final TransliteratingASCII cs = charset(ASCII_IDENTITY, "Solo");
    assertEquals("Solo", cs.name());
    assertTrue(cs.aliases().isEmpty());
  }

  // ----- containsASCII -----

  @Test
  void containsASCIIWhenIdentity() {
    assertTrue(charset(ASCII_IDENTITY, "Identity").containsASCII());
  }

  @Test
  void containsASCIIFalseWhenDropped() {
    final IntFunction<CharSequence> t = cp -> cp == 'A' ? "" : ASCII_IDENTITY.apply(cp);
    assertFalse(charset(t, "DropsA").containsASCII());
  }

  @Test
  void containsASCIIFalseWhenNull() {
    final IntFunction<CharSequence> t = cp -> cp == 'A' ? null : ASCII_IDENTITY.apply(cp);
    assertFalse(charset(t, "NullsA").containsASCII());
  }

  @Test
  void containsASCIIFalseWhenMultiChar() {
    final IntFunction<CharSequence> t = cp -> cp == 'A' ? "AA" : ASCII_IDENTITY.apply(cp);
    assertFalse(charset(t, "ExpandsA").containsASCII());
  }

  @Test
  void containsASCIIFalseWhenRemapped() {
    final IntFunction<CharSequence> t = cp -> cp == 'A' ? "B" : ASCII_IDENTITY.apply(cp);
    assertFalse(charset(t, "RemapsA").containsASCII());
  }

  // ----- contains(Charset) -----

  @Test
  void containsSelf() {
    final TransliteratingASCII cs = charset(ASCII_IDENTITY, "Self");
    assertTrue(cs.contains(cs));
  }

  @Test
  void containsNull() {
    assertFalse(charset(ASCII_IDENTITY, "NoNull").contains(null));
  }

  @Test
  void containsUsAsciiWhenIdentity() {
    assertTrue(charset(ASCII_IDENTITY, "Contains").contains(StandardCharsets.US_ASCII));
  }

  @Test
  void doesNotContainUsAsciiWhenLossy() {
    final IntFunction<CharSequence> t = cp -> cp == 'A' ? "" : ASCII_IDENTITY.apply(cp);
    assertFalse(charset(t, "Lossy").contains(StandardCharsets.US_ASCII));
  }

  @Test
  void doesNotContainArbitraryCharset() {
    assertFalse(charset(ASCII_IDENTITY, "NoUtf8").contains(StandardCharsets.UTF_8));
  }

  // ----- encoding -----

  @Test
  void encodesAscii() {
    final byte[] out = "Hello".getBytes(charset(ASCII_IDENTITY, "Enc"));
    assertArrayEquals("Hello".getBytes(StandardCharsets.US_ASCII), out);
  }

  @Test
  void encodesExpansion() {
    final IntFunction<CharSequence> t = cp -> cp == 0x00E6 ? "ae" : ASCII_IDENTITY.apply(cp);
    // Drive the encoder directly to assert the exact bytes of an expanding transliteration.
    final CharsetEncoder encoder = charset(t, "Expand").newEncoder();
    final ByteBuffer out = ByteBuffer.allocate(8);
    final CoderResult result = encoder.encode(CharBuffer.wrap("æ"), out, true);
    assertTrue(result.isUnderflow());
    out.flip();
    final byte[] bytes = new byte[out.remaining()];
    out.get(bytes);
    assertArrayEquals(new byte[] {'a', 'e'}, bytes);
  }

  @Test
  void encoderReportsDefaultMaxBytesPerChar() {
    // The convenience constructor declares the default bound; the encoder must advertise it.
    assertEquals(
        TransliteratingASCII.DEFAULT_MAX_BYTES_PER_CHAR,
        charset(ASCII_IDENTITY, "Max").newEncoder().maxBytesPerChar());
  }

  @Test
  void encoderHonorsExplicitMaxBytesPerChar() {
    final TransliteratingASCII cs = new TransliteratingASCII(ASCII_IDENTITY, 10F, "ExplicitMax");
    assertEquals(10F, cs.newEncoder().maxBytesPerChar());
  }

  @Test
  void getBytesHandlesExpansion() {
    // Regression: with an honest maxBytesPerChar, String.getBytes no longer overflows on expansion.
    final IntFunction<CharSequence> t = cp -> cp == 0x00E6 ? "ae" : ASCII_IDENTITY.apply(cp);
    final byte[] out = "æ".getBytes(charset(t, "GetBytesExpand"));
    assertArrayEquals(new byte[] {'a', 'e'}, out);
  }

  @Test
  void encodeUnmappableWhenEmpty() {
    final CoderResult result = encodeReporting(charset(ASCII_IDENTITY, "Drop"), "é");
    assertTrue(result.isUnmappable());
    assertEquals(1, result.length());
  }

  @Test
  void encodeUnmappableWhenResultExceedsAscii() {
    final IntFunction<CharSequence> t = cp -> cp == 'X' ? "ÿ" : ASCII_IDENTITY.apply(cp);
    final CoderResult result = encodeReporting(charset(t, "AboveAscii"), "X");
    assertTrue(result.isUnmappable());
  }

  @Test
  void encodeUnmappableSupplementaryConsumesTwo() {
    final String emoji = new String(Character.toChars(0x1F600));
    final CoderResult result = encodeReporting(charset(ASCII_IDENTITY, "Supp"), emoji);
    assertTrue(result.isUnmappable());
    assertEquals(2, result.length());
  }

  @Test
  void encodeOverflowWhenOutputFull() {
    final IntFunction<CharSequence> t = cp -> cp == 0x00E6 ? "ae" : ASCII_IDENTITY.apply(cp);
    final CharsetEncoder encoder = charset(t, "Overflow").newEncoder();
    final CoderResult result = encoder.encode(CharBuffer.wrap("æ"), ByteBuffer.allocate(1), true);
    assertTrue(result.isOverflow());
  }

  @Test
  void encodeReplacesUnmappable() {
    final byte[] out = "aéb".getBytes(charset(ASCII_IDENTITY, "Replace"));
    assertArrayEquals(new byte[] {'a', '?', 'b'}, out);
  }

  // ----- decoding -----

  @Test
  void decodesAscii() {
    final String decoded = new String(new byte[] {'H', 'i'}, charset(ASCII_IDENTITY, "Dec"));
    assertEquals("Hi", decoded);
  }

  @Test
  void decodeUnmappableWhenRejected() {
    final IntFunction<CharSequence> t = cp -> cp == 0x07 ? "" : ASCII_IDENTITY.apply(cp);
    final CoderResult result = decodeReporting(charset(t, "RejectBell"), new byte[] {0x07}, 16);
    assertTrue(result.isUnmappable());
    assertEquals(1, result.length());
  }

  @Test
  void decodeMalformedForHighByte() {
    final CoderResult result =
        decodeReporting(charset(ASCII_IDENTITY, "Malformed"), new byte[] {(byte) 0x80}, 16);
    assertTrue(result.isMalformed());
    assertEquals(1, result.length());
  }

  @Test
  void decodeOverflowWhenOutputFull() {
    final CharsetDecoder decoder = charset(ASCII_IDENTITY, "DecOverflow").newDecoder();
    final CoderResult result =
        decoder.decode(ByteBuffer.wrap(new byte[] {'A'}), CharBuffer.allocate(0), true);
    assertTrue(result.isOverflow());
  }
}
