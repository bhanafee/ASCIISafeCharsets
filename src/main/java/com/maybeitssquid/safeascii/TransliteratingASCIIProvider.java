package com.maybeitssquid.safeascii;

import com.maybeitssquid.safeascii.internal.ASCIIFilter;
import com.maybeitssquid.safeascii.internal.Cache;
import com.maybeitssquid.safeascii.internal.Decompose;
import com.maybeitssquid.safeascii.internal.Name;
import com.maybeitssquid.safeascii.internal.SingleCharacterFilter;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

/**
 * A {@link CharsetProvider} that supplies ASCII-safe character sets for encoding Unicode text. This
 * provider offers five distinct character set implementations:
 *
 * <dl>
 *   <dt>X-ASCII-Printable (alias: ASCII-Printable)
 *   <dd>Strict printable ASCII: allows 0x20 through 0x7E inclusive. Control characters, including
 *       tabs and newlines, are reported as unmappable.
 *   <dt>X-ASCII-Plain (alias: ASCII-Plain)
 *   <dd>Printable ASCII with newline support: linefeed (0x0A) passes through; carriage return
 *       (0x0D) is unmappable so CRLF normalises to LF under the {@code IGNORE} error action. All
 *       other control characters are unmappable.
 *   <dt>X-ASCII-Formatted (alias: ASCII-Formatted)
 *   <dd>Printable ASCII with tab and newline support: tab (0x09) and linefeed (0x0A) pass through;
 *       carriage return (0x0D) is unmappable so CRLF normalises to LF under the {@code IGNORE}
 *       error action. All other control characters are unmappable.
 *   <dt>X-Transliterating
 *   <dd>Aggressive Unicode-to-ASCII transliteration using NFKD decomposition and character-name
 *       lookup. Output length may vary (one Unicode character may produce multiple ASCII bytes).
 *   <dt>X-Transliterating-Single-Byte (alias: ACH)
 *   <dd>Same transliteration as X-Transliterating but guarantees 1:1 character output. Any
 *       transliteration that would produce more or fewer than one character is rejected as
 *       unmappable.
 * </dl>
 */
public class TransliteratingASCIIProvider extends CharsetProvider {

  /** Canonical name for the strict printable ASCII character set. */
  public static final String ASCII_PRINTABLE_CHARSET = "X-ASCII-Printable";

  /** Alias for {@link #ASCII_PRINTABLE_CHARSET}. */
  public static final String ASCII_PRINTABLE_ALIAS = "ASCII-Printable";

  /** Canonical name for the printable ASCII character set with newline support. */
  public static final String ASCII_PLAIN_CHARSET = "X-ASCII-Plain";

  /** Alias for {@link #ASCII_PLAIN_CHARSET}. */
  public static final String ASCII_PLAIN_ALIAS = "ASCII-Plain";

  /** Canonical name for the printable ASCII character set with tab and newline support. */
  public static final String ASCII_FORMATTED_CHARSET = "X-ASCII-Formatted";

  /** Alias for {@link #ASCII_FORMATTED_CHARSET}. */
  public static final String ASCII_FORMATTED_ALIAS = "ASCII-Formatted";

  /** Canonical name for the transliterating character set. */
  public static final String TRANSLITERATING_CHARSET = "X-Transliterating";

  /** Canonical name for the single-byte transliterating character set. */
  public static final String TRANSLITERATING_SINGLE_BYTE_CHARSET = "X-Transliterating-Single-Byte";

  /** ACH alias for the single-byte transliterating character set. */
  public static final String ACH_ALIAS = "ACH";

  private static final List<Charset> CHARSETS =
      List.of(
          asciiPrintable(),
          asciiPlain(),
          asciiFormatted(),
          transliterating(),
          transliteratingSingleByte());

  /**
   * Creates a new TransliteratingASCIIProvider instance.
   *
   * <p>The available character sets are initialized once and safely shared by every provider
   * instance.
   */
  public TransliteratingASCIIProvider() {
    // Default constructor
  }

