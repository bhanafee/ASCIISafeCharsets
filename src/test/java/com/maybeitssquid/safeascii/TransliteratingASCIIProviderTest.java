package com.maybeitssquid.safeascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for the {@link TransliteratingASCIIProvider} charset provider. */
class TransliteratingASCIIProviderTest {

  private TransliteratingASCIIProvider provider;

  @BeforeEach
  void setUp() {
    provider = new TransliteratingASCIIProvider();
  }

  /** Encodes through the charset, rendering unmappable input as the {@code ?} replacement byte. */
  private static String transliterate(final Charset cs, final String input) {
    return new String(input.getBytes(cs), StandardCharsets.US_ASCII);
  }

  @ValueSource(
      strings = {
        TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET,
        TransliteratingASCIIProvider.ASCII_PRINTABLE_ALIAS,
        TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET,
        TransliteratingASCIIProvider.ASCII_PLAIN_ALIAS,
        TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET,
        TransliteratingASCIIProvider.ASCII_FORMATTED_ALIAS,
        TransliteratingASCIIProvider.TRANSLITERATING_CHARSET,
        TransliteratingASCIIProvider.TRANSLITERATING_SINGLE_BYTE_CHARSET,
        TransliteratingASCIIProvider.ACH_ALIAS
      })
  @ParameterizedTest
  void charsetForNameResolvesKnownNames(final String name) {
    final Charset cs = provider.charsetForName(name);
    assertNotNull(cs, "Expected provider to supply charset for " + name);
  }

  @Test
  void charsetForNameReturnsNullForUnknown() {
    assertNull(provider.charsetForName("does-not-exist"));
  }

  @ValueSource(
      strings = {
        "x-ascii-printable",
        "Ascii-Plain",
        "x-ASCII-formatted",
        "x-TRANSLITERATING",
        "x-transliterating-single-byte",
        "ach"
      })
  @ParameterizedTest
  void charsetForNameIgnoresAsciiLetterCase(final String name) {
    assertNotNull(provider.charsetForName(name));
  }

  @Test
  void asciiPrintableAliasResolvesToSameInstance() {
    assertSame(
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET),
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_ALIAS));
    assertTrue(
        provider
            .charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET)
            .aliases()
            .contains(TransliteratingASCIIProvider.ASCII_PRINTABLE_ALIAS));
  }

  @Test
  void asciiPlainAliasResolvesToSameInstance() {
    assertSame(
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET),
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_ALIAS));
    assertTrue(
        provider
            .charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET)
            .aliases()
            .contains(TransliteratingASCIIProvider.ASCII_PLAIN_ALIAS));
  }

  @Test
  void asciiFormattedAliasResolvesToSameInstance() {
    assertSame(
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET),
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_ALIAS));
    assertTrue(
        provider
            .charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET)
            .aliases()
            .contains(TransliteratingASCIIProvider.ASCII_FORMATTED_ALIAS));
  }

  @Test
  void achIsAnAliasForSingleByte() {
    final Charset byCanonical =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_SINGLE_BYTE_CHARSET);
    final Charset byAlias = provider.charsetForName(TransliteratingASCIIProvider.ACH_ALIAS);
    assertSame(byCanonical, byAlias);
    assertTrue(
        byCanonical.aliases().contains(TransliteratingASCIIProvider.ACH_ALIAS),
        "Single-byte charset should advertise the ACH alias");
  }

  @Test
  void charsetForNameIsCached() {
    final Charset first =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET);
    final Charset second =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET);
    assertSame(first, second, "Provider should lazily cache and reuse charset instances");
  }

  @Test
  void charsetsIteratorExposesAllFive() {
    final List<String> names = new ArrayList<>();
    final Iterator<Charset> it = provider.charsets();
    while (it.hasNext()) {
      names.add(it.next().name());
    }
    assertEquals(5, names.size());
    assertTrue(names.contains(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET));
    assertTrue(names.contains(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET));
    assertTrue(names.contains(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET));
    assertTrue(names.contains(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET));
    assertTrue(names.contains(TransliteratingASCIIProvider.TRANSLITERATING_SINGLE_BYTE_CHARSET));
  }

  @Test
  void charsetsIteratorReturnsCachedInstances() {
    final Charset fromIterator = provider.charsets().next();
    final Charset fromLookup =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET);
    assertSame(fromLookup, fromIterator);
  }

  @Test
  void spiDiscoveryResolvesEveryCanonicalNameAndAlias() {
    final Iterator<Charset> charsets = provider.charsets();
    while (charsets.hasNext()) {
      final Charset charset = charsets.next();
      assertSame(charset, Charset.forName(charset.name()));
      for (String alias : charset.aliases()) {
        assertSame(charset, Charset.forName(alias));
      }
    }
  }

  @Test
  void concurrentLookupAndEncodingUseTheSameInitializedCharsets() throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(8);
    try {
      final List<Callable<Charset>> tasks = new ArrayList<>();
      for (int task = 0; task < 32; task++) {
        tasks.add(
            () -> {
              Charset resolved = null;
              for (int iteration = 0; iteration < 200; iteration++) {
                resolved = provider.charsetForName("x-transliterating");
                assertEquals("cafe rad/s2", transliterate(resolved, "café ㎯"));
                assertSame(
                    provider.charsetForName("ACH"),
                    provider.charsetForName("x-transliterating-single-byte"));
              }
              return resolved;
            });
      }
      final List<Future<Charset>> results = executor.invokeAll(tasks);
      final Charset expected = results.get(0).get();
      for (Future<Charset> result : results) {
        assertSame(expected, result.get());
      }
    } finally {
      executor.shutdownNow();
    }
  }

  // ----- end-to-end transliteration through each charset -----

  @Test
  void asciiPrintablePassesPrintableAndRejectsControls() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET);
    assertEquals("Hello!", transliterate(cs, "Hello!"));
    // Tab is a control character and is rejected, replaced by '?'.
    assertEquals("a?b", transliterate(cs, "a\tb"));
  }

  @Test
  void asciiPrintableDoesNotContainUsAscii() {
    // Control characters are blocked, so not every ASCII character round-trips.
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_PRINTABLE_CHARSET);
    assertFalse(cs.contains(StandardCharsets.US_ASCII));
  }

  @Test
  void asciiPlainAllowsNewline() {
    final Charset cs = provider.charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET);
    assertEquals("a\nb", transliterate(cs, "a\nb"));
  }

  @Test
  void asciiPlainRejectsCarriageReturn() {
    // Lone CR is unmappable; default REPLACE action substitutes '?'.
    final Charset cs = provider.charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET);
    assertEquals("a?b", transliterate(cs, "a\rb"));
  }

  @Test
  void asciiPlainNormalisesCRLF() {
    // With IGNORE action, CR is silently dropped so CRLF becomes LF.
    final Charset cs = provider.charsetForName(TransliteratingASCIIProvider.ASCII_PLAIN_CHARSET);
    final java.nio.charset.CharsetEncoder enc =
        cs.newEncoder().onUnmappableCharacter(java.nio.charset.CodingErrorAction.IGNORE);
    try {
      final java.nio.ByteBuffer out = enc.encode(java.nio.CharBuffer.wrap("a\r\nb"));
      assertEquals("a\nb", new String(out.array(), 0, out.limit(), StandardCharsets.US_ASCII));
    } catch (java.nio.charset.CharacterCodingException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void asciiFormattedPassesPrintableAndTab() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET);
    assertEquals("a\tb", transliterate(cs, "a\tb"));
  }

  @Test
  void asciiFormattedAllowsNewline() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET);
    assertEquals("a\nb", transliterate(cs, "a\nb"));
  }

  @Test
  void asciiFormattedRejectsCarriageReturn() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET);
    assertEquals("a?b", transliterate(cs, "a\rb"));
  }

  @Test
  void asciiFormattedDoesNotContainUsAscii() {
    // CR is unmappable, so the charset does not contain all of US-ASCII.
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.ASCII_FORMATTED_CHARSET);
    assertFalse(cs.contains(StandardCharsets.US_ASCII));
  }

  @Test
  void transliteratingDecomposesAccents() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET);
    assertEquals("cafe", transliterate(cs, "café"));
    assertEquals("naive", transliterate(cs, "naïve"));
  }

  @Test
  void transliteratingExpandsViaGetBytes() {
    // Regression: U+33AF maps to "rad/s2" (6 bytes). String.getBytes sizes its buffer from
    // maxBytesPerChar, so an underestimated bound would throw BufferOverflowException here.
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET);
    assertEquals("rad/s2", transliterate(cs, "㎯"));
  }

  @Test
  void transliteratingContainsUsAscii() {
    // No categories are blocked, so every ASCII character maps to itself.
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_CHARSET);
    assertTrue(cs.contains(StandardCharsets.US_ASCII));
  }

  @Test
  void singleByteIsLengthPreserving() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_SINGLE_BYTE_CHARSET);
    // Each input character maps to exactly one ASCII character.
    assertEquals("cafe", transliterate(cs, "café"));
    // Em dash is a single character that maps to a single hyphen.
    assertEquals("a-b", transliterate(cs, "a—b"));
  }

  @Test
  void singleByteRejectsMultiCharExpansion() {
    final Charset cs =
        provider.charsetForName(TransliteratingASCIIProvider.TRANSLITERATING_SINGLE_BYTE_CHARSET);
    // 'æ' would expand to "ae" (two characters); the single-byte charset rejects it.
    assertEquals("?", transliterate(cs, "æ"));
  }
}