  private static Charset asciiPrintable() {
    final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
    final Cache transliterator = new Cache(filter);
    return new TransliteratingASCII(transliterator, ASCII_PRINTABLE_CHARSET, ASCII_PRINTABLE_ALIAS);
  }

  private static Charset asciiPlain() {
    final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
    final Cache cache = new Cache(filter);
    cache.cache(0x0A, "\n");
    cache.cache(0x0D, ""); // CR is unmappable; CRLF normalises to LF under IGNORE
    return new TransliteratingASCII(cache, ASCII_PLAIN_CHARSET, ASCII_PLAIN_ALIAS);
  }

  private static Charset asciiFormatted() {
    final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
    final Cache cache = new Cache(filter);
    cache.cache(0x09, "\t");
    cache.cache(0x0A, "\n");
    cache.cache(0x0D, ""); // CR is unmappable; CRLF normalises to LF under IGNORE
    return new TransliteratingASCII(cache, ASCII_FORMATTED_CHARSET, ASCII_FORMATTED_ALIAS);
  }

  private static Charset transliterating() {
    final ASCIIFilter filter = new ASCIIFilter();
    final IntFunction<CharSequence> transliterator = new Decompose(new Name(filter));
    final Cache cache = new Cache(transliterator);
    return new TransliteratingASCII(cache, TRANSLITERATING_CHARSET);
  }

  private static Charset transliteratingSingleByte() {
    final ASCIIFilter filter = new ASCIIFilter();
    final IntFunction<CharSequence> transliterator = new Decompose(new Name(filter));
    final SingleCharacterFilter lengthPreserving = new SingleCharacterFilter(transliterator);
    final Cache cache = new Cache(lengthPreserving);
    return new TransliteratingASCII(cache, TRANSLITERATING_SINGLE_BYTE_CHARSET, ACH_ALIAS);
  }

  /**
   * Returns an iterator over all available character sets provided by this class. The available
   * charsets are: ASCII-Printable, ASCII-Plain, ASCII-Formatted, X-Transliterating, and
   * X-Transliterating-Single-Byte.
   *
   * @return Iterator containing all supported character sets {@code @ThreadSafe} This method is
   *     thread-safe and uses lazy initialization
   */
  @Override
  public Iterator<Charset> charsets() {
    return CHARSETS.iterator();
  }

  /**
   * Retrieves a specific character set by name. Supported charset names are:
   *
   * <ul>
   *   <li>{@link #ASCII_PRINTABLE_CHARSET X-ASCII-Printable} (alias: {@link #ASCII_PRINTABLE_ALIAS
   *       ASCII-Printable})
   *   <li>{@link #ASCII_PLAIN_CHARSET X-ASCII-Plain} (alias: {@link #ASCII_PLAIN_ALIAS
   *       ASCII-Plain})
   *   <li>{@link #ASCII_FORMATTED_CHARSET X-ASCII-Formatted} (alias: {@link #ASCII_FORMATTED_ALIAS
   *       ASCII-Formatted})
   *   <li>{@link #TRANSLITERATING_CHARSET X-Transliterating}
   *   <li>{@link #TRANSLITERATING_SINGLE_BYTE_CHARSET X-Transliterating-Single-Byte} (alias: {@link
   *       #ACH_ALIAS ACH})
   * </ul>
   *
   * @param charsetName the name of the requested charset
   * @return the corresponding Charset object, or null if the requested charset is not supported
   *     {@code @ThreadSafe} This method is thread-safe
   */
  @Override
  public Charset charsetForName(final String charsetName) {
    if (charsetName == null) {
      return null;
    }
    return switch (charsetName.toUpperCase(Locale.ROOT)) {
      case "X-ASCII-PRINTABLE", "ASCII-PRINTABLE" -> CHARSETS.get(0);
      case "X-ASCII-PLAIN", "ASCII-PLAIN" -> CHARSETS.get(1);
      case "X-ASCII-FORMATTED", "ASCII-FORMATTED" -> CHARSETS.get(2);
      case "X-TRANSLITERATING" -> CHARSETS.get(3);
      case "X-TRANSLITERATING-SINGLE-BYTE", "ACH" -> CHARSETS.get(4);
      default -> null;
    };
  }
}
